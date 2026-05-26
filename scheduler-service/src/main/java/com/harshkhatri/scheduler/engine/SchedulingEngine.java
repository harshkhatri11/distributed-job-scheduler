package com.harshkhatri.scheduler.engine;

import com.harshkhatri.scheduler.entity.Job;
import com.harshkhatri.scheduler.entity.JobExecution;
import com.harshkhatri.scheduler.enums.ExecutionStatus;
import com.harshkhatri.scheduler.enums.JobStatus;
import com.harshkhatri.scheduler.metrics.SchedulerMetrics;
import com.harshkhatri.scheduler.model.ScheduledJob;
import com.harshkhatri.scheduler.producer.JobEventProducer;
import com.harshkhatri.scheduler.repository.JobExecutionRepository;
import com.harshkhatri.scheduler.repository.JobRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulingEngine {

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final JobEventProducer jobEventProducer;
    private final StringRedisTemplate redisTemplate;
    private final SchedulerMetrics schedulerMetrics;

    private final DelayQueue<ScheduledJob> delayQueue = new DelayQueue<>();
    private final ExecutorService dispatchThread = Executors.newSingleThreadExecutor(
            r -> {
                Thread t = new Thread(r, "scheduling-engine");
                t.setDaemon(false);
                return t;
            }
    );

    private volatile boolean running = true;

    // ── Lifecycle ──────────────────────────────────────────────────

    @PostConstruct
    public void start() {
        log.info("SchedulingEngine starting — loading active jobs from DB");
        loadActiveJobs();
        dispatchThread.submit(this::dispatchLoop);
        log.info("SchedulingEngine started — {} jobs in queue", delayQueue.size());
    }

    @PreDestroy
    public void stop() {
        log.info("SchedulingEngine stopping");
        running = false;
        dispatchThread.shutdownNow();
    }

    // ── Public API ─────────────────────────────────────────────────

    public void scheduleJob(Job job) {
        Instant nextFire = calculateNextFireTime(job.getCronExpression());
        if (nextFire == null) {
            log.warn("Could not calculate next fire time for job={}", job.getId());
            return;
        }
        delayQueue.put(new ScheduledJob(job.getId(), job.getName(), nextFire));
        log.info("Scheduled job={} nextFire={}", job.getId(), nextFire);
    }

    public void removeJob(java.util.UUID jobId) {
        delayQueue.removeIf(sj -> sj.getJobId().equals(jobId));
        log.info("Removed job={} from queue", jobId);
    }

    // ── Private ────────────────────────────────────────────────────

    private void loadActiveJobs() {
        List<Job> activeJobs = jobRepository.findByStatus(JobStatus.ACTIVE);
        for (Job job : activeJobs) {
            Instant nextFire = calculateNextFireTime(job.getCronExpression());
            if (nextFire == null) {
                log.warn("Skipping job={} — invalid cron: {}", job.getId(), job.getCronExpression());
                continue;
            }
            job.setNextFireTime(nextFire);
            jobRepository.save(job);
            delayQueue.put(new ScheduledJob(job.getId(), job.getName(), nextFire));
        }
        log.info("Loaded {} active jobs into DelayQueue", activeJobs.size());
    }

    private void dispatchLoop() {
        log.info("Dispatch loop started — waiting for jobs");
        while (running) {
            try {
                ScheduledJob scheduledJob = delayQueue.poll(5, TimeUnit.SECONDS);
                if (scheduledJob == null) {
                    continue;
                }
                log.info("Dequeued job={} name={}", scheduledJob.getJobId(), scheduledJob.getJobName());
                dispatchJob(scheduledJob);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Dispatch loop interrupted — shutting down");
                break;
            } catch (Exception e) {
                log.error("Unexpected error in dispatch loop", e);
            }
        }
        log.info("Dispatch loop terminated");
    }

    @Transactional
    void dispatchJob(ScheduledJob scheduledJob) {
        Job job = jobRepository.findById(scheduledJob.getJobId()).orElse(null);
        if (job == null || job.getStatus() != JobStatus.ACTIVE) {
            log.info("Skipping job={} — not found or not ACTIVE", scheduledJob.getJobId());
            return;
        }

        String lockKey = "lock:job:" + job.getId();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked",
                        Duration.ofSeconds(job.getTimeoutSeconds()));

        if (!Boolean.TRUE.equals(acquired)) {
            log.warn("Could not acquire lock for job={} — skipping", job.getId());
            reschedule(job);
            return;
        }

        log.info("Lock acquired for job={}", job.getId());

        // Capture fire time before dispatch to measure scheduling latency
        Instant fireTime = Instant.now();

        try {
            JobExecution execution = JobExecution.builder()
                    .job(job)
                    .status(ExecutionStatus.TRIGGERED)
                    .attemptNumber(1)
                    .triggeredAt(Instant.now())
                    .build();
            jobExecutionRepository.save(execution);

            job.setLastFireTime(Instant.now());
            jobRepository.save(job);

            jobEventProducer.publishJobTrigger(job, execution);

            // Kafka publish succeeded — record the trigger and how long it took
            schedulerMetrics.recordJobTriggered();
            schedulerMetrics.recordSchedulingLatency(
                    Duration.between(fireTime, Instant.now()).toMillis()
            );

        } catch (Exception e) {
            log.error("Failed to dispatch job={}", job.getId(), e);
            // Kafka publish failed — count it so Grafana can alert on publish errors
            schedulerMetrics.recordTriggerFailure();
        } finally {
            Job fresh = jobRepository.findById(job.getId()).orElse(null);
            if (fresh != null && fresh.getStatus() == JobStatus.ACTIVE) {
                reschedule(fresh);
            } else {
                log.info("Job={} is no longer ACTIVE after dispatch — skipping reschedule", job.getId());
            }
        }
    }

    private void reschedule(Job job) {
        Instant nextFire = calculateNextFireTime(job.getCronExpression());
        if (nextFire == null) {
            log.warn("Cannot reschedule job={} — invalid cron", job.getId());
            return;
        }
        job.setNextFireTime(nextFire);
        jobRepository.save(job);
        delayQueue.put(new ScheduledJob(job.getId(), job.getName(), nextFire));
        log.info("Rescheduled job={} nextFire={}", job.getId(), nextFire);
    }

    private Instant calculateNextFireTime(String cronExpression) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            ZonedDateTime next = cron.next(ZonedDateTime.now());
            return next != null ? next.toInstant() : null;
        } catch (Exception e) {
            log.error("Invalid cron expression: {}", cronExpression, e);
            return null;
        }
    }
}
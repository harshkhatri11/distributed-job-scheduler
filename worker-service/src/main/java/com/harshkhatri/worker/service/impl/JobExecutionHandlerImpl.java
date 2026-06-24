package com.harshkhatri.worker.service.impl;

import com.harshkhatri.worker.executor.JobExecutor;
import com.harshkhatri.worker.metrics.WorkerMetrics;
import com.harshkhatri.worker.model.JobResultPayload;
import com.harshkhatri.worker.model.JobTriggerPayload;
import com.harshkhatri.worker.producer.JobResultProducer;
import com.harshkhatri.worker.service.JobExecutionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutionHandlerImpl implements JobExecutionHandler {

    private final List<JobExecutor> executors;
    private final JobResultProducer jobResultProducer;
    private final StringRedisTemplate redisTemplate;
    private final WorkerMetrics workerMetrics;

    private static final String JOB_PAUSED_KEY = "paused:job:";

    @Override
    public void handle(JobTriggerPayload payload) {
        String pausedKey = JOB_PAUSED_KEY + payload.jobId();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(pausedKey))) {
            log.info("Job={} is PAUSED — skipping stale trigger executionId={}",
                    payload.jobId(), payload.executionId());
            return;
        }

        log.info("Handling job={} type={} attempt={}",
                payload.jobId(), payload.jobType(), 1);

        JobExecutor executor = executors.stream()
                .filter(e -> e.supports() == payload.jobType())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No executor found for job type: " + payload.jobType()));

        String traceId = MDC.get("traceId");
        log.info("DEBUG traceId from MDC: {}", traceId);

        Instant startedAt = Instant.now();
        int maxAttempts = payload.maxRetries() + 1;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Attempt {}/{} for job={}", attempt, maxAttempts, payload.jobId());

                String output = executor.execute(payload);

                Instant completedAt = Instant.now();
                log.info("job={} succeeded on attempt {}", payload.jobId(), attempt);

                jobResultProducer.publishResult(new JobResultPayload(
                        payload.jobId(),
                        payload.executionId(),
                        "SUCCESS",
                        attempt,
                        startedAt,
                        completedAt,
                        output,
                        null,
                        traceId
                ));

                workerMetrics.recordSuccess();
                workerMetrics.recordExecutionDuration(
                        Duration.between(startedAt, completedAt).toMillis()
                );
                return;

            } catch (Exception e) {
                log.warn("Attempt {}/{} failed for job={}: {}",
                        attempt, maxAttempts, payload.jobId(), e.getMessage());

                workerMetrics.recordFailure();

                if (attempt == maxAttempts) {
                    log.error("job={} exhausted all {} attempts — sending to DLQ",
                            payload.jobId(), maxAttempts);

                    Instant deadAt = Instant.now();

                    jobResultProducer.publishToDlq(new JobResultPayload(
                            payload.jobId(),
                            payload.executionId(),
                            "DEAD_LETTERED",
                            attempt,
                            startedAt,
                            deadAt,
                            null,
                            e.getMessage(),
                            traceId
                    ));

                    workerMetrics.recordDeadLettered();
                    workerMetrics.recordExecutionDuration(
                            Duration.between(startedAt, deadAt).toMillis()
                    );
                } else {
                    sleep(attempt);
                }
            }
        }
    }

    private void sleep(int attempt) {
        try {
            long backoffMs = (long) Math.pow(2, attempt) * 1000L;
            log.info("Backing off {}ms before next attempt", backoffMs);
            Thread.sleep(backoffMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
package com.harshkhatri.scheduler.consumer;

import com.harshkhatri.scheduler.consumer.payload.JobResultPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harshkhatri.scheduler.entity.DeadLetterEvent;
import com.harshkhatri.scheduler.entity.Job;
import com.harshkhatri.scheduler.entity.JobExecution;
import com.harshkhatri.scheduler.engine.SchedulingEngine;
import com.harshkhatri.scheduler.enums.ExecutionStatus;
import com.harshkhatri.scheduler.enums.JobStatus;
import com.harshkhatri.scheduler.repository.DeadLetterEventRepository;
import com.harshkhatri.scheduler.repository.JobExecutionRepository;
import com.harshkhatri.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobDlqConsumer {

    private final DeadLetterEventRepository deadLetterEventRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final JobRepository jobRepository;
    private final SchedulingEngine schedulingEngine;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    @KafkaListener(
            topics = "${kafka.topics.job-dlq}",
            groupId = "scheduler-dlq-group",
            properties = {
                    "spring.json.value.default.type=com.harshkhatri.scheduler.consumer.payload.JobResultPayload"
            }
    )
    @Transactional
    public void consume(JobResultPayload payload) {
        log.warn("DLQ event received for job={} executionId={} attempts={}",
                payload.jobId(), payload.executionId(), payload.attemptNumber());

        updateExecutionStatus(payload);
        pauseJob(payload);
        writeDeadLetterEvent(payload);
    }

    private void updateExecutionStatus(JobResultPayload payload) {
        Optional<JobExecution> executionOpt = jobExecutionRepository
                .findById(payload.executionId());

        if (executionOpt.isEmpty()) {
            log.warn("No execution found for executionId={}", payload.executionId());
            return;
        }

        JobExecution execution = executionOpt.get();
        execution.setStatus(ExecutionStatus.DEAD_LETTERED);
        execution.setStartedAt(payload.startedAt());
        execution.setCompletedAt(payload.completedAt());
        execution.setErrorMessage(payload.errorMessage());
        execution.setAttemptNumber(payload.attemptNumber());
        jobExecutionRepository.save(execution);

        log.info("Updated execution={} status=DEAD_LETTERED", execution.getId());
    }

    private void pauseJob(JobResultPayload payload) {
        Optional<Job> jobOpt = jobRepository.findById(payload.jobId());

        if (jobOpt.isEmpty()) {
            log.warn("No job found for jobId={}", payload.jobId());
            return;
        }

        Job job = jobOpt.get();

        if (job.getStatus() == JobStatus.PAUSED) {
            log.info("Job={} already PAUSED, skipping", job.getId());
            // Still write Redis key in case it wasn't set (e.g. restart scenario)
            writePausedFlag(job.getId());
            return;
        }

        job.setStatus(JobStatus.PAUSED);
        jobRepository.save(job);
        schedulingEngine.removeJob(job.getId());
        writePausedFlag(job.getId());

        log.warn("Job={} paused and removed from queue after exhausting retries", job.getId());
    }

    private void writeDeadLetterEvent(JobResultPayload payload) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            payloadJson = payload.toString();
        }

        DeadLetterEvent event = DeadLetterEvent.builder()
                .jobId(payload.jobId())
                .executionId(payload.executionId())
                .payload(payloadJson)
                .failureReason(payload.errorMessage())
                .attemptsMade(payload.attemptNumber())
                .build();

        deadLetterEventRepository.save(event);
        log.warn("Written DeadLetterEvent for job={} attempts={}",
                payload.jobId(), payload.attemptNumber());
    }

    private void writePausedFlag(UUID jobId) {
        // 24 hours is enough to drain any backlog; adjust as needed
        redisTemplate.opsForValue()
                .set("paused:job:" + jobId, "1", Duration.ofHours(24));
    }
}
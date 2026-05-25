package com.harshkhatri.scheduler.consumer;

import com.harshkhatri.scheduler.consumer.payload.JobResultPayload;
import com.harshkhatri.scheduler.entity.JobExecution;
import com.harshkhatri.scheduler.enums.ExecutionStatus;
import com.harshkhatri.scheduler.repository.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobResultConsumer {

    private final JobExecutionRepository jobExecutionRepository;

    @KafkaListener(
            topics = "${kafka.topics.job-result}",
            groupId = "scheduler-result-group",
            properties = {
                    "spring.json.value.default.type=com.harshkhatri.scheduler.consumer.payload.JobResultPayload"
            }
    )
    @Transactional
    public void consume(JobResultPayload payload) {
        log.info("Received result for job={} executionId={} status={}",
                payload.jobId(), payload.executionId(), payload.status());

        Optional<JobExecution> executionOpt = jobExecutionRepository
                .findById(payload.executionId());

        if (executionOpt.isEmpty()) {
            log.warn("No execution found for executionId={} — skipping",
                    payload.executionId());
            return;
        }

        JobExecution execution = executionOpt.get();
        execution.setStatus(ExecutionStatus.valueOf(payload.status()));
        execution.setStartedAt(payload.startedAt());
        execution.setCompletedAt(payload.completedAt());
        execution.setResultOutput(payload.resultOutput());
        execution.setErrorMessage(payload.errorMessage());
        execution.setAttemptNumber(payload.attemptNumber());
        jobExecutionRepository.save(execution);

        log.info("Updated execution={} status={}", execution.getId(), execution.getStatus());
    }
}

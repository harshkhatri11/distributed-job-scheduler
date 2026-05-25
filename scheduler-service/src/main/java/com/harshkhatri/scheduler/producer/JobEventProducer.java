package com.harshkhatri.scheduler.producer;

import com.harshkhatri.scheduler.entity.Job;
import com.harshkhatri.scheduler.entity.JobExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.job-trigger}")
    private String jobTriggerTopic;

    public void publishJobTrigger(Job job, JobExecution execution) {
        JobTriggerPayload payload = new JobTriggerPayload(
                job.getId(),
                execution.getId(),
                job.getJobType(),
                job.getJobConfig(),
                job.getMaxRetries(),
                job.getTimeoutSeconds()
        );

        kafkaTemplate.send(jobTriggerTopic, job.getId().toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish trigger for job={} executionId={}",
                                job.getId(), execution.getId(), ex);
                    } else {
                        log.info("Published trigger for job={} executionId={} partition={} offset={}",
                                job.getId(),
                                execution.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public record JobTriggerPayload(
            java.util.UUID jobId,
            java.util.UUID executionId,
            com.harshkhatri.scheduler.enums.JobType jobType,
            java.util.Map<String, Object> jobConfig,
            Integer maxRetries,
            Integer timeoutSeconds
    ) {}
}

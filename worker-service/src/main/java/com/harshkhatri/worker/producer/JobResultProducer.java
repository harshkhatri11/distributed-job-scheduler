package com.harshkhatri.worker.producer;

import com.harshkhatri.worker.model.JobResultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.job-result}")
    private String jobResultTopic;

    @Value("${kafka.topics.job-dlq}")
    private String jobDlqTopic;

    public void publishResult(JobResultPayload payload) {
        kafkaTemplate.send(jobResultTopic, payload.jobId().toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish result for job={}", payload.jobId(), ex);
                    } else {
                        log.info("Published result for job={} status={} partition={} offset={}",
                                payload.jobId(),
                                payload.status(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public void publishToDlq(JobResultPayload payload) {
        kafkaTemplate.send(jobDlqTopic, payload.jobId().toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish DLQ for job={}", payload.jobId(), ex);
                    } else {
                        log.warn("Published DLQ for job={} attempts={} partition={} offset={}",
                                payload.jobId(),
                                payload.attemptNumber(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}

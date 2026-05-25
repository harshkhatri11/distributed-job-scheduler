package com.harshkhatri.worker.consumer;

import com.harshkhatri.worker.model.JobTriggerPayload;
import com.harshkhatri.worker.service.JobExecutionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventConsumer {

    private final JobExecutionHandler jobExecutionHandler;

    @KafkaListener(
            topics = "${kafka.topics.job-trigger}",
            groupId = "worker-group",
            concurrency = "3"
    )
    public void consume(JobTriggerPayload payload) {
        log.info("Received trigger for job={} type={}",
                payload.jobId(), payload.jobType());
        try {
            jobExecutionHandler.handle(payload);
        } catch (Exception e) {
            log.error("Unhandled error processing job={}", payload.jobId(), e);
        }
    }
}

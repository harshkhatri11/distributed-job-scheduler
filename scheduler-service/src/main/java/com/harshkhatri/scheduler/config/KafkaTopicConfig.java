package com.harshkhatri.scheduler.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topics.job-trigger}")
    private String jobTriggerTopic;

    @Value("${kafka.topics.job-result}")
    private String jobResultTopic;

    @Value("${kafka.topics.job-dlq}")
    private String jobDlqTopic;

    @Bean
    public NewTopic jobTriggerTopic() {
        return TopicBuilder.name(jobTriggerTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic jobResultTopic() {
        return TopicBuilder.name(jobResultTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic jobDlqTopic() {
        return TopicBuilder.name(jobDlqTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}

package com.harshkhatri.scheduler.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SchedulerMetrics {

    private final Counter jobsTriggered;
    private final Counter jobTriggerFailures;
    private final Timer jobSchedulingLatency;

    public SchedulerMetrics(MeterRegistry registry) {
        this.jobsTriggered = Counter.builder("scheduler.jobs.triggered")
                .description("Total jobs successfully dispatched to Kafka")
                .register(registry);

        this.jobTriggerFailures = Counter.builder("scheduler.jobs.trigger_failures")
                .description("Jobs that failed to publish to Kafka")
                .register(registry);

        this.jobSchedulingLatency = Timer.builder("scheduler.job.scheduling_latency")
                .description("Time from job fire time to Kafka publish completion")
                .register(registry);
    }

    public void recordJobTriggered() {
        jobsTriggered.increment();
    }

    public void recordTriggerFailure() {
        jobTriggerFailures.increment();
    }

    public void recordSchedulingLatency(long milliseconds) {
        jobSchedulingLatency.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}

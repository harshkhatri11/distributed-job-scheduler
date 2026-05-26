package com.harshkhatri.worker.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class WorkerMetrics {

    private final Counter jobsSucceeded;
    private final Counter jobsFailed;
    private final Counter jobsDeadLettered;
    private final Timer executionDuration;

    public WorkerMetrics(MeterRegistry registry) {
        this.jobsSucceeded = Counter.builder("worker.jobs.succeeded")
                .description("Jobs completed successfully")
                .register(registry);

        this.jobsFailed = Counter.builder("worker.jobs.failed")
                .description("Jobs that failed at least one attempt")
                .register(registry);

        this.jobsDeadLettered = Counter.builder("worker.jobs.dead_lettered")
                .description("Jobs that exhausted all retries and moved to DLQ")
                .register(registry);

        this.executionDuration = Timer.builder("worker.job.execution_duration")
                .description("End-to-end execution time per job from first attempt to final outcome")
                .register(registry);
    }

    public void recordSuccess() {
        jobsSucceeded.increment();
    }

    public void recordFailure() {
        jobsFailed.increment();
    }

    public void recordDeadLettered() {
        jobsDeadLettered.increment();
    }

    public void recordExecutionDuration(long milliseconds) {
        executionDuration.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}
package com.harshkhatri.scheduler.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Getter
public class ScheduledJob implements Delayed {

    private final UUID jobId;
    private final String jobName;
    private final long fireTimeEpochMillis;

    public ScheduledJob(UUID jobId, String jobName, Instant fireTime) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.fireTimeEpochMillis = fireTime.toEpochMilli();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = fireTimeEpochMillis - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(
                this.getDelay(TimeUnit.MILLISECONDS),
                other.getDelay(TimeUnit.MILLISECONDS)
        );
    }
}

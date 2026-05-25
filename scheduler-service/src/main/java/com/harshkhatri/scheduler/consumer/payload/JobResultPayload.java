package com.harshkhatri.scheduler.consumer.payload;

import java.time.Instant;
import java.util.UUID;

public record JobResultPayload(
        UUID jobId,
        UUID executionId,
        String status,
        Integer attemptNumber,
        Instant startedAt,
        Instant completedAt,
        String resultOutput,
        String errorMessage
) {}

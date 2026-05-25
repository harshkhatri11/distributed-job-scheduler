package com.harshkhatri.scheduler.api.dto.response;

import com.harshkhatri.scheduler.enums.ExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public record JobExecutionResponse(
        UUID id,
        UUID jobId,
        ExecutionStatus status,
        Integer attemptNumber,
        Instant triggeredAt,
        Instant startedAt,
        Instant completedAt,
        String resultOutput,
        String errorMessage,
        String traceId
) {}
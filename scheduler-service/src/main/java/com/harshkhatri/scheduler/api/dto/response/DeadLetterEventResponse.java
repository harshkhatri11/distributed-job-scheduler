package com.harshkhatri.scheduler.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DeadLetterEventResponse(
        UUID id,
        UUID jobId,
        UUID executionId,
        String payload,
        String failureReason,
        Integer attemptsMade,
        Instant createdAt
) {}
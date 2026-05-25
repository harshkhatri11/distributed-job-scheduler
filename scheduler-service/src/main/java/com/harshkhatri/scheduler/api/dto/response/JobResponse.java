package com.harshkhatri.scheduler.api.dto.response;

import com.harshkhatri.scheduler.enums.JobStatus;
import com.harshkhatri.scheduler.enums.JobType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record JobResponse(
        UUID id,
        String name,
        String description,
        JobType jobType,
        String cronExpression,
        Map<String, Object> jobConfig,
        JobStatus status,
        Integer maxRetries,
        Integer timeoutSeconds,
        Instant nextFireTime,
        Instant lastFireTime,
        Instant createdAt,
        Instant updatedAt
) {}

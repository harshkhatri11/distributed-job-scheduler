package com.harshkhatri.worker.model;

import com.harshkhatri.worker.enums.JobType;

import java.util.Map;
import java.util.UUID;

public record JobTriggerPayload(
        UUID jobId,
        UUID executionId,
        JobType jobType,
        Map<String, Object> jobConfig,
        Integer maxRetries,
        Integer timeoutSeconds
) {}

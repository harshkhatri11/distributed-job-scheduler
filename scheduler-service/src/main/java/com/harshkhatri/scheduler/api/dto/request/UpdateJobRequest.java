package com.harshkhatri.scheduler.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateJobRequest {

    @NotBlank(message = "Job name is required")
    private String name;

    private String description;

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    @NotNull(message = "Job config is required")
    private Map<String, Object> jobConfig;

    @Min(value = 0, message = "Max retries must be 0 or more")
    @Max(value = 10, message = "Max retries cannot exceed 10")
    private Integer maxRetries;

    @Min(value = 5, message = "Timeout must be at least 5 seconds")
    @Max(value = 3600, message = "Timeout cannot exceed 1 hour")
    private Integer timeoutSeconds;
}

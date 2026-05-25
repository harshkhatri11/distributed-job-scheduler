package com.harshkhatri.scheduler.api.dto.request;

import com.harshkhatri.scheduler.enums.JobType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;

@Data
public class CreateJobRequest {

    @NotBlank(message = "Job name is required")
    private String name;

    private String description;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    @NotNull(message = "Job config is required")
    private Map<String, Object> jobConfig;

    @Min(value = 0, message = "Max retries must be 0 or more")
    @Max(value = 10, message = "Max retries cannot exceed 10")
    private Integer maxRetries = 3;

    @Min(value = 5, message = "Timeout must be at least 5 seconds")
    @Max(value = 3600, message = "Timeout cannot exceed 1 hour")
    private Integer timeoutSeconds = 30;
}
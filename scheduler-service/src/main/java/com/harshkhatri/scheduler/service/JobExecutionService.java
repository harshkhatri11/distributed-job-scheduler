package com.harshkhatri.scheduler.service;


import com.harshkhatri.scheduler.api.dto.response.JobExecutionResponse;

import java.util.List;
import java.util.UUID;

public interface JobExecutionService {
    List<JobExecutionResponse> getExecutionsForJob(UUID jobId);
}
package com.harshkhatri.scheduler.service;


import com.harshkhatri.scheduler.api.dto.request.CreateJobRequest;
import com.harshkhatri.scheduler.api.dto.request.UpdateJobRequest;
import com.harshkhatri.scheduler.api.dto.response.JobResponse;
import com.harshkhatri.scheduler.entity.Job;

import java.util.List;
import java.util.UUID;

public interface JobService {
    JobResponse createJob(CreateJobRequest request);
    List<JobResponse> getAllJobs();
    JobResponse getJobById(UUID id);
    JobResponse updateJob(UUID id, UpdateJobRequest request);
    void deleteJob(UUID id);
    JobResponse pauseJob(UUID id);
    JobResponse resumeJob(UUID id);
    Job findActiveJob(UUID id);
    Job findJobById(UUID id);
}
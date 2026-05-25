package com.harshkhatri.scheduler.service.impl;


import com.harshkhatri.scheduler.api.dto.request.CreateJobRequest;
import com.harshkhatri.scheduler.api.dto.request.UpdateJobRequest;
import com.harshkhatri.scheduler.api.dto.response.JobResponse;
import com.harshkhatri.scheduler.api.mapper.JobMapper;
import com.harshkhatri.scheduler.engine.SchedulingEngine;
import com.harshkhatri.scheduler.entity.Job;
import com.harshkhatri.scheduler.enums.JobStatus;
import com.harshkhatri.scheduler.exception.JobNotFoundException;
import com.harshkhatri.scheduler.repository.JobRepository;
import com.harshkhatri.scheduler.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final SchedulingEngine schedulingEngine;

    @Override
    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        Job job = jobMapper.toEntity(request);
        Job saved = jobRepository.save(job);
        schedulingEngine.scheduleJob(saved);
        return jobMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs() {
        return jobRepository.findByStatusNot(JobStatus.DELETED)
                .stream()
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(UUID id) {
        return jobMapper.toResponse(findActiveJob(id));
    }

    @Override
    @Transactional
    public JobResponse updateJob(UUID id, UpdateJobRequest request) {
        Job job = findActiveJob(id);
        job.setName(request.getName());
        job.setDescription(request.getDescription());
        job.setCronExpression(request.getCronExpression());
        job.setJobConfig(request.getJobConfig());
        job.setMaxRetries(request.getMaxRetries());
        job.setTimeoutSeconds(request.getTimeoutSeconds());
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Override
    @Transactional
    public void deleteJob(UUID id) {
        Job job = findActiveJob(id);
        job.setStatus(JobStatus.DELETED);
        jobRepository.save(job);
        schedulingEngine.removeJob(id);
    }

    @Override
    @Transactional
    public JobResponse pauseJob(UUID id) {
        Job job = findActiveJob(id);
        job.setStatus(JobStatus.PAUSED);
        jobRepository.save(job);
        schedulingEngine.removeJob(id);
        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse resumeJob(UUID id) {
        Job job = findJobById(id);
        if (job.getStatus() != JobStatus.PAUSED) {
            throw new IllegalStateException("Only paused jobs can be resumed");
        }
        job.setStatus(JobStatus.ACTIVE);
        jobRepository.save(job);
        schedulingEngine.scheduleJob(job);
        return jobMapper.toResponse(job);
    }


    @Override
    public Job findActiveJob(UUID id) {
        Job job = findJobById(id);
        if (job.getStatus() == JobStatus.DELETED) {
            throw new JobNotFoundException(id);
        }
        return job;
    }

    @Override
    public Job findJobById(UUID id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }
}

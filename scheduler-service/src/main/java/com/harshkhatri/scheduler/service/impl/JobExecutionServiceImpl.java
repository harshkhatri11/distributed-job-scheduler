package com.harshkhatri.scheduler.service.impl;

import com.harshkhatri.scheduler.api.dto.response.JobExecutionResponse;
import com.harshkhatri.scheduler.api.mapper.JobExecutionMapper;
import com.harshkhatri.scheduler.repository.JobExecutionRepository;
import com.harshkhatri.scheduler.service.JobExecutionService;
import com.harshkhatri.scheduler.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobExecutionServiceImpl implements JobExecutionService {

    private final JobExecutionRepository jobExecutionRepository;
    private final JobExecutionMapper jobExecutionMapper;
    private final JobService jobService;

    @Override
    @Transactional(readOnly = true)
    public List<JobExecutionResponse> getExecutionsForJob(UUID jobId) {
        jobService.findActiveJob(jobId);
        return jobExecutionRepository.findByJobIdOrderByTriggeredAtDesc(jobId)
                .stream()
                .map(jobExecutionMapper::toResponse)
                .toList();
    }
}

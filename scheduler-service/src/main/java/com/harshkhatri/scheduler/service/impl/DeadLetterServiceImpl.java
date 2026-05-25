package com.harshkhatri.scheduler.service.impl;

import com.harshkhatri.scheduler.api.dto.response.DeadLetterEventResponse;
import com.harshkhatri.scheduler.api.mapper.DeadLetterEventMapper;
import com.harshkhatri.scheduler.repository.DeadLetterEventRepository;
import com.harshkhatri.scheduler.service.DeadLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeadLetterServiceImpl implements DeadLetterService {

    private final DeadLetterEventRepository deadLetterEventRepository;
    private final DeadLetterEventMapper deadLetterEventMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DeadLetterEventResponse> getAllDeadLetterEvents() {
        return deadLetterEventRepository.findAll()
                .stream()
                .map(deadLetterEventMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeadLetterEventResponse> getDeadLetterEventsByJobId(UUID jobId) {
        return deadLetterEventRepository.findByJobIdOrderByCreatedAtDesc(jobId)
                .stream()
                .map(deadLetterEventMapper::toResponse)
                .toList();
    }
}

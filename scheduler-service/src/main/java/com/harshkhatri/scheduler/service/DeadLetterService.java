package com.harshkhatri.scheduler.service;

import com.harshkhatri.scheduler.api.dto.response.DeadLetterEventResponse;

import java.util.List;
import java.util.UUID;

public interface DeadLetterService {
    List<DeadLetterEventResponse> getAllDeadLetterEvents();
    List<DeadLetterEventResponse> getDeadLetterEventsByJobId(UUID jobId);
}

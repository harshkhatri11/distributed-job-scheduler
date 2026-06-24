package com.harshkhatri.scheduler.api.controller;

import com.harshkhatri.scheduler.api.dto.response.DeadLetterEventResponse;
import com.harshkhatri.scheduler.service.DeadLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dlq")
@RequiredArgsConstructor
public class DeadLetterEventController {

    private final DeadLetterService deadLetterService;

    @GetMapping
    public ResponseEntity<List<DeadLetterEventResponse>> getAllEvents() {
        return ResponseEntity.ok(deadLetterService.getAllDeadLetterEvents());
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<List<DeadLetterEventResponse>> getEventsByJobId(
            @PathVariable UUID jobId) {
        return ResponseEntity.ok(deadLetterService.getDeadLetterEventsByJobId(jobId));
    }
}
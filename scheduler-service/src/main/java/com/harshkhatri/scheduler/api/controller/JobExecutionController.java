package com.harshkhatri.scheduler.api.controller;

import com.harshkhatri.scheduler.api.dto.response.JobExecutionResponse;
import com.harshkhatri.scheduler.service.JobExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobExecutionController {

    private final JobExecutionService jobExecutionService;

    @GetMapping("/{id}/executions")
    public ResponseEntity<List<JobExecutionResponse>> getExecutionsForJob(
            @PathVariable UUID id) {
        return ResponseEntity.ok(jobExecutionService.getExecutionsForJob(id));
    }
}

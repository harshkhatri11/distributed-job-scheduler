package com.harshkhatri.scheduler.api.controller;

import com.harshkhatri.scheduler.api.dto.request.CreateJobRequest;
import com.harshkhatri.scheduler.api.dto.request.UpdateJobRequest;
import com.harshkhatri.scheduler.api.dto.response.JobResponse;
import com.harshkhatri.scheduler.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.createJob(request));
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<JobResponse> pauseJob(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.pauseJob(id));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<JobResponse> resumeJob(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.resumeJob(id));
    }
}
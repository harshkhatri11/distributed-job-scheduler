package com.harshkhatri.scheduler.entity;

import com.harshkhatri.scheduler.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    private Integer attemptNumber;

    private Instant triggeredAt;
    private Instant startedAt;
    private Instant completedAt;

    @Column(columnDefinition = "text")
    private String resultOutput;

    @Column(columnDefinition = "text")
    private String errorMessage;

    private String traceId;
}

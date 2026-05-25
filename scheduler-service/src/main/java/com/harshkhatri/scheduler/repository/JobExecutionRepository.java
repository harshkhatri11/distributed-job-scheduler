package com.harshkhatri.scheduler.repository;

import com.harshkhatri.scheduler.entity.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {
    List<JobExecution> findByJobIdOrderByTriggeredAtDesc(UUID jobId);
}

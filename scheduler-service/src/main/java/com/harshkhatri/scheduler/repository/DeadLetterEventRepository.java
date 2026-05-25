package com.harshkhatri.scheduler.repository;

import com.harshkhatri.scheduler.entity.DeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, UUID> {
    List<DeadLetterEvent> findByJobIdOrderByCreatedAtDesc(UUID jobId);
}

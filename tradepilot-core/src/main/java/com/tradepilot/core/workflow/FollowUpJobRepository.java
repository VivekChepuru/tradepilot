package com.tradepilot.core.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FollowUpJobRepository extends JpaRepository<FollowUpJob, Long> {

    List<FollowUpJob> findByStatusAndScheduledAtBefore(FollowUpJobStatus status, LocalDateTime cutoff);

    List<FollowUpJob> findByOrderIdAndJobTypeAndStatus(Long orderId, FollowUpJobType type, FollowUpJobStatus status);
}
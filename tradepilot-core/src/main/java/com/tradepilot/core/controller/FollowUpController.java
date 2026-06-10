package com.tradepilot.core.controller;

import com.tradepilot.core.workflow.FollowUpJob;
import com.tradepilot.core.workflow.FollowUpJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpJobRepository followUpJobRepository;

    @GetMapping
    public List<FollowUpJob> getFollowUps() {
        log.info("Fetching all follow-up jobs");
        return followUpJobRepository.findAll(Sort.by(Sort.Direction.DESC, "scheduledAt"));
    }
}

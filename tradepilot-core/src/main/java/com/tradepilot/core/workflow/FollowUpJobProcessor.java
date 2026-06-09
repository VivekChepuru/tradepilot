package com.tradepilot.core.workflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradepilot.core.channel.SendResult;
import com.tradepilot.core.channel.WhatsAppSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpJobProcessor {

    private static final int MAX_ATTEMPTS = 3;

    private final FollowUpJobRepository followUpJobRepository;
    private final FollowUpMessageBuilder messageBuilder;
    private final WhatsAppSenderService whatsAppSenderService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60_000)
    public void processJobs() {
        try {
            List<FollowUpJob> due = followUpJobRepository
                    .findByStatusAndScheduledAtBefore(FollowUpJobStatus.PENDING, LocalDateTime.now());

            for (FollowUpJob job : due) {
                processJob(job);
            }

            log.info("Follow-up processor ran — processed {} jobs", due.size());
        } catch (Exception e) {
            log.error("Follow-up processor encountered an unexpected error", e);
        }
    }

    private void processJob(FollowUpJob job) {
        try {
            Map<String, String> context = parseContext(job.getContextPayload());
            String toNumber = context.getOrDefault("toNumber", "");
            log.debug("Processing job id={} template={} context={}", job.getId(), job.getMessageTemplate(), context);
            String message  = messageBuilder.buildMessage(job.getMessageTemplate(), context);

            SendResult result = whatsAppSenderService.send(toNumber, message);

            job.setAttemptCount(job.getAttemptCount() + 1);

            if (result.success()) {
                job.setStatus(FollowUpJobStatus.SENT);
                job.setExecutedAt(LocalDateTime.now());
            } else {
                if (job.getAttemptCount() >= MAX_ATTEMPTS) {
                    job.setStatus(FollowUpJobStatus.FAILED);
                }
            }
        } catch (Exception e) {
            log.error("Error processing follow-up job id={}", job.getId(), e);
            job.setAttemptCount(job.getAttemptCount() + 1);
            if (job.getAttemptCount() >= MAX_ATTEMPTS) {
                job.setStatus(FollowUpJobStatus.FAILED);
            }
        }

        followUpJobRepository.save(job);
    }

    private Map<String, String> parseContext(String payload) {
        if (payload == null || payload.isBlank()) {
            return new HashMap<>();
        }
        Map<String, String> raw;
        try {
            raw = objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Could not parse context_payload: {}", payload);
            return new HashMap<>();
        }

        Map<String, String> context = new HashMap<>(raw);
        context.putIfAbsent("name",  raw.getOrDefault("displayName", raw.getOrDefault("toNumber", "")));
        context.putIfAbsent("price", raw.getOrDefault("quotedPrice", ""));
        context.putIfAbsent("ref",   raw.getOrDefault("orderReference", ""));
        return context;
    }
}

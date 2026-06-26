package com.tradepilot.core.trade.approval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PendingApprovalStore {

    private final ConcurrentHashMap<String, PendingApproval> store = new ConcurrentHashMap<>();

    public void add(PendingApproval approval) {
        store.put(approval.whatsappMessageId(), approval);
        log.info("Stored pending approval for messageId={}", approval.whatsappMessageId());
    }

    public List<PendingApproval> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    public Optional<PendingApproval> findById(String messageId) {
        return Optional.ofNullable(store.get(messageId));
    }

    public void remove(String messageId) {
        store.remove(messageId);
        log.info("Removed pending approval messageId={}", messageId);
    }
}

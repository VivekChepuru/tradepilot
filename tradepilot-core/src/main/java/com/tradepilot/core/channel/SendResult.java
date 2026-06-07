package com.tradepilot.core.channel;

public record SendResult(boolean success, String messageId, String errorReason) {
}
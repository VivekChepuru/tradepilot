package com.tradepilot.core.channel;

public interface WhatsAppSenderService {
    SendResult send(String toNumber, String message);
}
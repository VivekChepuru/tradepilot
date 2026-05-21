from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class InboundMessageEvent(BaseModel):
    """Mirrors the InboundMessageEvent published by Spring Boot"""
    whatsappMessageId: str
    fromNumber: str
    displayName: Optional[str] = None
    messageType: str
    textContent: Optional[str] = None
    mediaId: Optional[str] = None
    phoneNumberId: Optional[str] = None
    receivedAt: Optional[datetime] = None

class AiProcessingResult(BaseModel):
    """What this service publishes back to Kafka after processing"""
    whatsappMessageId: str
    fromNumber: str
    detectedIntent: str
    confidenceScore: float
    extractedEntities: dict
    suggestedReply: Optional[str] = None
    routingDecision: str  # AUTO_SEND, PENDING_APPROVAL, ESCALATED
    processedAt: datetime
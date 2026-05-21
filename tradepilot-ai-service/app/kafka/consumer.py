import asyncio
import json
import logging
from aiokafka import AIOKafkaConsumer
from app.config import settings
from app.services.ai_pipeline import process_message

logger = logging.getLogger(__name__)

async def start_consumer():
    consumer = AIOKafkaConsumer(
        settings.kafka_topic_messages_inbound,
        bootstrap_servers=settings.kafka_bootstrap_servers,
        group_id=settings.kafka_consumer_group,
        auto_offset_reset="earliest",
        value_deserializer=lambda m: json.loads(m.decode("utf-8")),
    )

    await consumer.start()
    logger.info("Kafka consumer started — listening on %s",
                settings.kafka_topic_messages_inbound)

    try:
        async for message in consumer:
            logger.info("Received message from %s — offset %s",
                        message.key, message.offset)
            try:
                await process_message(message.value)
            except Exception as e:
                logger.error("Failed to process message offset %s: %s",
                             message.offset, e)
    finally:
        await consumer.stop()
        logger.info("Kafka consumer stopped")
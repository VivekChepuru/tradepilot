import json
import logging
from aiokafka import AIOKafkaProducer
from app.config import settings

logger = logging.getLogger(__name__)

_producer = None

async def get_producer() -> AIOKafkaProducer:
    global _producer
    if _producer is None:
        _producer = AIOKafkaProducer(
            bootstrap_servers=settings.kafka_bootstrap_servers,
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
            key_serializer=lambda k: k.encode("utf-8") if k else None,
        )
        await _producer.start()
        logger.info("Kafka producer started")
    return _producer

async def stop_producer():
    global _producer
    if _producer is not None:
        await _producer.stop()
        _producer = None
        logger.info("Kafka producer stopped")

async def publish_ai_result(from_number: str, result: dict):
    producer = await get_producer()
    await producer.send(
        settings.kafka_topic_ai_results,
        key=from_number,
        value=result,
    )
    logger.info("Published AI result for %s to %s",
                from_number, settings.kafka_topic_ai_results)
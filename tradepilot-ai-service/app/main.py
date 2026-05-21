import asyncio
import logging
import uvicorn
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.api.health import router as health_router
from app.kafka.consumer import start_consumer
from app.kafka.producer import stop_producer
from app.config import settings

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s — %(name)s — %(levelname)s — %(message)s",
)
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Starting TradePilot AI Service")
    consumer_task = asyncio.create_task(start_consumer())
    yield
    # Shutdown
    consumer_task.cancel()
    await stop_producer()
    logger.info("TradePilot AI Service stopped")

app = FastAPI(
    title="TradePilot AI Service",
    description="Commodity-tuned AI pipeline for B2B trade communication",
    version="0.1.0",
    lifespan=lifespan,
)

app.include_router(health_router)

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host=settings.app_host,
        port=settings.app_port,
        reload=True,
    )
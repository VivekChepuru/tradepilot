from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    kafka_bootstrap_servers: str
    kafka_consumer_group: str
    kafka_topic_messages_inbound: str
    kafka_topic_ai_results: str
    anthropic_api_key: str
    app_host: str = "0.0.0.0"
    app_port: int = 8001

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

settings = Settings()
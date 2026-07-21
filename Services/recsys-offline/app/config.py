from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    social_postgres_url: str | None = None
    social_mongo_url: str | None = None
    social_mongo_db: str = "social_db"
    recsys_dataset_output_dir: str = "data/cleaned"
    recsys_artifact_dir: str = "data/artifacts"

    def require_db_urls(self) -> None:
        missing = []
        if not self.social_postgres_url:
            missing.append("SOCIAL_POSTGRES_URL")
        if not self.social_mongo_url:
            missing.append("SOCIAL_MONGO_URL")
        if missing:
            raise ValueError(
                "Missing required database configuration: " + ", ".join(missing)
            )


def get_settings() -> Settings:
    return Settings()

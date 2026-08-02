from pathlib import Path

from dotenv import load_dotenv
from pydantic_settings import BaseSettings, SettingsConfigDict

# Load .env into os.environ so pipelines that use os.getenv
# (e.g. RECSYS_HOME_CONFIG_FROM_ENV / HOME_LTR_*) see the same file.
_ENV_PATH = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(_ENV_PATH, override=False)


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=str(_ENV_PATH),
        env_file_encoding="utf-8",
        extra="ignore",
    )

    social_postgres_url: str | None = None
    social_mongo_url: str | None = None
    social_mongo_db: str = "social_db"
    commerce_postgres_url: str | None = None
    auth_postgres_url: str | None = None
    recsys_dataset_output_dir: str = "data/cleaned"
    recsys_artifact_dir: str = "data/artifacts"
    recsys_home_sim_dir: str = "data/home_sim"
    recsys_home_artifact_dir: str = "data/home_artifacts"
    admin_base_url: str | None = None
    admin_service_token: str | None = None
    recsys_sim_allow: bool = False

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

    def require_commerce_url(self) -> None:
        if not self.commerce_postgres_url:
            raise ValueError(
                "Missing required database configuration: COMMERCE_POSTGRES_URL"
            )

    def require_sim_allow(self) -> None:
        if not self.recsys_sim_allow:
            raise ValueError(
                "Simulation/seed refused: set RECSYS_SIM_ALLOW=1 for dev-only runs"
            )


def get_settings() -> Settings:
    return Settings()

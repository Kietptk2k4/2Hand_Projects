from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_health_ok():
    response = client.get("/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "ok"
    assert body["service"] == "recsys-offline"


def test_clean_job_fails_when_db_config_missing(monkeypatch):
    monkeypatch.delenv("SOCIAL_POSTGRES_URL", raising=False)
    monkeypatch.delenv("SOCIAL_MONGO_URL", raising=False)
    # Force fresh settings without env
    from app import config

    monkeypatch.setattr(
        config,
        "get_settings",
        lambda: config.Settings(social_postgres_url=None, social_mongo_url=None),
    )
    monkeypatch.setattr("app.main.get_settings", lambda: config.Settings())

    response = client.post("/jobs/clean")
    assert response.status_code == 400
    assert "Missing required database configuration" in response.json()["detail"]


def test_stub_jobs_do_not_expose_predict():
    for path in ("/jobs/train", "/jobs/evaluate", "/jobs/export-activate"):
        response = client.post(path)
        assert response.status_code == 200
        assert response.json()["status"] == "not_implemented"

    # No recommend/predict route
    assert client.get("/recommend").status_code == 404
    assert client.post("/predict").status_code == 404

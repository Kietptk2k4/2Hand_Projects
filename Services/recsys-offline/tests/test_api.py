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
    from app import config

    monkeypatch.setattr(
        "app.main.get_settings",
        lambda: config.Settings(social_postgres_url=None, social_mongo_url=None),
    )

    response = client.post("/jobs/clean")
    assert response.status_code == 400
    assert "Missing required database configuration" in response.json()["detail"]


def test_build_dataset_fails_when_cleaned_dir_missing(monkeypatch, tmp_path):
    from app import config

    missing = tmp_path / "nope"
    monkeypatch.setattr(
        "app.main.get_settings",
        lambda: config.Settings(recsys_dataset_output_dir=str(missing)),
    )
    response = client.post("/jobs/build-dataset")
    assert response.status_code == 400


def test_split_dataset_fails_when_parquet_missing(monkeypatch, tmp_path):
    from app import config

    monkeypatch.setattr(
        "app.main.get_settings",
        lambda: config.Settings(recsys_dataset_output_dir=str(tmp_path)),
    )
    response = client.post("/jobs/split-dataset")
    assert response.status_code == 400
    assert "dataset.parquet" in response.json()["detail"]


def test_train_job_fails_when_train_parquet_missing(monkeypatch, tmp_path):
    from app import config

    monkeypatch.setattr(
        "app.main.get_settings",
        lambda: config.Settings(
            recsys_dataset_output_dir=str(tmp_path),
            recsys_artifact_dir=str(tmp_path / "arts"),
        ),
    )
    response = client.post("/jobs/train")
    assert response.status_code == 400
    assert "not found" in response.json()["detail"].lower() or "parquet" in response.json()["detail"].lower()


def test_evaluate_job_fails_when_test_parquet_missing(monkeypatch, tmp_path):
    from app import config

    monkeypatch.setattr(
        "app.main.get_settings",
        lambda: config.Settings(
            recsys_dataset_output_dir=str(tmp_path),
            recsys_artifact_dir=str(tmp_path / "arts"),
        ),
    )
    response = client.post("/jobs/evaluate")
    assert response.status_code == 400


def test_stub_jobs_do_not_expose_predict():
    for path in ("/jobs/export-activate",):
        response = client.post(path)
        assert response.status_code == 200
        assert response.json()["status"] == "not_implemented"

    assert client.get("/recommend").status_code == 404
    assert client.post("/predict").status_code == 404

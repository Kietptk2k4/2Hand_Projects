# View Commerce Home Model Registry - API & Behavior

## 1. Business Goal

Expose read-only Commerce admin APIs so FE can inspect `commerce_home_ranker` artifacts and current runtime status without adding activate/switch actions in the UI.

## 2. API Contract

- **Method:** GET
- **Base URL:** `/commerce/api/v1/admin/home`
- **Auth:** Bearer JWT - role `ADMIN` or `MODERATOR` only

### Endpoint A - List model artifacts

- **URL:** `/commerce/api/v1/admin/home/recommendation-model-artifacts`
- **Query params:**

| Param | Type | Required | Mo ta |
|-------|------|----------|-------|
| `modelName` | string | no | Mac dinh `commerce_home_ranker` khi trong/null/blank |

### Endpoint B - View runtime status

- **URL:** `/commerce/api/v1/admin/home/recommendation-model-status`
- **Query params:** none

## 3. Response - Success

### A. Artifacts

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach Commerce Home model artifact thanh cong.",
  "data": [
    {
      "modelName": "commerce_home_ranker",
      "version": 3,
      "format": "ONNX",
      "artifactPath": "artifacts/commerce_home_ranker/v3/model.onnx",
      "isActive": true,
      "trainedAt": "2026-08-02T08:30:00Z",
      "metrics": {
        "ndcg@10": 0.42
      }
    }
  ],
  "errors": null,
  "timestamp": "2026-08-02T08:45:00Z"
}
```

### B. Runtime status

**HTTP 200 OK**

```json
{
  "code": 200,
  "success": true,
  "message": "Lay trang thai Commerce Home ranking model thanh cong.",
  "data": {
    "mode": "LIGHTGBM",
    "modelVersion": 3,
    "modelName": "commerce_home_ranker",
    "reason": null,
    "configuredRankingModel": "lightgbm"
  },
  "errors": null,
  "timestamp": "2026-08-02T08:45:00Z"
}
```

`mode` co the la `LIGHTGBM` hoac `DEGRADED`. Khi `DEGRADED`, `reason` chua machine-readable fallback reason tu `HomeModelLoader.resolveRuntime()`.

## 4. Errors

| HTTP | Code | Khi nao |
|------|------|---------|
| 401 | `COMMERCE-401` | Thieu JWT / principal khong hop le |
| 403 | `COMMERCE-403` | Role khong phai `ADMIN` hoac `MODERATOR` |
| 500 | `COMMERCE-500` | JSON metrics trong registry khong parse duoc |

## 5. Business Rules

- Read-only only: khong co endpoint activate/deactivate artifact tu Commerce FE.
- Artifact payload mirror social-service admin response shape de FE co the reuse mapper.
- `metrics` duoc doc tu `model_artifacts.metrics` JSONB va parse thanh JSON object trong response.
- Runtime status doc truc tiep tu `HomeModelLoader.resolveRuntime()`; khong query DB de suy doan mode.

## 6. Related

- Change: `openspec/changes/commerce-home-hybrid-ltr`
- Migration: `Services/commerce-service/src/main/resources/db/migration/V10__commerce_home_hybrid_ltr.sql`

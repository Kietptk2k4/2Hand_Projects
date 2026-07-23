# View Recommendation Model Artifacts - API and Behavior

## 1. Muc tieu nghiep vu
- Cho phep ADMIN hoac MODERATOR xem danh sach `model_artifacts` cua model recommendation de van hanh va doi soat.
- Ho tro phan biet version dang active va version inactive/rejected thong qua `isActive` va du lieu `metrics`.

## 2. API Contract
- **Method:** `GET`
- **URL:** `/api/v1/social/admin/recommendation-model-artifacts`
- **Auth:** Bat buoc JWT Bearer token.
- **Headers:**
  - `Authorization: Bearer <access_token>`
- **Query params:**
  - `modelName` (optional, mac dinh `feed_ranker`)
- **Request body:** Khong co.

## 3. Response schema + vi du thanh cong
- Response wrapper chuan:

```json
{
  "code": 200,
  "success": true,
  "message": "Lay danh sach model artifact thanh cong.",
  "data": [
    {
      "version": 3,
      "format": "ONNX",
      "modelName": "feed_ranker",
      "artifactPath": "artifacts/feed_ranker/v3/model.onnx",
      "isActive": true,
      "trainedAt": "2026-07-22T08:30:00Z",
      "metrics": {
        "gate": {
          "status": "passed"
        },
        "auc": 0.81
      }
    },
    {
      "version": 2,
      "format": "ONNX",
      "artifactPath": "artifacts/feed_ranker/v2/model.onnx",
      "isActive": false,
      "trainedAt": "2026-07-22T07:30:00Z",
      "metrics": {
        "gate": {
          "status": "rejected_by_metrics"
        },
        "auc": 0.73
      }
    }
  ],
  "errors": null,
  "timestamp": "2026-07-22T09:00:00Z"
}
```

## 4. Danh sach ma loi + vi du loi
- **401 Unauthorized**: Thieu token hoac principal khong hop le.
- **403 Forbidden**: User khong co role `ADMIN` hoac `MODERATOR`.
- **500 Internal Server Error**: Du lieu `metrics` trong DB khong parse duoc thanh JSON.

Vi du loi forbidden:

```json
{
  "code": 403,
  "success": false,
  "message": "Access denied",
  "data": null,
  "errors": null,
  "timestamp": "2026-07-22T09:00:00Z"
}
```

## 5. Business rules
- Chi ADMIN hoac MODERATOR moi duoc goi endpoint.
- Neu `modelName` rong, null hoac chi co khoang trang thi mac dinh dung `feed_ranker`.
- Danh sach tra ve sap xep `version DESC` (moi nhat truoc).
- `metrics` duoc parse tu JSONB/text trong PostgreSQL sang JSON object trong response de FE render truc tiep.

## 6. Edge cases
- Khong co artifact nao cho model: tra `200` voi `data = []`.
- Artifact inactive nhung co `metrics.gate.status = rejected_by_metrics`: FE can hien thi badge rejected dua tren du lieu nay.
- Neu `metrics` null: tra `metrics = null`.

## 7. Phu thuoc du lieu (Mongo/Postgre)
- **PostgreSQL `model_artifacts`**:
  - Doc cac cot `model_name`, `version`, `format`, `artifact_path`, `is_active`, `trained_at`, `metrics`.
- **MongoDB**:
  - Khong su dung.

## 8. Ghi chu FE integration (`frontend-api-integration.md`)
- FE doc du lieu trong `data` nhu mot array item.
- FE co the suy ra badge:
  - `isActive = true` -> Active
  - `isActive = false` va `metrics.gate.status = rejected_by_metrics` -> Rejected
  - `isActive = false` va khong co gate reject -> Inactive
- FE khong can goi sang `recsys-offline` de doc metrics co ban cua registry.

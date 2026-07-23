# View Recommendation Model Status - API and Behavior

## 1. Muc tieu nghiep vu
- Cho phep ADMIN hoac MODERATOR xem trang thai runtime cua ranking model dang phuc vu feed.
- Ho tro phan biet feed dang chay LightGBM ONNX hay fallback rule-based.

## 2. API Contract
- **Method:** `GET`
- **URL:** `/api/v1/social/admin/recommendation-model-status`
- **Auth:** Bat buoc JWT Bearer token.
- **Query params:** Khong co.
- **Request body:** Khong co.

## 3. Response schema + vi du thanh cong

```json
{
  "code": 200,
  "success": true,
  "message": "Lay trang thai ranking model thanh cong.",
  "data": {
    "mode": "lightgbm",
    "modelVersion": 3,
    "modelName": "feed_ranker",
    "reason": null,
    "configuredRankingModel": "lightgbm"
  },
  "errors": null,
  "timestamp": "2026-07-22T09:00:00Z"
}
```

Vi du fallback rule-based:

```json
{
  "code": 200,
  "success": true,
  "message": "Lay trang thai ranking model thanh cong.",
  "data": {
    "mode": "rule_based",
    "modelVersion": null,
    "modelName": null,
    "reason": "file_not_found",
    "configuredRankingModel": "lightgbm"
  },
  "errors": null,
  "timestamp": "2026-07-22T09:00:00Z"
}
```

## 4. Danh sach ma loi
- **401 Unauthorized**: Thieu token hoac principal khong hop le.
- **403 Forbidden**: User khong co role `ADMIN` hoac `MODERATOR`.

## 5. Business rules
- Chi ADMIN hoac MODERATOR moi duoc goi endpoint.
- `mode = lightgbm` khi ONNX session san sang va model artifact active ton tai.
- `mode = rule_based` khi runtime fallback; `reason` giai thich nguyen nhan.
- `configuredRankingModel` phan anh gia tri config `social.recommendation.ranking.model`.

## 6. Ghi chu FE integration
- Hien thi banner runtime o dau trang Model registry.
- Dich `reason` sang tieng Viet cho operator (file_not_found, load_error, onnx_session_missing, config_rule_based).
- Khong can goi recsys-offline de lay runtime status.

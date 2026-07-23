export const DEFAULT_MODEL_NAME = "feed_ranker";

export const ARTIFACT_STATUS = {
  ACTIVE: "active",
  REJECTED: "rejected",
  INACTIVE: "inactive",
};

export const ARTIFACT_STATUS_LABELS = {
  [ARTIFACT_STATUS.ACTIVE]: "Đang phục vụ",
  [ARTIFACT_STATUS.REJECTED]: "Bị từ chối",
  [ARTIFACT_STATUS.INACTIVE]: "Không hoạt động",
};

export const MODEL_REGISTRY_VIEW_MODES = {
  DETAIL: "detail",
  METRICS: "metrics",
};

export const MODEL_REGISTRY_QUICK_FILTER_PRESETS = [
  { id: "all", label: "Tất cả" },
  { id: "active", label: ARTIFACT_STATUS_LABELS.active },
  { id: "rejected", label: ARTIFACT_STATUS_LABELS.rejected },
  { id: "inactive", label: ARTIFACT_STATUS_LABELS.inactive },
];

export const RUNTIME_MODE_LABELS = {
  lightgbm: "LightGBM",
  rule_based: "Rule-based fallback",
};

export const RUNTIME_REASON_LABELS = {
  file_not_found: "Không tìm thấy file model",
  load_error: "Lỗi tải model",
  onnx_session_missing: "Thiếu ONNX session",
  config_rule_based: "Cấu hình dùng rule-based",
};

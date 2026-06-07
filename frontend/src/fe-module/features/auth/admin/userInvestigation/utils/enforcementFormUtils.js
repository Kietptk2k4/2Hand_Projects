export function toIsoExpiresAt(datetimeLocalValue) {
  if (!datetimeLocalValue) return null;
  const date = new Date(datetimeLocalValue);
  if (Number.isNaN(date.getTime())) return null;
  return date.toISOString();
}

export function validateEnforcementForm({ reasonCode, description, durationMode, expiresAt }) {
  const errors = {};
  if (!reasonCode) {
    errors.reasonCode = "Vui lòng chọn lý do.";
  }
  const trimmed = (description || "").trim();
  if (!trimmed) {
    errors.description = "Vui lòng nhập mô tả chi tiết.";
  } else if (trimmed.length < 5) {
    errors.description = "Mô tả phải có ít nhất 5 ký tự.";
  }
  if (durationMode === "temporary") {
    if (!expiresAt) {
      errors.expiresAt = "Vui lòng chọn thời điểm hết hạn.";
    } else {
      const expires = new Date(expiresAt);
      if (Number.isNaN(expires.getTime()) || expires.getTime() <= Date.now()) {
        errors.expiresAt = "Thời điểm hết hạn phải trong tương lai.";
      }
    }
  }
  return errors;
}

export function buildEnforcementPayload({ reasonCode, description, durationMode, expiresAt }) {
  const payload = {
    reason_code: reasonCode,
    description: description.trim(),
  };
  if (durationMode === "temporary" && expiresAt) {
    payload.expires_at = toIsoExpiresAt(expiresAt);
  } else {
    payload.expires_at = null;
  }
  return payload;
}

export function validateRevokeForm({ reason }) {
  const errors = {};
  if (!reason) {
    errors.reason = "Vui lòng chọn lý do thu hồi.";
  }
  return errors;
}
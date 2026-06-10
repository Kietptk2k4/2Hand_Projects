import {
  getAllowedTargetStatuses,
  isTerminalShipmentStatus,
} from "../constants/shipmentOverrideConstants.js";

const REASON_MIN = 10;
const REASON_MAX = 500;

export function validateShipmentOverrideForm({
  status,
  reason,
  force,
  currentStatus,
  carrier,
  canForceWriteShipment,
}) {
  const errors = {};
  const trimmedReason = (reason || "").trim();

  if (!status) {
    errors.status = "Chọn trạng thái mới.";
  } else if (currentStatus) {
    const allowed = getAllowedTargetStatuses({ carrier, currentStatus, force });
    if (!allowed.includes(status)) {
      errors.status = force
        ? "Trạng thái không hợp lệ."
        : "Chuyển trạng thái này không được phép. Bật force nếu có quyền Super Admin.";
    }
  }

  if (!trimmedReason) {
    errors.reason = "Nhập lý do ghi đè.";
  } else if (trimmedReason.length < REASON_MIN) {
    errors.reason = `Lý do tối thiểu ${REASON_MIN} ký tự.`;
  } else if (trimmedReason.length > REASON_MAX) {
    errors.reason = `Lý do tối đa ${REASON_MAX} ký tự.`;
  }

  if (force && !canForceWriteShipment) {
    errors.force = "Thiếu quyền SHIPMENT_SUPPORT_FORCE_WRITE.";
  }

  if (
    currentStatus &&
    status &&
    status !== currentStatus &&
    isTerminalShipmentStatus(currentStatus) &&
    !force
  ) {
    errors.force = "Vận đơn đã kết thúc. Cần bật force để ghi đè.";
  }

  return errors;
}

export function resolveShipmentOverrideSubmitError(error) {
  const code = error?.code;
  if (code === 403 || code === "403" || code === "ADMIN-403") {
    return error?.message || "Bạn không có quyền ghi đè trạng thái vận đơn.";
  }
  if (code === 409 || code === "409" || code === "ADMIN-409-SHIPMENT-STATUS") {
    return error?.message || "Chuyển trạng thái không hợp lệ hoặc vận đơn đã kết thúc.";
  }
  if (code === 503 || code === "503" || code === "ADMIN-503") {
    return error?.message || "Commerce service không khả dụng.";
  }
  if (code === 404 || code === "404" || code === "ADMIN-404") {
    return "Không tìm thấy vận đơn.";
  }
  return error?.message || "Không thể ghi đè trạng thái vận đơn.";
}

export function buildShipmentOverrideConfirmMessage({ currentStatus, nextStatus, force }) {
  const suffix = force ? " (force)" : "";
  return `Ghi đè trạng thái vận đơn từ ${currentStatus || "—"} → ${nextStatus}${suffix}? Hành động này chỉ cập nhật Commerce DB.`;
}

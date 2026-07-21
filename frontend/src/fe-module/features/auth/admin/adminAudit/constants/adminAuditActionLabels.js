/** Mirrors AdminActionLogPolicy.CRITICAL_ACTION_TYPES on admin-service. */
export const CRITICAL_AUDIT_ACTION_TYPES = new Set([
  "USER_SUSPEND",
  "USER_BAN",
  "USER_RESTRICT",
  "USER_ENFORCEMENT_REVOKE",
  "PRODUCT_REMOVE",
  "PRODUCT_RESTORE",
  "REVIEW_HIDE",
  "REVIEW_REMOVE",
  "REVIEW_RESTORE",
  "SHOP_SUSPEND",
  "SHOP_CLOSE",
  "SHOP_RESTORE",
  "POST_MODERATE",
  "POST_RESTORE",
  "COMMENT_MODERATE",
  "COMMENT_RESTORE",
  "SYSTEM_CONFIG_CREATE",
  "SYSTEM_CONFIG_UPDATE",
  "SYSTEM_CONFIG_TOGGLE",
  "SYSTEM_ANNOUNCEMENT_PUBLISH",
  "SYSTEM_ANNOUNCEMENT_CANCEL",
  "REFUND_EXECUTE",
  "REFUND_REQUEST_REJECT",
  "PAYOUT_REQUEST_APPROVE",
  "PAYOUT_REQUEST_REJECT",
  "PAYOUT_REQUEST_MARK_PAID",
  "ADMIN_SESSION_REVOKE",
  "SHIPMENT_STATUS_OVERRIDE",
]);

export const AUDIT_ACTION_LABELS = {
  USER_SUSPEND: "Đình chỉ người dùng",
  USER_BAN: "Cấm người dùng",
  USER_RESTRICT: "Hạn chế người dùng",
  USER_ENFORCEMENT_REVOKE: "Gỡ thực thi người dùng",
  PRODUCT_REMOVE: "Gỡ sản phẩm",
  PRODUCT_RESTORE: "Khôi phục sản phẩm",
  REVIEW_HIDE: "Ẩn đánh giá",
  REVIEW_REMOVE: "Xóa đánh giá",
  REVIEW_RESTORE: "Khôi phục đánh giá",
  SHOP_SUSPEND: "Đình chỉ shop",
  SHOP_CLOSE: "Đóng shop",
  SHOP_RESTORE: "Mở lại shop",
  POST_MODERATE: "Kiểm duyệt bài viết",
  POST_RESTORE: "Khôi phục bài viết",
  COMMENT_MODERATE: "Kiểm duyệt bình luận",
  COMMENT_RESTORE: "Khôi phục bình luận",
  SYSTEM_CONFIG_CREATE: "Tạo cấu hình hệ thống",
  SYSTEM_CONFIG_UPDATE: "Cập nhật cấu hình hệ thống",
  SYSTEM_CONFIG_TOGGLE: "Bật/tắt cấu hình hệ thống",
  SYSTEM_ANNOUNCEMENT_CREATE: "Tạo thông báo hệ thống",
  SYSTEM_ANNOUNCEMENT_PIN: "Ghim thông báo hệ thống",
  SYSTEM_ANNOUNCEMENT_PUBLISH: "Phát hành thông báo hệ thống",
  SYSTEM_ANNOUNCEMENT_CANCEL: "Hủy thông báo hệ thống",
  REFUND_EXECUTE: "Thực hiện hoàn tiền",
  REFUND_REQUEST_REJECT: "Từ chối yêu cầu hoàn tiền",
  PAYOUT_REQUEST_APPROVE: "Duyệt yêu cầu chi trả",
  PAYOUT_REQUEST_REJECT: "Từ chối yêu cầu chi trả",
  PAYOUT_REQUEST_MARK_PAID: "Đánh dấu đã chi trả",
  ADMIN_SESSION_REVOKE: "Thu hồi phiên admin",
  SHIPMENT_STATUS_OVERRIDE: "Ghi đè trạng thái vận đơn",
  ORDER_SUPPORT_VIEW: "Xem hỗ trợ đơn hàng",
  PAYMENT_SUPPORT_VIEW: "Xem hỗ trợ thanh toán",
  SHIPMENT_SUPPORT_VIEW: "Xem hỗ trợ vận đơn",
  WEBHOOK_SUPPORT_VIEW: "Xem webhook hỗ trợ",
  PAYOUT_SUPPORT_VIEW: "Xem hỗ trợ chi trả",
  REFUND_SUPPORT_VIEW: "Xem hỗ trợ hoàn tiền",
  FINANCE_SUPPORT_VIEW: "Xem tài chính nền tảng",
  FINANCE_SELLER_DRILL_DOWN_VIEW: "Xem chi tiết tài chính seller",
  ADMIN_ACCESS_DENIED: "Truy cập admin bị từ chối",
};

export const AUDIT_TARGET_TYPE_LABELS = {
  USER: "Người dùng",
  PRODUCT: "Sản phẩm",
  REVIEW: "Đánh giá",
  SHOP: "Shop",
  POST: "Bài viết",
  COMMENT: "Bình luận",
  CONFIG: "Cấu hình",
  ANNOUNCEMENT: "Thông báo",
  ORDER: "Đơn hàng",
  PAYMENT: "Thanh toán",
  SHIPMENT: "Vận đơn",
  WEBHOOK: "Webhook",
  PAYOUT_REQUEST: "Yêu cầu chi trả",
  REFUND_REQUEST: "Yêu cầu hoàn tiền",
  PLATFORM_FINANCE: "Tài chính nền tảng",
  SECURITY: "Bảo mật",
  ADMIN_SESSION: "Phiên admin",
  CATEGORY: "Danh mục",
  BRAND: "Thương hiệu",
};

export function getAuditActionLabel(actionType) {
  if (!actionType) return "—";
  return AUDIT_ACTION_LABELS[actionType] || actionType;
}

export function getAuditTargetTypeLabel(targetType) {
  if (!targetType) return "—";
  return AUDIT_TARGET_TYPE_LABELS[targetType] || targetType;
}

export function isCriticalAuditAction(actionType) {
  return CRITICAL_AUDIT_ACTION_TYPES.has(String(actionType || "").toUpperCase());
}

export function isReadOnlyAuditAction(actionType) {
  const normalized = String(actionType || "").toUpperCase();
  return normalized.endsWith("_VIEW") || normalized === "ADMIN_ACCESS_DENIED";
}

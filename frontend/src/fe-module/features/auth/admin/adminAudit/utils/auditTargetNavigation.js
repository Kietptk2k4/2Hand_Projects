import { buildAdminSearchParams } from "../../adminUrlParams.js";
import { getAuditTargetTypeLabel } from "../constants/adminAuditActionLabels.js";

function isUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
    String(value || "").trim(),
  );
}

/**
 * @returns {{ id: string, label: string, buildParams: (preserve?: URLSearchParams) => URLSearchParams } | null}
 */
export function resolveAuditTargetNavigation(targetType, targetId) {
  const type = String(targetType || "").toUpperCase();
  const id = String(targetId || "").trim();

  if (!type) return null;

  const withId = (label, builder) => {
    if (!id) return null;
    return {
      id: `${type}:${id}`,
      label,
      buildParams: (preserve) => builder(id, preserve),
    };
  };

  switch (type) {
    case "USER":
      return withId("Mở điều tra người dùng", (userId, preserve) =>
        buildAdminSearchParams({
          section: "userInvestigation",
          tab: "profile",
          userId,
          preserve,
        }),
      );
    case "ORDER":
      return withId("Mở hỗ trợ đơn hàng", (orderId, preserve) =>
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "order-detail",
          orderId,
          preserve,
        }),
      );
    case "PAYMENT":
      return withId("Mở hỗ trợ thanh toán", (paymentId, preserve) =>
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "payment-detail",
          paymentId,
          preserve,
        }),
      );
    case "SHIPMENT":
      return withId("Mở hỗ trợ vận đơn", (shipmentId, preserve) =>
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "shipment-detail",
          shipmentId,
          preserve,
        }),
      );
    case "PRODUCT":
      return withId("Mở kiểm duyệt sản phẩm", (productId, preserve) =>
        buildAdminSearchParams({
          section: "contentModeration",
          tab: "product-moderation",
          productId,
          preserve,
        }),
      );
    case "POST":
      return withId("Mở kiểm duyệt bài viết", (postId, preserve) =>
        buildAdminSearchParams({
          section: "contentModeration",
          tab: "post-moderation",
          postId,
          preserve,
        }),
      );
    case "COMMENT":
      return withId("Mở kiểm duyệt bình luận", (commentId, preserve) =>
        buildAdminSearchParams({
          section: "contentModeration",
          tab: "comment-moderation",
          commentId,
          preserve,
        }),
      );
    case "CONFIG":
      return withId("Mở cấu hình hệ thống", (configId, preserve) =>
        buildAdminSearchParams({
          section: "systemOperations",
          tab: "system-configs",
          configId,
          preserve,
        }),
      );
    case "REVIEW":
      return id
        ? {
            id: `REVIEW:${id}`,
            label: "Mở kiểm duyệt đánh giá",
            buildParams: (preserve) =>
              buildAdminSearchParams({
                section: "contentModeration",
                tab: "review-moderation",
                reviewId: id,
                preserve,
              }),
          }
        : null;
    case "SHOP":
      return id
        ? {
            id: `SHOP:${id}`,
            label: "Mở kiểm duyệt cửa hàng",
            buildParams: (preserve) =>
              buildAdminSearchParams({
                section: "contentModeration",
                tab: "shop-moderation",
                shopId: id,
                preserve,
              }),
          }
        : null;
    case "REFUND_REQUEST":
      return {
        id: `REFUND_REQUEST:${id || "list"}`,
        label: "Mở duyệt hoàn tiền",
        buildParams: (preserve) =>
          buildAdminSearchParams({
            section: "orderSupport",
            tab: "refund-approvals",
            preserve,
          }),
      };
    case "PAYOUT_REQUEST":
      return {
        id: `PAYOUT_REQUEST:${id || "queue"}`,
        label: "Mở hàng đợi rút tiền",
        buildParams: (preserve) =>
          buildAdminSearchParams({
            section: "commerceFinance",
            tab: "payout-queue",
            preserve,
          }),
      };
    case "PLATFORM_FINANCE":
      if (isUuid(id)) {
        return withId("Mở chi tiết seller", (sellerId, preserve) =>
          buildAdminSearchParams({
            section: "commerceFinance",
            tab: "seller-detail",
            sellerId,
            preserve,
          }),
        );
      }
      return {
        id: "PLATFORM_FINANCE",
        label: "Mở tài chính nền tảng",
        buildParams: (preserve) =>
          buildAdminSearchParams({
            section: "commerceFinance",
            tab: "finance-overview",
            preserve,
          }),
      };
    case "WEBHOOK":
      return {
        id: "WEBHOOK",
        label: "Mở webhook logs",
        buildParams: (preserve) =>
          buildAdminSearchParams({
            section: "orderSupport",
            tab: "webhook-logs",
            preserve,
          }),
      };
    case "ANNOUNCEMENT":
      return id
        ? {
            id: `ANNOUNCEMENT:${id}`,
            label: "Mở thông báo hệ thống",
            buildParams: (preserve) =>
              buildAdminSearchParams({
                section: "systemOperations",
                tab: "system-announcements",
                announcementId: id,
                preserve,
              }),
          }
        : {
            id: "ANNOUNCEMENT:list",
            label: "Mở thông báo hệ thống",
            buildParams: (preserve) =>
              buildAdminSearchParams({
                section: "systemOperations",
                tab: "system-announcements",
                preserve,
              }),
          };
    case "CATEGORY":
      return {
        id: "CATEGORY",
        label: "Mở quản lý danh mục",
        buildParams: (preserve) =>
          buildAdminSearchParams({
            section: "catalogManagement",
            tab: "categories",
            preserve,
          }),
      };
    case "BRAND":
      return {
        id: "BRAND",
        label: "Mở quản lý thương hiệu",
        buildParams: (preserve) =>
          buildAdminSearchParams({
            section: "catalogManagement",
            tab: "brands",
            preserve,
          }),
      };
    default:
      return id
        ? {
            id: `${type}:${id}`,
            label: `Mở ${getAuditTargetTypeLabel(type).toLowerCase()}`,
            buildParams: null,
          }
        : null;
  }
}

export function listAuditTargetNavigations(targetType, targetId) {
  const primary = resolveAuditTargetNavigation(targetType, targetId);
  return primary ? [primary] : [];
}

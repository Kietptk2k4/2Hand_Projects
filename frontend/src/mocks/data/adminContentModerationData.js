/**
 * Admin Content Moderation — MSW QA fixtures
 *
 * Prerequisites: VITE_USE_MOCK=true, login admin@2hands.vn
 *
 * | Section / Tab | Action | Sample ID |
 * |---------------|--------|-----------|
 * | adminAudit / action-logs | Open detail drawer | audit.logId |
 * | post-moderation | moderate HIDE/REMOVE, restore | post.sample |
 * | post-moderation | 404 | post.notFound |
 * | comment-moderation | moderate, restore | comment.sample |
 * | shop-moderation | suspend | shop.active |
 * | shop-moderation | restore (reopen) | shop.suspended |
 * | product-moderation | remove | product.active |
 * | product-moderation | restore + history | product.removed |
 * | review-moderation | hide | review.visible |
 * | review-moderation | restore | review.hidden |
 *
 * Write APIs: /admin/api/v1/... (admin-service)
 * Social list APIs: GET /api/v1/social/admin/posts|comments (social-service)
 * Commerce list APIs: GET /commerce/api/v1/admin/... (unchanged)
 */
export {
  ADMIN_CONTENT_MODERATION_QA,
  buildAdminContentModerationQaUrl,
} from "../../fe-module/shared/constants/adminContentModerationQaIds.js";

export { MOCK_AUDIT_LOG_ID } from "./adminAuditData.js";
export {
  MOCK_ADMIN_SHOP_ACTIVE,
  MOCK_ADMIN_SHOP_SUSPENDED,
  MOCK_ADMIN_SHOP_CLOSED,
} from "./commerceAdminShopModerationData.js";
export {
  MOCK_ADMIN_PRODUCT_ACTIVE,
  MOCK_ADMIN_PRODUCT_REMOVED,
} from "./commerceAdminProductRemovalData.js";
export {
  MOCK_ADMIN_REVIEW_VISIBLE,
  MOCK_ADMIN_REVIEW_HIDDEN,
} from "./commerceAdminReviewModerationData.js";
import { useMemo } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  CONTENT_MODERATION_PERMISSIONS,
  hasContentModerationPermission,
} from "../constants/contentModerationPermissions.js";

export function useContentModerationPermissions() {
  const { user } = useAuthSession();
  const permissions = user?.permissions || [];

  return useMemo(
    () => ({
      permissions,
      canModeratePost: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.POST_MODERATE,
      ),
      canRestorePost: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.POST_RESTORE,
      ),
      canReadPostHistory: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.POST_MODERATION_READ,
      ),
      canModerateComment: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.COMMENT_MODERATE,
      ),
      canRestoreComment: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.COMMENT_RESTORE,
      ),
      canSuspendShop: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.SHOP_SUSPEND,
      ),
      canCloseShop: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.SHOP_CLOSE,
      ),
      canReopenShop: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.SHOP_RESTORE,
      ),
      canRemoveProduct: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.PRODUCT_REMOVE,
      ),
      canRestoreProduct: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.PRODUCT_RESTORE,
      ),
      canReadProductHistory: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.PRODUCT_MODERATION_READ,
      ),
      canHideReview: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.REVIEW_HIDE,
      ),
      canRemoveReview: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.REVIEW_REMOVE,
      ),
      canRestoreReview: hasContentModerationPermission(
        permissions,
        CONTENT_MODERATION_PERMISSIONS.REVIEW_RESTORE,
      ),
    }),
    [permissions],
  );
}
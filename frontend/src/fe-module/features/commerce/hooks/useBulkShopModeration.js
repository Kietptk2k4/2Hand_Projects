import { useCallback, useState } from "react";
import {
  closeShop,
  reopenShop,
  suspendShop,
} from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import { MODERATION_ACTIONS } from "../constants/adminShopModerationConstants.js";
import { mapShopModerationResponse } from "../../auth/admin/contentModeration/utils/contentModerationAdminMapper.js";
import { mapContentModerationApiError } from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";

async function executeShopBulkAction(shopId, mode, { action, reason, note }) {
  const body = { reason, note };
  if (mode === "restore") {
    const data = await reopenShop(shopId, body);
    return { shopId, result: mapShopModerationResponse(data) };
  }
  if (action === MODERATION_ACTIONS.SUSPEND) {
    const data = await suspendShop(shopId, body);
    return { shopId, result: mapShopModerationResponse(data) };
  }
  if (action === MODERATION_ACTIONS.CLOSE) {
    const data = await closeShop(shopId, body);
    return { shopId, result: mapShopModerationResponse(data) };
  }
  throw { code: "ADMIN-400-VALIDATION", message: "Hành động không hợp lệ." };
}

export function useBulkShopModeration() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const execute = useCallback(async ({ shopIds, mode, action, reason, note }) => {
    if (!shopIds?.length) {
      return { succeeded: [], failed: [] };
    }

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const results = await Promise.allSettled(
        shopIds.map((shopId) => executeShopBulkAction(shopId, mode, { action, reason, note })),
      );

      const succeeded = [];
      const failed = [];

      results.forEach((entry, index) => {
        const shopId = shopIds[index];
        if (entry.status === "fulfilled") {
          succeeded.push(entry.value);
        } else {
          failed.push({
            shopId,
            message: mapContentModerationApiError(entry.reason),
          });
        }
      });

      if (failed.length && !succeeded.length) {
        setSubmitError(failed[0]?.message || "Không thể thực hiện thao tác hàng loạt.");
      }

      return { succeeded, failed };
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  const clearError = useCallback(() => setSubmitError(""), []);

  return {
    isSubmitting,
    submitError,
    execute,
    clearError,
  };
}

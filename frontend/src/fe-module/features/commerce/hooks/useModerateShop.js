import { useCallback, useState } from "react";
import {
  closeShop,
  reopenShop,
  suspendShop,
} from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import { mapAdminShopModerationApiError } from "../constants/adminShopModerationConstants";
import { MODERATION_ACTIONS } from "../constants/adminShopModerationConstants";
import {
  mapModerationPayload,
  mapShopModerationResponse,
} from "../../auth/admin/contentModeration/utils/contentModerationAdminMapper.js";
import {
  isAdminUnauthorizedError,
  mapContentModerationApiError,
} from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

async function executeShopAction(shopId, action, payload) {
  const body = mapModerationPayload(payload);
  if (action === MODERATION_ACTIONS.SUSPEND) {
    return suspendShop(shopId, body);
  }
  if (action === MODERATION_ACTIONS.CLOSE) {
    return closeShop(shopId, body);
  }
  if (action === MODERATION_ACTIONS.RESTORE) {
    return reopenShop(shopId, body);
  }
  throw { code: "ADMIN-400-VALIDATION", message: "Hanh dong khong hop le." };
}

export function useModerateShop({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const submit = useCallback(
    async (shopId, { action, reason }) => {
      const trimmed = String(reason ?? "").trim();
      if (!trimmed) {
        setSubmitError("Vui lòng nhập lý do moderation.");
        return null;
      }

      setIsSubmitting(true);
      setSubmitError("");

      try {
        const raw = await executeShopAction(shopId, action, { reason: trimmed });
        const result = mapShopModerationResponse(raw);
        onSuccess?.(result, action);
        return result;
      } catch (error) {
        if (isAdminUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setSubmitError(
          mapContentModerationApiError(error, mapAdminShopModerationApiError(error)),
        );
        return null;
      } finally {
        setIsSubmitting(false);
      }
    },
    [onSuccess, showSessionExpired],
  );

  const clearError = useCallback(() => setSubmitError(""), []);

  return {
    isSubmitting,
    submitError,
    submit,
    clearError,
  };
}

import { useCallback, useState } from "react";
import { moderateShop } from "../api/adminShopModerationApi";
import { mapAdminShopModerationApiError } from "../constants/adminShopModerationConstants";
import {
  mapModerateShopPayload,
  mapModerateShopResponse,
} from "../utils/adminShopModerationMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
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
        const raw = await moderateShop(shopId, mapModerateShopPayload({ action, reason: trimmed }));
        const result = mapModerateShopResponse(raw);
        onSuccess?.(result, action);
        return result;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setSubmitError(mapAdminShopModerationApiError(error));
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

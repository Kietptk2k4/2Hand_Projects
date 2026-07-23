import { useCallback, useState } from "react";
import { restoreProduct } from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import {
  mapModerationPayload,
  mapProductModerationResponse,
} from "../../auth/admin/contentModeration/utils/contentModerationAdminMapper.js";
import {
  isAdminUnauthorizedError,
  mapContentModerationApiError,
} from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";
import { mapAdminProductRemovalApiError } from "../constants/adminProductRemovalConstants";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useRestoreProductByAdmin({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const submit = useCallback(
    async (productId, { reason, note }) => {
      const trimmed = String(reason ?? "").trim();
      if (!trimmed) {
        setSubmitError("Vui lòng nhập lý do khôi phục sản phẩm.");
        return null;
      }

      setIsSubmitting(true);
      setSubmitError("");

      try {
        const raw = await restoreProduct(productId, mapModerationPayload({ reason: trimmed, note }));
        const result = mapProductModerationResponse(raw);
        onSuccess?.(result);
        return result;
      } catch (error) {
        if (isAdminUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setSubmitError(
          mapContentModerationApiError(error, mapAdminProductRemovalApiError(error)),
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

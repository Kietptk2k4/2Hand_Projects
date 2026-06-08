import { useCallback, useState } from "react";
import { removeProduct } from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
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

export function useRemoveProductByAdmin({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const submit = useCallback(
    async (productId, { reason }) => {
      const trimmed = String(reason ?? "").trim();
      if (!trimmed) {
        setSubmitError("Vui lòng nhập lý do gỡ sản phẩm.");
        return null;
      }

      setIsSubmitting(true);
      setSubmitError("");

      try {
        const raw = await removeProduct(productId, mapModerationPayload({ reason: trimmed }));
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

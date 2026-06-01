import { useCallback, useState } from "react";
import { removeProductByAdmin } from "../api/adminProductRemovalApi";
import { mapAdminProductRemovalApiError } from "../constants/adminProductRemovalConstants";
import {
  mapRemoveProductPayload,
  mapRemoveProductResponse,
} from "../utils/adminProductRemovalMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

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
        const raw = await removeProductByAdmin(
          productId,
          mapRemoveProductPayload({ reason: trimmed }),
        );
        const result = mapRemoveProductResponse(raw);
        onSuccess?.(result);
        return result;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setSubmitError(mapAdminProductRemovalApiError(error));
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

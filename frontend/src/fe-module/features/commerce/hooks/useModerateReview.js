import { useCallback, useState } from "react";
import { moderateReview } from "../api/adminReviewModerationApi";
import { mapAdminReviewModerationApiError } from "../constants/adminReviewModerationConstants";
import {
  mapModerateReviewPayload,
  mapModerateReviewResponse,
} from "../utils/adminReviewModerationMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useModerateReview({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const submit = useCallback(
    async (reviewId, { action, reason }) => {
      const trimmed = String(reason ?? "").trim();
      if (!trimmed) {
        setSubmitError("Vui lòng nhập lý do kiểm duyệt.");
        return null;
      }

      setIsSubmitting(true);
      setSubmitError("");

      try {
        const raw = await moderateReview(reviewId, mapModerateReviewPayload({ action, reason: trimmed }));
        const result = mapModerateReviewResponse(raw);
        onSuccess?.(result, action);
        return result;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setSubmitError(mapAdminReviewModerationApiError(error));
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

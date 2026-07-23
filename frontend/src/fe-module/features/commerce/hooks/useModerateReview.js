import { useCallback, useState } from "react";
import {
  hideReview,
  removeReview,
  restoreReview,
} from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import { MODERATION_ACTIONS } from "../constants/adminReviewModerationConstants";
import { mapAdminReviewModerationApiError } from "../constants/adminReviewModerationConstants";
import {
  mapModerationPayload,
  mapReviewModerationResponse,
} from "../../auth/admin/contentModeration/utils/contentModerationAdminMapper.js";
import {
  isAdminUnauthorizedError,
  mapContentModerationApiError,
} from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

async function executeReviewAction(reviewId, action, payload) {
  const body = mapModerationPayload(payload);
  if (action === MODERATION_ACTIONS.HIDE) {
    return hideReview(reviewId, body);
  }
  if (action === MODERATION_ACTIONS.REMOVE) {
    return removeReview(reviewId, body);
  }
  if (action === MODERATION_ACTIONS.RESTORE) {
    return restoreReview(reviewId, body);
  }
  throw { code: "ADMIN-400-VALIDATION", message: "Hanh dong khong hop le." };
}

export function useModerateReview({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const submit = useCallback(
    async (reviewId, { action, reason, note }) => {
      const trimmed = String(reason ?? "").trim();
      if (!trimmed) {
        setSubmitError("Vui lòng nhập lý do kiểm duyệt.");
        return null;
      }

      setIsSubmitting(true);
      setSubmitError("");

      try {
        const raw = await executeReviewAction(reviewId, action, { reason: trimmed, note });
        const result = mapReviewModerationResponse(raw);
        onSuccess?.(result, action);
        return result;
      } catch (error) {
        if (isAdminUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setSubmitError(
          mapContentModerationApiError(error, mapAdminReviewModerationApiError(error)),
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

import { useCallback, useState } from "react";
import { moderateComment } from "../api/contentModerationAdminApi.js";
import { mapCommentModerationResponse, mapModerationPayload } from "../utils/contentModerationAdminMapper.js";
import { isAdminUnauthorizedError, mapContentModerationApiError } from "../utils/mapContentModerationApiError.js";
import { mapSocialModerationApiError } from "../constants/socialModerationConstants.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";

export function useModerateComment({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const submit = useCallback(
    async (commentId, { action, reason, note }) => {
      const trimmed = String(reason ?? "").trim();
      if (!trimmed) {
        setSubmitError("Vui long nhap ly do kiem duyet.");
        return null;
      }

      setIsSubmitting(true);
      setSubmitError("");

      try {
        const raw = await moderateComment(commentId, {
          action,
          ...mapModerationPayload({ reason: trimmed, note }),
        });
        const result = mapCommentModerationResponse(raw);
        onSuccess?.(result, action);
        return result;
      } catch (error) {
        if (isAdminUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setSubmitError(mapContentModerationApiError(error, mapSocialModerationApiError(error)));
        return null;
      } finally {
        setIsSubmitting(false);
      }
    },
    [onSuccess, showSessionExpired],
  );

  const clearError = useCallback(() => setSubmitError(""), []);

  return { isSubmitting, submitError, submit, clearError };
}
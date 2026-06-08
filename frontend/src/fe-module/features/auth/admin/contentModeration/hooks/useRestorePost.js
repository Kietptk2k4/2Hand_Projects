import { useCallback, useState } from "react";
import { restorePost } from "../api/contentModerationAdminApi.js";
import { mapModerationPayload, mapPostRestoreResponse } from "../utils/contentModerationAdminMapper.js";
import { isAdminUnauthorizedError, mapContentModerationApiError } from "../utils/mapContentModerationApiError.js";
import { mapSocialModerationApiError } from "../constants/socialModerationConstants.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";

export function useRestorePost({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const submit = useCallback(
    async (postId, { reason, note }) => {
      const trimmed = String(reason ?? "").trim();
      if (!trimmed) {
        setSubmitError("Vui long nhap ly do khoi phuc.");
        return null;
      }

      setIsSubmitting(true);
      setSubmitError("");

      try {
        const raw = await restorePost(postId, mapModerationPayload({ reason: trimmed, note }));
        const result = mapPostRestoreResponse(raw);
        onSuccess?.(result);
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
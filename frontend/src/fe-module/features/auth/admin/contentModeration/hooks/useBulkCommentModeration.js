import { useCallback, useState } from "react";
import { moderateComment, restoreComment } from "../api/contentModerationAdminApi.js";
import {
  mapCommentModerationResponse,
  mapCommentRestoreResponse,
} from "../utils/contentModerationAdminMapper.js";
import { mapContentModerationApiError } from "../utils/mapContentModerationApiError.js";

export function useBulkCommentModeration() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const execute = useCallback(async ({ commentIds, mode, action, reason, note }) => {
    if (!commentIds?.length) {
      return { succeeded: [], failed: [] };
    }

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const results = await Promise.allSettled(
        commentIds.map(async (commentId) => {
          if (mode === "restore") {
            const data = await restoreComment(commentId, { reason, note });
            return { commentId, result: mapCommentRestoreResponse(data) };
          }
          const data = await moderateComment(commentId, { action, reason, note });
          return { commentId, result: mapCommentModerationResponse(data) };
        }),
      );

      const succeeded = [];
      const failed = [];

      results.forEach((entry, index) => {
        const commentId = commentIds[index];
        if (entry.status === "fulfilled") {
          succeeded.push(entry.value);
        } else {
          failed.push({
            commentId,
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

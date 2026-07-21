import { useCallback, useState } from "react";
import { moderatePost, restorePost } from "../api/contentModerationAdminApi.js";
import { mapPostModerationResponse, mapPostRestoreResponse } from "../utils/contentModerationAdminMapper.js";
import { mapContentModerationApiError } from "../utils/mapContentModerationApiError.js";

export function useBulkPostModeration() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const execute = useCallback(async ({ postIds, mode, action, reason, note }) => {
    if (!postIds?.length) {
      return { succeeded: [], failed: [] };
    }

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const results = await Promise.allSettled(
        postIds.map(async (postId) => {
          if (mode === "restore") {
            const data = await restorePost(postId, { reason, note });
            return { postId, result: mapPostRestoreResponse(data) };
          }
          const data = await moderatePost(postId, { action, reason, note });
          return { postId, result: mapPostModerationResponse(data) };
        }),
      );

      const succeeded = [];
      const failed = [];

      results.forEach((entry, index) => {
        const postId = postIds[index];
        if (entry.status === "fulfilled") {
          succeeded.push(entry.value);
        } else {
          failed.push({
            postId,
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

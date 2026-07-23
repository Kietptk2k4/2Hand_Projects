import { useCallback, useState } from "react";
import {
  hideReview,
  restoreReview,
} from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import {
  mapModerationPayload,
  mapReviewModerationResponse,
} from "../../auth/admin/contentModeration/utils/contentModerationAdminMapper.js";
import { mapContentModerationApiError } from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";

async function executeReviewBulkAction(reviewId, mode, { reason, note }) {
  const body = mapModerationPayload({ reason, note });
  if (mode === "restore") {
    const data = await restoreReview(reviewId, body);
    return { reviewId, result: mapReviewModerationResponse(data) };
  }
  const data = await hideReview(reviewId, body);
  return { reviewId, result: mapReviewModerationResponse(data) };
}

export function useBulkReviewModeration() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const execute = useCallback(async ({ reviewIds, mode, reason, note }) => {
    if (!reviewIds?.length) {
      return { succeeded: [], failed: [] };
    }

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const results = await Promise.allSettled(
        reviewIds.map((reviewId) => executeReviewBulkAction(reviewId, mode, { reason, note })),
      );

      const succeeded = [];
      const failed = [];

      results.forEach((entry, index) => {
        const reviewId = reviewIds[index];
        if (entry.status === "fulfilled") {
          succeeded.push(entry.value);
        } else {
          failed.push({
            reviewId,
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

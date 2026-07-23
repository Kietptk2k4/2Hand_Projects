import { useCallback, useEffect, useState } from "react";
import { fetchReviewModerationHistory } from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import { mapReviewModerationHistoryResponse } from "../../auth/admin/contentModeration/utils/contentModerationAdminMapper.js";
import {
  isAdminUnauthorizedError,
  mapContentModerationApiError,
} from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useReviewModerationHistory(reviewId) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchHistory = useCallback(async () => {
    if (!reviewId) {
      setResult(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchReviewModerationHistory(reviewId, { page: 1, size: 20 });
      setResult(mapReviewModerationHistoryResponse(data));
      setStatus("ready");
    } catch (error) {
      if (isAdminUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(mapContentModerationApiError(error));
      setResult(null);
    }
  }, [reviewId, showSessionExpired]);

  useEffect(() => {
    fetchHistory();
  }, [fetchHistory]);

  return {
    result,
    status,
    errorMessage,
    refetch: fetchHistory,
  };
}

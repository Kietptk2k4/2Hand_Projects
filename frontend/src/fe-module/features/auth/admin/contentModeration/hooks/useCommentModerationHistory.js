import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchCommentModerationHistory } from "../api/contentModerationAdminApi.js";
import { mapCommentModerationHistoryResponse } from "../utils/contentModerationAdminMapper.js";
import { mapContentModerationApiError } from "../utils/mapContentModerationApiError.js";

export function useCommentModerationHistory(commentId) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchHistory = useCallback(async () => {
    if (!commentId) {
      setResult(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchCommentModerationHistory(commentId, { page: 1, size: 20 });
      setResult(mapCommentModerationHistoryResponse(data));
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(mapContentModerationApiError(error));
    }
  }, [commentId, showSessionExpired]);

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

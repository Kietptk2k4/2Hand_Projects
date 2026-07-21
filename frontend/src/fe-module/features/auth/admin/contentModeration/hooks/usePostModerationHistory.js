import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchPostModerationHistory } from "../api/contentModerationAdminApi.js";
import { mapPostModerationHistoryResponse } from "../utils/contentModerationAdminMapper.js";
import { mapContentModerationApiError } from "../utils/mapContentModerationApiError.js";

export function usePostModerationHistory(postId) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchHistory = useCallback(async () => {
    if (!postId) {
      setResult(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchPostModerationHistory(postId, { page: 1, size: 20 });
      setResult(mapPostModerationHistoryResponse(data));
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(mapContentModerationApiError(error));
    }
  }, [postId, showSessionExpired]);

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

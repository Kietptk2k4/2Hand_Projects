import { useCallback, useEffect, useState } from "react";
import { fetchProductModerationHistory } from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import { mapProductModerationHistoryResponse } from "../../auth/admin/contentModeration/utils/contentModerationAdminMapper.js";
import {
  isAdminUnauthorizedError,
  mapContentModerationApiError,
} from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useProductModerationHistory(productId) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchHistory = useCallback(async () => {
    if (!productId) {
      setResult(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchProductModerationHistory(productId, { page: 1, size: 20 });
      setResult(mapProductModerationHistoryResponse(data));
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
  }, [productId, showSessionExpired]);

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

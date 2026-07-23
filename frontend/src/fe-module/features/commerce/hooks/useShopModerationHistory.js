import { useCallback, useEffect, useState } from "react";
import { fetchShopModerationHistory } from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import { mapShopModerationHistoryResponse } from "../utils/shopModerationDetailMapper.js";
import { mapContentModerationApiError } from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";

export function useShopModerationHistory(shopId) {
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const refetch = useCallback(async () => {
    if (!shopId) {
      setResult(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchShopModerationHistory(shopId, { page: 1, size: 20 });
      setResult(mapShopModerationHistoryResponse(data));
      setStatus("ready");
    } catch (error) {
      setResult(null);
      setStatus("error");
      setErrorMessage(mapContentModerationApiError(error));
    }
  }, [shopId]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return {
    result,
    status,
    errorMessage,
    refetch,
  };
}

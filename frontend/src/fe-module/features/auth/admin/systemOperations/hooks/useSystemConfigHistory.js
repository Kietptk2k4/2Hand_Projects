import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchSystemConfigHistory } from "../api/systemConfigApi.js";
import { CONFIG_PAGE_SIZE } from "../constants/systemConfigConstants.js";
import { mapSystemConfigHistoryResponse } from "../utils/systemConfigMapper.js";
import { handleSystemOperationsLoadError } from "../utils/systemOperationsTabErrors.js";

export function useSystemConfigHistory({ configId, page = 1, size = CONFIG_PAGE_SIZE, enabled }) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchHistory = useCallback(async () => {
    if (!enabled || !configId) return;
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await fetchSystemConfigHistory(configId, { page, size });
      setResult(mapSystemConfigHistoryResponse(data));
      setStatus("ready");
    } catch (error) {
      handleSystemOperationsLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        notFoundMessage: "Không tìm thấy lịch sử cấu hình.",
        permissionHint: "SYSTEM_CONFIG_VIEW",
      });
      setResult(null);
    }
  }, [configId, enabled, page, showSessionExpired, size]);

  useEffect(() => {
    fetchHistory();
  }, [fetchHistory]);

  return { result, status, errorMessage, refetch: fetchHistory };
}
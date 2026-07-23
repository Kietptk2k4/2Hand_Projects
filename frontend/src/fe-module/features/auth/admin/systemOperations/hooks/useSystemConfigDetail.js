import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchSystemConfigDetail } from "../api/systemConfigApi.js";
import { mapSystemConfigEntry } from "../utils/systemConfigMapper.js";
import { handleSystemOperationsLoadError } from "../utils/systemOperationsTabErrors.js";

export function useSystemConfigDetail(configId) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!configId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchSystemConfigDetail(configId);
      setDetail(mapSystemConfigEntry(data));
      setStatus("ready");
    } catch (error) {
      handleSystemOperationsLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        notFoundMessage: "Không tìm thấy cấu hình.",
        permissionHint: "SYSTEM_CONFIG_VIEW",
      });
      setDetail(null);
    }
  }, [configId, showSessionExpired]);

  useEffect(() => {
    fetchDetail();
  }, [fetchDetail]);

  return {
    detail,
    status,
    errorMessage,
    refetch: fetchDetail,
  };
}

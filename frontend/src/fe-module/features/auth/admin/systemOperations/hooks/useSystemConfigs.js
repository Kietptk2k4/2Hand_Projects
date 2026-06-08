import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchSystemConfigs } from "../api/systemConfigApi.js";
import { CONFIG_PAGE_SIZE } from "../constants/systemConfigConstants.js";
import { mapSystemConfigsResponse } from "../utils/systemConfigMapper.js";
import { handleSystemOperationsLoadError } from "../utils/systemOperationsTabErrors.js";

function buildQueryParams(filters) {
  const params = {
    page: Number(filters?.page) || 1,
    size: Number(filters?.size) || CONFIG_PAGE_SIZE,
  };
  if (filters?.q) params.q = filters.q;
  if (filters?.value_type) params.value_type = filters.value_type;
  if (filters?.is_active === "true" || filters?.is_active === "false") {
    params.is_active = filters.is_active === "true";
  }
  return params;
}

export function useSystemConfigs({ configFilters, enabled }) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchConfigs = useCallback(async () => {
    if (!enabled) return;
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await fetchSystemConfigs(buildQueryParams(configFilters));
      setResult(mapSystemConfigsResponse(data));
      setStatus("ready");
    } catch (error) {
      handleSystemOperationsLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionHint: "SYSTEM_CONFIG_VIEW",
      });
      setResult(null);
    }
  }, [configFilters, enabled, showSessionExpired]);

  useEffect(() => {
    fetchConfigs();
  }, [fetchConfigs]);

  return { result, status, errorMessage, refetch: fetchConfigs };
}
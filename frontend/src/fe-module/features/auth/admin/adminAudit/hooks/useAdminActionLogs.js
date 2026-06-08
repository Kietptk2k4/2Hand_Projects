import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchAdminActionLogs } from "../api/adminAuditApi.js";
import { AUDIT_PAGE_SIZE } from "../constants/adminAuditConstants.js";
import { mapAdminActionLogsResponse } from "../utils/adminAuditMapper.js";
import { handleAuditLoadError } from "../utils/adminAuditTabErrors.js";

function buildQueryParams(filters) {
  const params = {
    page: Number(filters?.page) || 1,
    size: Number(filters?.size) || AUDIT_PAGE_SIZE,
  };
  if (filters?.admin_id) params.admin_id = filters.admin_id;
  if (filters?.action) params.action = filters.action;
  if (filters?.target_type) params.target_type = filters.target_type;
  if (filters?.target_id) params.target_id = filters.target_id;
  if (filters?.status) params.status = filters.status;
  if (filters?.from) params.from = filters.from;
  if (filters?.to) params.to = filters.to;
  return params;
}

export function useAdminActionLogs({ auditFilters, enabled }) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchLogs = useCallback(async () => {
    if (!enabled) return;
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await fetchAdminActionLogs(buildQueryParams(auditFilters));
      setResult(mapAdminActionLogsResponse(data));
      setStatus("ready");
    } catch (error) {
      handleAuditLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
      });
      setResult(null);
    }
  }, [auditFilters, enabled, showSessionExpired]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  return {
    result,
    status,
    errorMessage,
    refetch: fetchLogs,
  };
}
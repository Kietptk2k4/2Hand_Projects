import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchSystemAnnouncements } from "../api/systemAnnouncementApi.js";
import { ANNOUNCEMENT_PAGE_SIZE } from "../constants/systemAnnouncementConstants.js";
import { mapSystemAnnouncementsResponse } from "../utils/systemAnnouncementMapper.js";
import { handleSystemOperationsLoadError } from "../utils/systemOperationsTabErrors.js";

function buildQueryParams(filters) {
  const params = {
    page: Number(filters?.page) || 1,
    size: Number(filters?.size) || ANNOUNCEMENT_PAGE_SIZE,
  };
  if (filters?.q) params.q = filters.q;
  if (filters?.status) params.status = filters.status;
  if (filters?.severity) params.severity = filters.severity;
  return params;
}

export function useSystemAnnouncements({ announcementFilters, enabled }) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchAnnouncements = useCallback(async () => {
    if (!enabled) return;
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await fetchSystemAnnouncements(buildQueryParams(announcementFilters));
      setResult(mapSystemAnnouncementsResponse(data));
      setStatus("ready");
    } catch (error) {
      handleSystemOperationsLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionHint: "SYSTEM_ANNOUNCEMENT",
      });
      setResult(null);
    }
  }, [announcementFilters, enabled, showSessionExpired]);

  useEffect(() => {
    fetchAnnouncements();
  }, [fetchAnnouncements]);

  return { result, status, errorMessage, refetch: fetchAnnouncements };
}
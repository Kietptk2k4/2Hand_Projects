import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchSystemAnnouncementDetail } from "../api/systemAnnouncementApi.js";
import { mapSystemAnnouncementEntry } from "../utils/systemAnnouncementMapper.js";
import { handleSystemOperationsLoadError } from "../utils/systemOperationsTabErrors.js";

export function useSystemAnnouncementDetail(announcementId) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!announcementId) {
      setDetail(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchSystemAnnouncementDetail(announcementId);
      setDetail(mapSystemAnnouncementEntry(data));
      setStatus("ready");
    } catch (error) {
      handleSystemOperationsLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        notFoundMessage: "Không tìm thấy thông báo.",
        permissionHint: "SYSTEM_ANNOUNCEMENT_CREATE",
      });
      setDetail(null);
    }
  }, [announcementId, showSessionExpired]);

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

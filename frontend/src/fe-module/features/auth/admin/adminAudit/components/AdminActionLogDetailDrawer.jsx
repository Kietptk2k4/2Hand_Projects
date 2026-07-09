import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { buildAdminSearchParams } from "../../adminUrlParams.js";
import { fetchAdminActionLogDetail } from "../api/adminAuditApi.js";
import { mapAdminActionLogEntry } from "../utils/adminAuditMapper.js";
import { handleAuditLoadError } from "../utils/adminAuditTabErrors.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { AdminActionLogDetailDrawerView } from "./AdminActionLogDetailDrawerView.jsx";

export function AdminActionLogDetailDrawer({ logId, onClose, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [, setSearchParams] = useSearchParams();
  const [entry, setEntry] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!logId) return;
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await fetchAdminActionLogDetail(logId);
      setEntry(mapAdminActionLogEntry(data));
      setStatus("ready");
    } catch (error) {
      handleAuditLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        notFoundMessage: "Nhật ký không tồn tại hoặc đã bị xóa.",
      });
      if (String(error?.code ?? "").includes("404")) {
        onNotify?.({
          variant: "error",
          message: error?.message || "Nhật ký không tồn tại.",
        });
        onClose?.();
      }
    }
  }, [logId, onClose, onNotify, showSessionExpired]);

  useEffect(() => {
    if (!logId) {
      setEntry(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [fetchDetail, logId]);

  const showFilterSameTarget = Boolean(entry?.targetType && entry?.targetId);

  const handleFilterSameTarget = () => {
    if (!entry?.targetType || !entry?.targetId) return;
    const targetFilterParams = buildAdminSearchParams({
      section: "adminAudit",
      tab: "action-logs",
      auditFilters: {
        target_type: entry.targetType,
        target_id: entry.targetId,
        page: "1",
        size: "20",
      },
      clearLogId: true,
    });
    setSearchParams(targetFilterParams, { replace: true });
    onClose?.();
  };

  return (
    <AdminActionLogDetailDrawerView
      logId={logId}
      status={status}
      errorMessage={errorMessage}
      entry={entry}
      showFilterSameTarget={showFilterSameTarget}
      onClose={onClose}
      onRetry={fetchDetail}
      onFilterSameTarget={handleFilterSameTarget}
    />
  );
}

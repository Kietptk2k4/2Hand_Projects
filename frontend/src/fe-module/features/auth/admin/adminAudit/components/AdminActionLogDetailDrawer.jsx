import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { buildAdminSearchParams } from "../../adminUrlParams.js";
import { lookupAuditAdminById } from "../api/auditAdminApi.js";
import { fetchAdminActionLogDetail } from "../api/adminAuditApi.js";
import { mapAdminActionLogEntry } from "../utils/adminAuditMapper.js";
import { handleAuditLoadError } from "../utils/adminAuditTabErrors.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { AdminActionLogDetailDrawerView } from "./AdminActionLogDetailDrawerView.jsx";

export function AdminActionLogDetailDrawer({ logId, onClose, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [, setSearchParams] = useSearchParams();
  const [entry, setEntry] = useState(null);
  const [adminSummary, setAdminSummary] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchDetail = useCallback(async () => {
    if (!logId) return;
    setStatus("loading");
    setErrorMessage("");
    setAdminSummary(null);
    try {
      const data = await fetchAdminActionLogDetail(logId);
      const mapped = mapAdminActionLogEntry(data);
      setEntry(mapped);
      setStatus("ready");

      if (mapped?.adminId) {
        lookupAuditAdminById(mapped.adminId)
          .then((admin) => {
            if (admin) setAdminSummary(admin);
          })
          .catch(() => {});
      }
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
      setAdminSummary(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchDetail();
  }, [fetchDetail, logId]);

  const handleFilterSameTarget = () => {
    if (!entry?.targetType || !entry?.targetId) return;
    setSearchParams(
      buildAdminSearchParams({
        section: "adminAudit",
        tab: "action-logs",
        auditFilters: {
          target_type: entry.targetType,
          target_id: entry.targetId,
          page: "1",
          size: "20",
        },
        clearLogId: true,
      }),
      { replace: true },
    );
    onClose?.();
  };

  const handleFilterSameAdmin = () => {
    if (!entry?.adminId) return;
    setSearchParams(
      buildAdminSearchParams({
        section: "adminAudit",
        tab: "action-logs",
        auditFilters: {
          admin_id: entry.adminId,
          page: "1",
          size: "20",
        },
        clearLogId: true,
      }),
      { replace: true },
    );
    onClose?.();
  };

  return (
    <AdminActionLogDetailDrawerView
      logId={logId}
      status={status}
      errorMessage={errorMessage}
      entry={entry}
      adminSummary={adminSummary}
      onClose={onClose}
      onRetry={fetchDetail}
      onFilterSameAdmin={handleFilterSameAdmin}
      onFilterSameTarget={handleFilterSameTarget}
    />
  );
}

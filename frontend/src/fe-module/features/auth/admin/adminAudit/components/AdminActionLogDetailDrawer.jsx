import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AccountSkeleton } from "../../../../../shared/ui/auth/authUi.jsx";
import { buildAdminSearchParams } from "../../adminUrlParams.js";
import { fetchAdminActionLogDetail } from "../api/adminAuditApi.js";
import { mapAdminActionLogEntry } from "../utils/adminAuditMapper.js";
import { handleAuditLoadError } from "../utils/adminAuditTabErrors.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { AuditStatusBadge } from "./AuditStatusBadge.jsx";

function JsonBlock({ label, value }) {
  return (
    <div>
      <h3 className="mb-2 text-sm font-semibold text-on-surface">{label}</h3>
      <pre className="max-h-72 overflow-auto rounded-lg bg-surface-container-low p-3 text-xs text-on-surface">
        {value == null ? "—" : JSON.stringify(value, null, 2)}
      </pre>
    </div>
  );
}

function DetailRow({ label, value, mono = false }) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase tracking-wide text-on-surface-variant">{label}</dt>
      <dd className={`mt-1 text-sm text-on-surface ${mono ? "font-mono break-all" : ""}`}>{value || "—"}</dd>
    </div>
  );
}

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

  if (!logId) return null;

  const targetFilterParams = entry?.targetType && entry?.targetId
    ? buildAdminSearchParams({
        section: "adminAudit",
        tab: "action-logs",
        auditFilters: {
          target_type: entry.targetType,
          target_id: entry.targetId,
          page: "1",
          size: "20",
        },
        clearLogId: true,
      })
    : null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button
        type="button"
        aria-label="Đóng chi tiết nhật ký"
        className="absolute inset-0 bg-black/40"
        onClick={onClose}
      />
      <aside className="relative flex h-full w-full max-w-xl flex-col border-l border-outline-variant bg-surface shadow-xl">
        <div className="flex items-start justify-between border-b border-outline-variant px-6 py-5">
          <div>
            <h2 className="text-lg font-semibold text-on-surface">Chi tiết nhật ký hành động</h2>
            <p className="mt-1 font-mono text-xs text-on-surface-variant">{logId}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg px-3 py-1.5 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
          >
            Đóng
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-6 py-5">
          {status === "loading" ? <AccountSkeleton /> : null}
          {status === "error" ? (
            <div className="space-y-3">
              <p className="text-sm text-error">{errorMessage}</p>
              <button
                type="button"
                onClick={fetchDetail}
                className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
              >
                Thu lại
              </button>
            </div>
          ) : null}
          {status === "ready" && entry ? (
            <div className="space-y-6">
              <div className="grid gap-4 sm:grid-cols-2">
                <DetailRow label="Thời gian" value={formatDateTime(entry.createdAt)} />
                <DetailRow label="Status" value={<AuditStatusBadge status={entry.status} />} />
                <DetailRow label="Admin ID" value={entry.adminId} mono />
                <DetailRow label="Action" value={entry.actionType} />
                <DetailRow label="Target type" value={entry.targetType} />
                <DetailRow label="Target ID" value={entry.targetId} mono />
                <DetailRow label="IP address" value={entry.ipAddress} mono />
                <DetailRow label="User agent" value={entry.userAgent} />
              </div>

              {targetFilterParams ? (
                <button
                  type="button"
                  onClick={() => {
                    setSearchParams(targetFilterParams, { replace: true });
                    onClose?.();
                  }}
                  className="inline-flex text-sm font-medium text-primary hover:underline"
                >
                  Lọc nhật ký cùng target
                </button>
              ) : null}

              <JsonBlock label="Request payload" value={entry.requestPayload} />
              <JsonBlock label="Response payload" value={entry.responsePayload} />
            </div>
          ) : null}
        </div>
      </aside>
    </div>
  );
}
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  getAuditActionLabel,
  getAuditTargetTypeLabel,
} from "../constants/adminAuditActionLabels.js";
import { formatAuditAdminSummary } from "../api/auditAdminApi.js";
import { AdminFilterButton, AdminSurfaceCard } from "../../components/ui";
import { AuditActionTypeBadge } from "./AuditActionTypeBadge.jsx";
import { AuditCopyableId } from "./AuditCopyableId.jsx";
import { AuditDrawerActions } from "./AuditDrawerActions.jsx";
import { AuditJsonBlock } from "./AuditJsonBlock.jsx";
import { AuditStatusBadge } from "./AuditStatusBadge.jsx";

export function DetailRow({ label, value, mono = false, className = "" }) {
  return (
    <div className={className}>
      <dt className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">{label}</dt>
      <dd
        className={[
          "mt-1 text-sm text-admin-text",
          mono ? "break-all font-mono" : "",
        ]
          .filter(Boolean)
          .join(" ")}
      >
        {value ?? "—"}
      </dd>
    </div>
  );
}

export function AdminActionLogDetailDrawerView({
  logId,
  status,
  errorMessage,
  entry,
  adminSummary,
  onClose,
  onRetry,
  onFilterSameAdmin,
  onFilterSameTarget,
}) {
  if (!logId) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex min-h-dvh sm:justify-end"
      role="dialog"
      aria-modal="true"
      aria-labelledby="audit-log-drawer-title"
    >
      <button
        type="button"
        aria-label="Đóng chi tiết nhật ký"
        className="absolute inset-0 bg-admin-text/40 backdrop-blur-sm"
        onClick={onClose}
      />

      <aside className="relative z-10 flex h-full max-h-dvh w-full flex-col bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:max-w-xl sm:border-l sm:border-admin-border">
        <div className="shrink-0 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0 flex-1">
              {status === "ready" && entry ? (
                <>
                  <div className="flex flex-wrap items-center gap-2">
                    <AuditStatusBadge status={entry.status} />
                    <AuditActionTypeBadge actionType={entry.actionType} />
                  </div>
                  <h2
                    id="audit-log-drawer-title"
                    className="mt-2 text-balance text-lg font-semibold tracking-tight text-admin-text"
                  >
                    {getAuditActionLabel(entry.actionType)}
                  </h2>
                  <p className="mt-1 font-mono text-xs text-admin-text-muted">{entry.actionType}</p>
                  <p className="mt-2 text-sm tabular-nums text-admin-text-secondary">
                    {formatDateTime(entry.createdAt)}
                  </p>
                </>
              ) : (
                <h2 id="audit-log-drawer-title" className="text-lg font-semibold text-admin-text">
                  Chi tiết nhật ký hành động
                </h2>
              )}
              <p className="mt-2 break-all font-mono text-[11px] text-admin-text-muted">{logId}</p>
            </div>
            <button
              type="button"
              onClick={onClose}
              className="flex min-h-11 min-w-11 shrink-0 items-center justify-center rounded-lg text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
            >
              Đóng
            </button>
          </div>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain px-4 py-5 sm:px-6">
          {status === "loading" ? (
            <AdminSurfaceCard padding="lg">
              <div className="space-y-3">
                {Array.from({ length: 5 }, (_, index) => (
                  <div key={index} className="h-10 animate-pulse rounded-lg bg-admin-surface-muted" />
                ))}
              </div>
            </AdminSurfaceCard>
          ) : null}

          {status === "error" ? (
            <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
              <p className="text-sm text-admin-danger">{errorMessage}</p>
              <AdminFilterButton type="button" variant="primary" className="mt-4 w-full sm:w-auto" onClick={onRetry}>
                Thử lại
              </AdminFilterButton>
            </AdminSurfaceCard>
          ) : null}

          {status === "ready" && entry ? (
            <div className="space-y-6">
              <AuditDrawerActions
                entry={entry}
                onFilterSameAdmin={onFilterSameAdmin}
                onFilterSameTarget={onFilterSameTarget}
                onClose={onClose}
              />

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <DetailRow
                  label="Admin"
                  value={
                    adminSummary
                      ? formatAuditAdminSummary(adminSummary)
                      : entry.adminId
                  }
                />
                <AuditCopyableId label="Admin ID" value={entry.adminId} />
                <DetailRow
                  label="Loại đối tượng"
                  value={getAuditTargetTypeLabel(entry.targetType)}
                  className="sm:col-span-2 sm:col-auto"
                />
                <AuditCopyableId label="Target ID" value={entry.targetId} />
                <AuditCopyableId label="IP address" value={entry.ipAddress} className="sm:col-span-2" />
                <DetailRow label="User agent" value={entry.userAgent} className="sm:col-span-2" />
              </div>

              <AuditJsonBlock label="Request payload" value={entry.requestPayload} />
              <AuditJsonBlock label="Response payload" value={entry.responsePayload} />
            </div>
          ) : null}
        </div>

        <div className="shrink-0 border-t border-admin-border bg-admin-surface-muted p-4 sm:hidden">
          <AdminFilterButton type="button" variant="secondary" className="w-full" onClick={onClose}>
            Đóng
          </AdminFilterButton>
        </div>
      </aside>
    </div>
  );
}

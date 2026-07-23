import { useMemo } from "react";
import { useAuditAdminSummaries } from "../../adminAudit/hooks/useAuditAdminSummaries.js";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminPagination } from "../../components/ui";
import { SystemOperationsListSkeleton } from "./ui/SystemOperationsListSkeleton.jsx";
import { SystemOperationsRetryPanel } from "./ui/SystemOperationsRetryPanel.jsx";

export function SystemConfigHistoryPanelView({
  status,
  errorMessage,
  valuesMasked,
  history,
  currentPage,
  totalPages,
  totalElements,
  onPageChange,
  onRetry,
}) {
  const adminIds = useMemo(
    () => (history || []).map((entry) => entry.changedBy).filter(Boolean),
    [history],
  );
  const adminSummaries = useAuditAdminSummaries(adminIds);

  if (status === "loading") return <SystemOperationsListSkeleton rows={4} />;

  if (status === "error") {
    return <SystemOperationsRetryPanel message={errorMessage} onRetry={onRetry} />;
  }

  if (!history?.length) {
    return <p className="text-sm text-admin-text-muted">Chưa có lịch sử thay đổi.</p>;
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <p className="text-sm font-medium text-admin-text">Lịch sử thay đổi</p>
        {totalElements ? (
          <span className="text-xs text-admin-text-muted">{totalElements} bản ghi</span>
        ) : null}
      </div>

      {valuesMasked ? (
        <p className="rounded-lg border border-admin-warning/40 bg-admin-warning-soft px-3 py-2 text-xs text-admin-warning">
          Một số giá trị đã được che vì lý do bảo mật.
        </p>
      ) : null}

      {totalPages > 1 ? (
        <AdminPagination
          currentPage={currentPage}
          totalPages={totalPages}
          showPageNumbers
          onPrevious={() => onPageChange?.(Math.max(1, currentPage - 1))}
          onNext={() => onPageChange?.(Math.min(totalPages, currentPage + 1))}
          onGoToPage={onPageChange}
        />
      ) : null}

      <ul className="space-y-3">
        {history.map((entry) => {
          const admin = adminSummaries[entry.changedBy];
          const adminLabel = admin?.displayName || admin?.email || entry.changedBy || "—";

          return (
            <li key={entry.historyId} className="rounded-lg border border-admin-border p-4">
              <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-admin-text-muted">
                <span>{formatDateTime(entry.createdAt)}</span>
                <span>
                  Admin: <span className="font-medium text-admin-text">{adminLabel}</span>
                </span>
              </div>
              <p className="mt-2 text-sm text-admin-text">
                <span className="text-admin-text-muted">Cũ:</span>{" "}
                <code className="break-all text-xs">{entry.oldValue ?? "—"}</code>
              </p>
              <p className="mt-1 text-sm text-admin-text">
                <span className="text-admin-text-muted">Mới:</span>{" "}
                <code className="break-all text-xs">{entry.newValue ?? "—"}</code>
              </p>
              {entry.reason ? (
                <p className="mt-2 text-xs text-admin-text-secondary">Lý do: {entry.reason}</p>
              ) : null}
            </li>
          );
        })}
      </ul>

      {totalPages > 1 ? (
        <AdminPagination
          className="pt-2"
          currentPage={currentPage}
          totalPages={totalPages}
          showPageNumbers
          onPrevious={() => onPageChange?.(Math.max(1, currentPage - 1))}
          onNext={() => onPageChange?.(Math.min(totalPages, currentPage + 1))}
          onGoToPage={onPageChange}
        />
      ) : null}
    </div>
  );
}

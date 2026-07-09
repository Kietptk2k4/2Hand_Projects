import { AdminPageHeader, AdminPagination, AdminSurfaceCard } from "../../../components/ui";
import { AdminActionLogFilterBar } from "../AdminActionLogFilterBar.jsx";
import { AdminActionLogsTable } from "../AdminActionLogsTable.jsx";
import { AuditEmptyState } from "../AuditEmptyState.jsx";
import { AuditForbiddenState } from "../AuditForbiddenState.jsx";

function AuditListSkeleton() {
  return (
    <AdminSurfaceCard padding="lg">
      <div className="space-y-3">
        {Array.from({ length: 6 }, (_, index) => (
          <div key={index} className="h-14 animate-pulse rounded-lg bg-admin-surface-muted" />
        ))}
      </div>
    </AdminSurfaceCard>
  );
}

function AuditRetryPanel({ message, onRetry }) {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
      <p className="text-sm text-admin-danger">{message}</p>
      <button
        type="button"
        onClick={onRetry}
        className="mt-4 inline-flex min-h-11 items-center justify-center rounded-lg bg-admin-accent px-4 py-2 text-sm font-medium text-white hover:bg-admin-accent-strong"
      >
        Thử lại
      </button>
    </AdminSurfaceCard>
  );
}

export function AdminActionLogsTabView({
  title,
  subtitle,
  canViewAudit,
  forbiddenMessage,
  filterKey,
  auditFilters,
  status,
  errorMessage,
  result,
  summary,
  currentPage,
  totalPages,
  logId,
  emptyMessage,
  onApplyFilters,
  onResetFilters,
  onPageChange,
  onSelectLog,
  onRetry,
  drawer,
}) {
  if (!canViewAudit) {
    return (
      <div className="space-y-6">
        <AdminPageHeader title={title} subtitle={subtitle} />
        <AuditForbiddenState message={forbiddenMessage} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <AdminPageHeader title={title} subtitle={subtitle} />

      <AdminSurfaceCard padding="md">
        <AdminActionLogFilterBar
          key={filterKey}
          filters={auditFilters}
          onApply={onApplyFilters}
          onReset={onResetFilters}
        />
      </AdminSurfaceCard>

      {status === "loading" ? <AuditListSkeleton /> : null}
      {status === "forbidden" ? <AuditForbiddenState message={errorMessage} /> : null}
      {status === "error" ? <AuditRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="md">
          <AdminPagination
            className="mb-4"
            currentPage={currentPage}
            totalPages={totalPages}
            summary={summary}
            onPrevious={() => onPageChange(currentPage - 1)}
            onNext={() => onPageChange(currentPage + 1)}
            previousLabel="Trước"
            nextLabel="Sau"
          />

          {result?.logs?.length ? (
            <AdminActionLogsTable
              logs={result.logs}
              selectedLogId={logId}
              onSelectLog={onSelectLog}
            />
          ) : (
            <AuditEmptyState message={emptyMessage} />
          )}
        </AdminSurfaceCard>
      ) : null}

      {drawer}
    </div>
  );
}

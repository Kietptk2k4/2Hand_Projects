import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../../components/ui";
import { AUDIT_PAGE_SIZE_OPTIONS } from "../../constants/adminAuditConstants.js";
import { AdminActionLogFilterBar } from "../AdminActionLogFilterBar.jsx";
import { AdminActionLogsTable } from "../AdminActionLogsTable.jsx";
import { AuditForbiddenState } from "../AuditForbiddenState.jsx";

function AuditListSkeleton() {
  return (
    <div className="space-y-3 p-4 lg:p-5">
      {Array.from({ length: 6 }, (_, index) => (
        <div key={index} className="h-14 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );
}

function AuditRetryPanel({ message, onRetry }) {
  return (
    <div className="p-4 lg:p-5">
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
    </div>
  );
}

function AuditPageSizeSelect({ value, onChange, disabled }) {
  return (
    <label className="inline-flex min-h-11 items-center gap-2 text-sm text-admin-text-secondary">
      <span className="hidden sm:inline">Hiển thị</span>
      <AdminFilterSelect
        value={value}
        disabled={disabled}
        onChange={(event) => onChange?.(event.target.value)}
        className="min-h-11 w-auto min-w-[7.5rem] py-1.5"
        aria-label="Số bản ghi mỗi trang"
      >
        {AUDIT_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
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
  logs,
  adminSummaries,
  summary,
  currentPage,
  totalPages,
  pageSize,
  logId,
  emptyMessage,
  onApplyFilters,
  onResetFilters,
  onQuickFilter,
  onRemoveFilterChip,
  onPageChange,
  onPageSizeChange,
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

      <AdminSurfaceCard padding="none" className="overflow-hidden">
        <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-5">
          <AdminActionLogFilterBar
            key={filterKey}
            filters={auditFilters}
            onApply={onApplyFilters}
            onReset={onResetFilters}
            onQuickFilter={onQuickFilter}
            onRemoveFilterChip={onRemoveFilterChip}
          />
        </div>

        {status === "loading" ? <AuditListSkeleton /> : null}
        {status === "forbidden" ? (
          <div className="p-4 lg:p-5">
            <AuditForbiddenState message={errorMessage} />
          </div>
        ) : null}
        {status === "error" ? <AuditRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

        {status === "ready" ? (
          <div className="p-4 lg:p-5">
            <div className="mb-4 flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
              <AdminPagination
                className="min-w-0 flex-1"
                currentPage={currentPage}
                totalPages={totalPages}
                summary={summary}
                showPageNumbers
                onPrevious={() => onPageChange(currentPage - 1)}
                onNext={() => onPageChange(currentPage + 1)}
                onGoToPage={onPageChange}
                previousLabel="Trước"
                nextLabel="Sau"
              />
              <AuditPageSizeSelect
                value={pageSize}
                onChange={onPageSizeChange}
                disabled={totalPages === 0 && !logs?.length}
              />
            </div>

            {logs?.length ? (
              <AdminActionLogsTable
                logs={logs}
                selectedLogId={logId}
                adminSummaries={adminSummaries}
                onSelectLog={onSelectLog}
              />
            ) : (
              <p className="rounded-lg border border-dashed border-admin-border px-4 py-10 text-center text-sm text-admin-text-secondary">
                {emptyMessage}
              </p>
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}

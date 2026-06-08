import { useMemo } from "react";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import { AUDIT_PAGE_SIZE } from "../../constants/adminAuditConstants.js";
import {
  ADMIN_AUDIT_LOGS_EMPTY,
  ADMIN_AUDIT_LOGS_FORBIDDEN,
  ADMIN_AUDIT_LOGS_SUBTITLE,
  ADMIN_AUDIT_LOGS_TITLE,
} from "../../constants/adminAuditUiStrings.js";
import { useAdminAuditPermissions } from "../../hooks/useAdminAuditPermissions.js";
import { useAdminActionLogs } from "../../hooks/useAdminActionLogs.js";
import { AdminActionLogDetailDrawer } from "../AdminActionLogDetailDrawer.jsx";
import { AdminActionLogFilterBar } from "../AdminActionLogFilterBar.jsx";
import { AdminActionLogsTable } from "../AdminActionLogsTable.jsx";
import { AuditEmptyState } from "../AuditEmptyState.jsx";
import { AuditForbiddenState } from "../AuditForbiddenState.jsx";

export function AdminActionLogsTab({
  logId,
  auditFilters,
  onFiltersChange,
  onLogIdChange,
  onNotify,
}) {
  const { canViewAudit } = useAdminAuditPermissions();
  const { result, status, errorMessage, refetch } = useAdminActionLogs({
    auditFilters,
    enabled: canViewAudit,
  });

  const currentPage = Number(auditFilters?.page) || 1;
  const totalPages = result?.totalPages || 0;

  const handleApplyFilters = (nextFilters) => {
    onFiltersChange?.(nextFilters);
  };

  const handleResetFilters = () => {
    onFiltersChange?.({
      admin_id: "",
      action: "",
      target_type: "",
      target_id: "",
      status: "",
      from: "",
      to: "",
      page: "1",
      size: String(AUDIT_PAGE_SIZE),
    });
  };

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...auditFilters,
      page: String(nextPage),
      size: auditFilters?.size || String(AUDIT_PAGE_SIZE),
    });
  };

  const summary = useMemo(() => {
    if (!result) return "";
    return `${result.totalElements} ban ghi · trang ${result.page}/${Math.max(result.totalPages, 1)}`;
  }, [result]);

  if (!canViewAudit) {
    return (
      <div className="space-y-6">
        <TabPanelHeader title={ADMIN_AUDIT_LOGS_TITLE} subtitle={ADMIN_AUDIT_LOGS_SUBTITLE} />
        <AuditForbiddenState message={ADMIN_AUDIT_LOGS_FORBIDDEN} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <TabPanelHeader title={ADMIN_AUDIT_LOGS_TITLE} subtitle={ADMIN_AUDIT_LOGS_SUBTITLE} />

      <AccountCard>
        <AdminActionLogFilterBar
          key={[
            auditFilters?.admin_id,
            auditFilters?.action,
            auditFilters?.target_type,
            auditFilters?.target_id,
            auditFilters?.status,
            auditFilters?.from,
            auditFilters?.to,
          ].join("|")}
          filters={auditFilters}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
        />
      </AccountCard>

      {status === "loading" ? <AccountSkeleton /> : null}

      {status === "forbidden" ? <AuditForbiddenState message={errorMessage} /> : null}

      {status === "error" ? (
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={refetch}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
          >
            Thu lai
          </button>
        </AccountCard>
      ) : null}

      {status === "ready" ? (
        <AccountCard>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-on-surface-variant">{summary}</p>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={currentPage <= 1}
                onClick={() => handlePageChange(currentPage - 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Truoc
              </button>
              <button
                type="button"
                disabled={currentPage >= totalPages}
                onClick={() => handlePageChange(currentPage + 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Sau
              </button>
            </div>
          </div>

          {result?.logs?.length ? (
            <AdminActionLogsTable
              logs={result.logs}
              selectedLogId={logId}
              onSelectLog={onLogIdChange}
            />
          ) : (
            <AuditEmptyState message={ADMIN_AUDIT_LOGS_EMPTY} />
          )}
        </AccountCard>
      ) : null}

      <AdminActionLogDetailDrawer
        logId={logId}
        onClose={() => onLogIdChange?.(null)}
        onNotify={onNotify}
      />
    </div>
  );
}
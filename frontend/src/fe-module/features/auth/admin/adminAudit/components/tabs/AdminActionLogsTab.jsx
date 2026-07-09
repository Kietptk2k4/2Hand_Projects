import { useMemo } from "react";
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
import { AdminActionLogsTabView } from "./AdminActionLogsTabView.jsx";

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
    return `${result.totalElements} bản ghi · trang ${result.page}/${Math.max(result.totalPages, 1)}`;
  }, [result]);

  const filterKey = [
    auditFilters?.admin_id,
    auditFilters?.action,
    auditFilters?.target_type,
    auditFilters?.target_id,
    auditFilters?.status,
    auditFilters?.from,
    auditFilters?.to,
  ].join("|");

  return (
    <AdminActionLogsTabView
      title={ADMIN_AUDIT_LOGS_TITLE}
      subtitle={ADMIN_AUDIT_LOGS_SUBTITLE}
      canViewAudit={canViewAudit}
      forbiddenMessage={ADMIN_AUDIT_LOGS_FORBIDDEN}
      filterKey={filterKey}
      auditFilters={auditFilters}
      status={status}
      errorMessage={errorMessage}
      result={result}
      summary={summary}
      currentPage={currentPage}
      totalPages={totalPages}
      logId={logId}
      emptyMessage={ADMIN_AUDIT_LOGS_EMPTY}
      onApplyFilters={handleApplyFilters}
      onResetFilters={handleResetFilters}
      onPageChange={handlePageChange}
      onSelectLog={onLogIdChange}
      onRetry={refetch}
      drawer={
        <AdminActionLogDetailDrawer
          logId={logId}
          onClose={() => onLogIdChange?.(null)}
          onNotify={onNotify}
        />
      }
    />
  );
}

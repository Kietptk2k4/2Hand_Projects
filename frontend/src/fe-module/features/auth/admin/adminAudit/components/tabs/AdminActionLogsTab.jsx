import { useMemo } from "react";
import { AUDIT_PAGE_SIZE } from "../../constants/adminAuditConstants.js";
import {
  ADMIN_AUDIT_LOGS_EMPTY,
  ADMIN_AUDIT_LOGS_EMPTY_CRITICAL,
  ADMIN_AUDIT_LOGS_FORBIDDEN,
  ADMIN_AUDIT_LOGS_SUBTITLE,
  ADMIN_AUDIT_LOGS_TITLE,
} from "../../constants/adminAuditUiStrings.js";
import { useAdminAuditPermissions } from "../../hooks/useAdminAuditPermissions.js";
import { useAdminActionLogs } from "../../hooks/useAdminActionLogs.js";
import {
  buildAuditQuickDateRange,
  filterAuditLogsForDisplay,
  isAuditQuickPresetActive,
  removeAuditFilterChip,
} from "../../utils/auditFilterHelpers.js";
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
  const pageSize = auditFilters?.size || String(AUDIT_PAGE_SIZE);

  const displayLogs = useMemo(
    () => filterAuditLogsForDisplay(result?.logs, auditFilters),
    [auditFilters, result?.logs],
  );

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
      critical_only: "",
      page: "1",
      size: String(AUDIT_PAGE_SIZE),
    });
  };

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...auditFilters,
      page: String(nextPage),
      size: pageSize,
    });
  };

  const handlePageSizeChange = (nextSize) => {
    onFiltersChange?.({
      ...auditFilters,
      page: "1",
      size: nextSize,
    });
  };

  const handleQuickFilter = (presetId) => {
    if (isAuditQuickPresetActive(auditFilters, presetId)) {
      if (presetId === "critical") {
        onFiltersChange?.({ ...auditFilters, critical_only: "", page: "1" });
        return;
      }
      if (presetId === "failure") {
        onFiltersChange?.({ ...auditFilters, status: "", page: "1" });
        return;
      }
      onFiltersChange?.({ ...auditFilters, from: "", to: "", page: "1" });
      return;
    }

    if (presetId === "critical") {
      onFiltersChange?.({
        ...auditFilters,
        critical_only: "true",
        page: "1",
      });
      return;
    }

    if (presetId === "failure") {
      onFiltersChange?.({
        ...auditFilters,
        status: "FAILURE",
        critical_only: "",
        from: "",
        to: "",
        page: "1",
      });
      return;
    }

    const range = buildAuditQuickDateRange(presetId);
    onFiltersChange?.({
      ...auditFilters,
      ...range,
      critical_only: "",
      page: "1",
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    onFiltersChange?.(removeAuditFilterChip(auditFilters, chipKey));
  };

  const summary = useMemo(() => {
    if (!result) return "";
    const pages = Math.max(result.totalPages, 1);
    const base = `${result.totalElements} bản ghi · trang ${result.page}/${pages}`;
    if (auditFilters?.critical_only === "true") {
      return `${displayLogs.length} quan trọng trên trang · ${base}`;
    }
    return base;
  }, [auditFilters?.critical_only, displayLogs.length, result]);

  const emptyMessage =
    auditFilters?.critical_only === "true" && result?.logs?.length
      ? ADMIN_AUDIT_LOGS_EMPTY_CRITICAL
      : ADMIN_AUDIT_LOGS_EMPTY;

  const filterKey = [
    auditFilters?.admin_id,
    auditFilters?.action,
    auditFilters?.target_type,
    auditFilters?.target_id,
    auditFilters?.status,
    auditFilters?.from,
    auditFilters?.to,
    auditFilters?.critical_only,
    auditFilters?.size,
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
      logs={displayLogs}
      summary={summary}
      currentPage={currentPage}
      totalPages={totalPages}
      pageSize={pageSize}
      logId={logId}
      emptyMessage={emptyMessage}
      onApplyFilters={handleApplyFilters}
      onResetFilters={handleResetFilters}
      onQuickFilter={handleQuickFilter}
      onRemoveFilterChip={handleRemoveFilterChip}
      onPageChange={handlePageChange}
      onPageSizeChange={handlePageSizeChange}
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

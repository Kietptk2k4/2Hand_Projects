import {
  AdminFilterButton,
  AdminListPageShell,
  AdminMetricCard,
  AdminPageRefreshButton,
} from "../../components/ui";
import { SYSTEM_ROLE_CODES } from "../utils/roleListUtils.js";
import { RoleListFilterBar } from "./RoleListFilterBar.jsx";
import { RoleListTable } from "./RoleListTable.jsx";
import { RoleListTableSkeleton } from "./ui/RoleListTableSkeleton.jsx";

function RoleListFooter({ totalCount, lastUpdatedLabel, onTabChange }) {
  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center sm:justify-between">
      <div className="space-y-1">
        <p>{totalCount} vai trò</p>
        {lastUpdatedLabel ? (
          <p className="text-xs text-admin-text-muted">
            Cập nhật lần cuối lúc{" "}
            <span className="font-mono tabular-nums text-admin-text-secondary">{lastUpdatedLabel}</span>
          </p>
        ) : null}
      </div>
      <div className="flex flex-wrap gap-3">
        <button
          type="button"
          onClick={() => onTabChange?.("assign")}
          className="text-admin-accent underline-offset-2 transition-colors hover:text-admin-accent-strong hover:underline"
        >
          Gán vai trò
        </button>
        <button
          type="button"
          onClick={() => onTabChange?.("revoke")}
          className="text-admin-accent underline-offset-2 transition-colors hover:text-admin-accent-strong hover:underline"
        >
          Thu hồi vai trò
        </button>
      </div>
    </div>
  );
}

export function RoleListTabView({
  eyebrow,
  title,
  subtitle,
  status,
  errorMessage,
  errorCode,
  forbiddenMessage,
  forbiddenAction,
  emptyMessage,
  emptyHint,
  roles,
  totalCount,
  systemRoleCount,
  showCreatedColumn,
  lastUpdatedLabel,
  query,
  sort,
  resultSummary,
  onQueryChange,
  onSortChange,
  onViewRolePermissions,
  onEditRole,
  onDeleteRole,
  onCreateRole,
  canManageRoles = true,
  onTabChange,
  onRetry,
  isRefreshing,
}) {
  const systemRoleHint = SYSTEM_ROLE_CODES.join(" · ");
  const filteredHint =
    query.trim() && roles.length !== totalCount
      ? `Đang hiển thị ${roles.length} / ${totalCount} vai trò`
      : systemRoleHint;

  return (
    <AdminListPageShell
      eyebrow={eyebrow}
      title={title}
      subtitle={subtitle}
      headerActions={
        <div className="flex flex-wrap items-center gap-2">
          {canManageRoles && status !== "loading" && status !== "error" ? (
            <AdminFilterButton type="button" variant="primary" onClick={onCreateRole}>
              <span className="material-symbols-outlined text-base" aria-hidden="true">
                add
              </span>
              Tạo vai trò
            </AdminFilterButton>
          ) : null}
          <AdminPageRefreshButton onClick={onRetry} disabled={isRefreshing} />
        </div>
      }
      metrics={
        totalCount > 0 && (status === "ready" || status === "empty") ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:max-w-2xl">
            <AdminMetricCard label="Tổng vai trò" value={String(totalCount)} />
            <AdminMetricCard
              label="Vai trò hệ thống"
              value={String(systemRoleCount)}
              hint={filteredHint}
            />
          </div>
        ) : null
      }
      toolbar={
        status !== "loading" && status !== "forbidden" && status !== "error" ? (
          <RoleListFilterBar
            query={query}
            sort={sort}
            onQueryChange={onQueryChange}
            onSortChange={onSortChange}
            resultSummary={resultSummary}
          />
        ) : null
      }
      status={status}
      forbiddenMessage={forbiddenMessage}
      forbiddenAction={forbiddenAction}
      errorMessage={errorMessage}
      errorCode={errorCode}
      emptyMessage={emptyMessage}
      emptyHint={emptyHint}
      emptyIcon="admin_panel_settings"
      onRetry={onRetry}
      skeleton={<RoleListTableSkeleton rows={6} showCreatedColumn={showCreatedColumn} />}
      footer={
        totalCount > 0 ? (
          <RoleListFooter
            totalCount={totalCount}
            lastUpdatedLabel={lastUpdatedLabel}
            onTabChange={onTabChange}
          />
        ) : null
      }
    >
      <RoleListTable
        roles={roles}
        showCreatedColumn={showCreatedColumn}
        onViewRolePermissions={onViewRolePermissions}
        onEditRole={onEditRole}
        onDeleteRole={onDeleteRole}
        canManageRoles={canManageRoles}
      />
    </AdminListPageShell>
  );
}

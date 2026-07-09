import { AdminFilterButton, AdminPageHeader, AdminPagination, AdminSurfaceCard } from "../../../components/ui";
import { SystemConfigFilterBar } from "../SystemConfigFilterBar.jsx";
import { SystemConfigTable } from "../SystemConfigTable.jsx";
import { SystemOperationsEmptyState } from "../SystemOperationsEmptyState.jsx";
import { SystemOperationsForbiddenState } from "../SystemOperationsForbiddenState.jsx";
import { SystemOperationsListSkeleton } from "../ui/SystemOperationsListSkeleton.jsx";
import { SystemOperationsRetryPanel } from "../ui/SystemOperationsRetryPanel.jsx";

export function SystemConfigsTabView({
  title,
  subtitle,
  canViewConfigs,
  forbiddenMessage,
  filterKey,
  configFilters,
  canUpdateConfigs,
  status,
  errorMessage,
  summary,
  currentPage,
  totalPages,
  items,
  selectedConfigId,
  emptyMessage,
  createModal,
  editDrawer,
  onApplyFilters,
  onResetFilters,
  onPageChange,
  onCreateClick,
  onSelectConfig,
  onOpenHistory,
  onRetry,
}) {
  if (!canViewConfigs) {
    return (
      <div className="space-y-4">
        <AdminPageHeader title={title} subtitle={subtitle} />
        <SystemOperationsForbiddenState message={forbiddenMessage} />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <AdminPageHeader title={title} subtitle={subtitle} />

      <AdminSurfaceCard padding="md">
        <SystemConfigFilterBar
          key={filterKey}
          filters={configFilters}
          onApply={onApplyFilters}
          onReset={onResetFilters}
        />
      </AdminSurfaceCard>

      {canUpdateConfigs ? (
        <div className="flex justify-stretch sm:justify-end">
          <AdminFilterButton
            type="button"
            variant="primary"
            className="w-full min-h-11 sm:w-auto"
            onClick={onCreateClick}
          >
            Tạo cấu hình
          </AdminFilterButton>
        </div>
      ) : null}

      {status === "loading" ? <SystemOperationsListSkeleton rows={5} /> : null}
      {status === "forbidden" ? <SystemOperationsForbiddenState message={errorMessage} /> : null}
      {status === "error" ? <SystemOperationsRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="md">
          <AdminPagination
            className="mb-4"
            currentPage={currentPage}
            totalPages={totalPages}
            summary={summary}
            onPrevious={() => onPageChange(currentPage - 1)}
            onNext={() => onPageChange(currentPage + 1)}
          />
          {items?.length ? (
            <SystemConfigTable
              items={items}
              selectedConfigId={selectedConfigId}
              canUpdate={canUpdateConfigs}
              onSelectConfig={onSelectConfig}
              onOpenHistory={onOpenHistory}
            />
          ) : (
            <SystemOperationsEmptyState message={emptyMessage} />
          )}
        </AdminSurfaceCard>
      ) : null}

      {createModal}
      {editDrawer}
    </div>
  );
}

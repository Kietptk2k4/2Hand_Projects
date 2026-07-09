import { AdminFilterButton, AdminPageHeader, AdminPagination, AdminSurfaceCard } from "../../../components/ui";
import { SystemAnnouncementFilterBar } from "../SystemAnnouncementFilterBar.jsx";
import { SystemAnnouncementStatsCards } from "../SystemAnnouncementStatsCards.jsx";
import { SystemAnnouncementTable } from "../SystemAnnouncementTable.jsx";
import { SystemOperationsEmptyState } from "../SystemOperationsEmptyState.jsx";
import { SystemOperationsForbiddenState } from "../SystemOperationsForbiddenState.jsx";
import { SystemOperationsListSkeleton } from "../ui/SystemOperationsListSkeleton.jsx";
import { SystemOperationsRetryPanel } from "../ui/SystemOperationsRetryPanel.jsx";

export function SystemAnnouncementsTabView({
  title,
  subtitle,
  canViewAnnouncements,
  forbiddenMessage,
  filterKey,
  announcementFilters,
  canCreateAnnouncements,
  stats,
  totalElements,
  status,
  errorMessage,
  summary,
  currentPage,
  totalPages,
  items,
  emptyMessage,
  createDrawer,
  confirmDialog,
  onApplyFilters,
  onResetFilters,
  onPageChange,
  onCreateClick,
  onActionRequest,
  onRetry,
}) {
  if (!canViewAnnouncements) {
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

      <SystemAnnouncementStatsCards stats={stats} totalElements={totalElements} />

      <AdminSurfaceCard padding="md">
        <SystemAnnouncementFilterBar
          key={filterKey}
          filters={announcementFilters}
          onApply={onApplyFilters}
          onReset={onResetFilters}
        />
      </AdminSurfaceCard>

      {canCreateAnnouncements ? (
        <div className="flex justify-stretch sm:justify-end">
          <AdminFilterButton
            type="button"
            variant="primary"
            className="w-full min-h-11 sm:w-auto"
            onClick={onCreateClick}
          >
            Tạo thông báo
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
            <SystemAnnouncementTable items={items} onActionRequest={onActionRequest} />
          ) : (
            <SystemOperationsEmptyState message={emptyMessage} />
          )}
        </AdminSurfaceCard>
      ) : null}

      {createDrawer}
      {confirmDialog}
    </div>
  );
}

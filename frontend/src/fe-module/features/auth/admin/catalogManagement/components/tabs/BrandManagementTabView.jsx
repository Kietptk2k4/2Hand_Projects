import { AdminPageHeader, AdminPagination, AdminSurfaceCard } from "../../../components/ui";
import { BrandDetailDrawer } from "../BrandDetailDrawer.jsx";
import { BrandDeactivateDialog } from "../BrandDeactivateDialog.jsx";
import { BrandHeroStrip } from "../BrandHeroStrip.jsx";
import { BrandTable } from "../BrandTable.jsx";
import { BrandToolbar } from "../BrandToolbar.jsx";
import { CatalogForbiddenState } from "../CatalogForbiddenState.jsx";
import {
  buildBrandPaginationSummary,
  getBrandEmptyMessage,
} from "../../utils/brandHelpers.js";

function CatalogListSkeleton() {
  return (
    <AdminSurfaceCard padding="lg">
      <div className="space-y-3">
        {Array.from({ length: 6 }, (_, index) => (
          <div key={index} className="h-12 animate-pulse rounded-lg bg-admin-surface-muted" />
        ))}
      </div>
    </AdminSurfaceCard>
  );
}

function CatalogRetryPanel({ message, onRetry }) {
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

export function BrandManagementTabView({
  title,
  subtitle,
  canRead,
  canWrite,
  filters,
  pagination,
  loadStatus,
  errorMessage,
  heroMetrics,
  items,
  actionId,
  detailItem,
  deactivateItem,
  deactivatePending,
  modal,
  onQueryChange,
  onStatusChange,
  onPageChange,
  onKpiStatusClick,
  onRefresh,
  onAdd,
  onEdit,
  onDeactivateRequest,
  onDeactivateConfirm,
  onDeactivateClose,
  onActivate,
  onOpenDetail,
  onCloseDetail,
  onRetry,
}) {
  const isLoading = loadStatus === "loading" || loadStatus === "idle";
  const emptyMessage = getBrandEmptyMessage(filters.status, filters.q);
  const actionPending = Boolean(actionId) || deactivatePending;

  if (!canRead) {
    return (
      <div className="mb-6 space-y-4">
        <AdminPageHeader eyebrow="Quản lý catalog" title={title} subtitle={subtitle} />
        <CatalogForbiddenState />
      </div>
    );
  }

  if (loadStatus === "error") {
    return (
      <div className="mb-6 space-y-4">
        <AdminPageHeader eyebrow="Quản lý catalog" title={title} subtitle={subtitle} />
        <CatalogRetryPanel message={errorMessage} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className="mb-6 space-y-6">
      <AdminPageHeader eyebrow="Quản lý catalog" title={title} subtitle={subtitle} />

      <BrandHeroStrip
        metrics={heroMetrics}
        activeStatusFilter={filters.status}
        isLoading={isLoading}
        onStatusClick={onKpiStatusClick}
      />

      <BrandToolbar
        query={filters.q}
        statusFilter={filters.status}
        canWrite={canWrite}
        isLoading={isLoading}
        onQueryChange={onQueryChange}
        onStatusChange={onStatusChange}
        onRefresh={onRefresh}
        onAdd={onAdd}
      />

      {isLoading ? <CatalogListSkeleton /> : null}

      {loadStatus === "forbidden" ? <CatalogForbiddenState message={errorMessage} /> : null}

      {loadStatus === "ready" ? (
        <AdminSurfaceCard padding="md">
          <BrandTable
            items={items}
            canWrite={canWrite}
            actionId={actionId}
            emptyMessage={emptyMessage}
            onEdit={onEdit}
            onDeactivate={onDeactivateRequest}
            onActivate={onActivate}
            onOpenDetail={onOpenDetail}
          />
          {items?.length ? (
            <AdminPagination
              className="mt-4"
              currentPage={pagination.page}
              totalPages={pagination.totalPages}
              summary={buildBrandPaginationSummary({
                page: pagination.page,
                limit: pagination.limit,
                totalItems: pagination.totalItems,
              })}
              onPrevious={() => onPageChange?.(pagination.page - 1)}
              onNext={() => onPageChange?.(pagination.page + 1)}
              disabled={actionPending}
            />
          ) : null}
        </AdminSurfaceCard>
      ) : null}

      {modal}

      <BrandDetailDrawer
        open={Boolean(detailItem)}
        item={detailItem}
        canWrite={canWrite}
        actionId={actionId}
        onClose={onCloseDetail}
        onEdit={onEdit}
        onDeactivate={onDeactivateRequest}
        onActivate={onActivate}
      />

      <BrandDeactivateDialog
        item={deactivateItem}
        pending={deactivatePending}
        onConfirm={onDeactivateConfirm}
        onClose={onDeactivateClose}
      />
    </div>
  );
}

import { AdminPageHeader, AdminSurfaceCard } from "../../../components/ui";
import { CategoryDetailDrawer } from "../CategoryDetailDrawer.jsx";
import { CategoryDeactivateDialog } from "../CategoryDeactivateDialog.jsx";
import { CategoryHeroStrip } from "../CategoryHeroStrip.jsx";
import { CategoryToolbar } from "../CategoryToolbar.jsx";
import { CategoryTreeTable } from "../CategoryTreeTable.jsx";
import { CatalogForbiddenState } from "../CatalogForbiddenState.jsx";
import { getCategoryEmptyMessage } from "../../utils/categoryHelpers.js";

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

export function CategoryManagementTabView({
  title,
  subtitle,
  canRead,
  canWrite,
  filters,
  loadStatus,
  errorMessage,
  heroMetrics,
  tree,
  categoryIndex,
  allItems,
  expandedIds,
  actionId,
  detailItem,
  deactivateItem,
  deactivatePending,
  modal,
  onQueryChange,
  onStatusChange,
  onKpiStatusClick,
  onRefresh,
  onAdd,
  onEdit,
  onDeactivateRequest,
  onDeactivateConfirm,
  onDeactivateClose,
  onActivate,
  onToggleExpand,
  onOpenDetail,
  onCloseDetail,
  onRetry,
}) {
  const isLoading = loadStatus === "loading" || loadStatus === "idle";
  const emptyMessage = getCategoryEmptyMessage(filters.status, filters.q);

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

      <CategoryHeroStrip
        metrics={heroMetrics}
        activeStatusFilter={filters.status}
        isLoading={isLoading}
        onStatusClick={onKpiStatusClick}
      />

      <CategoryToolbar
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
          <CategoryTreeTable
            tree={tree}
            categoryIndex={categoryIndex}
            expandedIds={expandedIds}
            canWrite={canWrite}
            actionId={actionId}
            emptyMessage={emptyMessage}
            onToggleExpand={onToggleExpand}
            onEdit={onEdit}
            onDeactivate={onDeactivateRequest}
            onActivate={onActivate}
            onOpenDetail={onOpenDetail}
          />
        </AdminSurfaceCard>
      ) : null}

      {modal}

      <CategoryDetailDrawer
        open={Boolean(detailItem)}
        item={detailItem}
        categoryIndex={categoryIndex}
        allItems={allItems}
        canWrite={canWrite}
        actionId={actionId}
        onClose={onCloseDetail}
        onEdit={onEdit}
        onDeactivate={onDeactivateRequest}
        onActivate={onActivate}
      />

      <CategoryDeactivateDialog
        item={deactivateItem}
        allItems={allItems}
        pending={deactivatePending}
        onConfirm={onDeactivateConfirm}
        onClose={onDeactivateClose}
      />
    </div>
  );
}

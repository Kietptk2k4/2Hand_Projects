import { AdminPageHeader, AdminSurfaceCard } from "../../../components/ui";
import { BrandTable } from "../BrandTable.jsx";
import { CatalogFilterBar } from "../CatalogFilterBar.jsx";
import { CatalogForbiddenState } from "../CatalogForbiddenState.jsx";

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
  query,
  activeFilter,
  loadStatus,
  errorMessage,
  items,
  actionId,
  emptyMessage,
  modal,
  onQueryChange,
  onActiveFilterChange,
  onFilter,
  onAdd,
  onEdit,
  onDeactivate,
  onActivate,
  onRetry,
}) {
  if (!canRead) {
    return (
      <div className="space-y-4">
        <AdminPageHeader title={title} subtitle={subtitle} />
        <CatalogForbiddenState />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <AdminPageHeader title={title} subtitle={subtitle} />

      <CatalogFilterBar
        query={query}
        activeFilter={activeFilter}
        canWrite={canWrite}
        addLabel="Thêm thương hiệu"
        onQueryChange={onQueryChange}
        onActiveFilterChange={onActiveFilterChange}
        onFilter={onFilter}
        onAdd={onAdd}
      />

      {loadStatus === "loading" ? <CatalogListSkeleton /> : null}
      {loadStatus === "forbidden" ? <CatalogForbiddenState message={errorMessage} /> : null}
      {loadStatus === "error" ? <CatalogRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

      {loadStatus === "ready" ? (
        <AdminSurfaceCard padding="md">
          <BrandTable
            items={items}
            canWrite={canWrite}
            actionId={actionId}
            emptyMessage={emptyMessage}
            onEdit={onEdit}
            onDeactivate={onDeactivate}
            onActivate={onActivate}
          />
        </AdminSurfaceCard>
      ) : null}

      {modal}
    </div>
  );
}

import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminSurfaceCard,
} from "../../components/ui";
import { CategoryStatusChips } from "./CategoryStatusChips.jsx";

export function BrandToolbar({
  query,
  statusFilter,
  canWrite,
  isLoading,
  onQueryChange,
  onStatusChange,
  onRefresh,
  onAdd,
}) {
  return (
    <AdminSurfaceCard padding="md">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div className="min-w-0 flex-1 space-y-3">
          <AdminFilterField label="Tìm kiếm" htmlFor="brand-search">
            <AdminFilterInput
              id="brand-search"
              value={query}
              onChange={(event) => onQueryChange?.(event.target.value)}
              placeholder="Tên hoặc slug"
            />
          </AdminFilterField>
          <CategoryStatusChips
            statusFilter={statusFilter}
            onStatusChange={onStatusChange}
            disabled={isLoading}
          />
        </div>
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
          <button
            type="button"
            onClick={onRefresh}
            disabled={isLoading}
            className="inline-flex min-h-10 items-center justify-center gap-2 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-60"
          >
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              refresh
            </span>
            Làm mới
          </button>
          {canWrite ? (
            <AdminFilterButton type="button" variant="primary" onClick={onAdd}>
              Thêm thương hiệu
            </AdminFilterButton>
          ) : null}
        </div>
      </div>
    </AdminSurfaceCard>
  );
}

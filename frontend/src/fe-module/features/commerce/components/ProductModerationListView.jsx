import {
  PRODUCT_MODERATION_LIST_SORT_OPTIONS,
  PRODUCT_MODERATION_PAGE_SIZE_OPTIONS,
} from "../constants/productModerationListConstants.js";
import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../auth/admin/components/ui";
import { ErrorState } from "../../../shared/ui/PageState.jsx";
import { AdminCommerceAccessDenied } from "./AdminCommerceAccessDenied";
import { ProductModerationBulkActionBar } from "./ProductModerationBulkActionBar.jsx";
import { ProductModerationFilterBar } from "./ProductModerationFilterBar.jsx";
import { ProductModerationStatsBar } from "./ProductModerationStatsBar.jsx";
import { ProductModerationTable } from "./ProductModerationTable.jsx";

function sortColumnLabel(sortField) {
  const option = PRODUCT_MODERATION_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Mới nhất";
}

function ListSkeleton() {
  return (
    <div className="space-y-3 p-4 lg:p-5">
      {Array.from({ length: 6 }, (_, index) => (
        <div key={index} className="h-14 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );
}

function PageSizeSelect({ value, onChange, disabled }) {
  return (
    <label className="inline-flex min-h-11 items-center gap-2 text-sm text-admin-text-secondary">
      <span className="hidden sm:inline">Hiển thị</span>
      <AdminFilterSelect
        value={value}
        disabled={disabled}
        onChange={(event) => onChange?.(event.target.value)}
        className="min-h-11 w-auto min-w-[7.5rem] py-1.5"
        aria-label="Số bản ghi mỗi trang"
      >
        {PRODUCT_MODERATION_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

export function ProductModerationListView({
  status,
  errorMessage,
  appliedFilters,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onClearFilters,
  onQuickFilter,
  onRemoveFilterChip,
  onRetry,
  stats,
  statsStatus,
  onStatPresetClick,
  items,
  pagination,
  currentPage,
  totalPages,
  pageSize,
  activeSort,
  selectedProductId,
  selectedProductIds,
  selectionEnabled,
  canRemoveProduct,
  canRestoreProduct,
  bulkSubmitting,
  onToggleProduct,
  onToggleAll,
  onOpenBulkRemove,
  onOpenBulkRestore,
  onClearBulkSelection,
  onPageChange,
  onPageSizeChange,
  onRowSelect,
  drawer,
}) {
  const summary =
    status === "ready"
      ? `${pagination?.total_items ?? pagination?.totalItems ?? 0} sản phẩm · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${pagination?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Kiểm duyệt nội dung"
        title="Kiểm duyệt sản phẩm"
        subtitle="Lọc danh sách, mở drawer để xem ngữ cảnh sản phẩm và gỡ hoặc khôi phục listing."
      />

      <ProductModerationStatsBar stats={stats} status={statsStatus} onPresetClick={onStatPresetClick} />

      <AdminSurfaceCard padding="none" className="overflow-hidden">
        <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-5">
          <ProductModerationFilterBar
            appliedFilters={appliedFilters}
            draftFilters={draftFilters}
            onDraftFiltersChange={onDraftFiltersChange}
            onApply={onApplyFilters}
            onClear={onClearFilters}
            onQuickFilter={onQuickFilter}
            onRemoveFilterChip={onRemoveFilterChip}
          />
        </div>

        {status === "loading" ? <ListSkeleton /> : null}

        {status === "forbidden" ? (
          <div className="p-4 lg:p-5">
            <AdminCommerceAccessDenied />
          </div>
        ) : null}

        {status === "error" ? (
          <div className="p-4 lg:p-5">
            <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
              <ErrorState message={errorMessage} />
              <button
                type="button"
                onClick={onRetry}
                className="mt-4 inline-flex min-h-11 items-center justify-center rounded-lg bg-admin-accent px-4 py-2 text-sm font-medium text-white hover:bg-admin-accent-strong"
              >
                Thử lại
              </button>
            </AdminSurfaceCard>
          </div>
        ) : null}

        {status === "ready" ? (
          <div className="p-4 lg:p-5">
            <ProductModerationBulkActionBar
              selectedCount={selectedProductIds?.length || 0}
              canRemoveProduct={canRemoveProduct}
              canRestoreProduct={canRestoreProduct}
              disabled={bulkSubmitting}
              onRemove={onOpenBulkRemove}
              onRestore={onOpenBulkRestore}
              onClearSelection={onClearBulkSelection}
            />

            <div className="mb-4 flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
              <AdminPagination
                className="min-w-0 flex-1"
                currentPage={currentPage}
                totalPages={totalPages}
                summary={summary}
                showPageNumbers
                onPrevious={() => onPageChange?.(currentPage - 1)}
                onNext={() => onPageChange?.(currentPage + 1)}
                onGoToPage={onPageChange}
                previousLabel="Trước"
                nextLabel="Sau"
              />
              <PageSizeSelect
                value={pageSize}
                onChange={onPageSizeChange}
                disabled={totalPages === 0 && !items?.length}
              />
            </div>

            {items?.length ? (
              <ProductModerationTable
                items={items}
                selectedProductId={selectedProductId}
                selectedProductIds={selectedProductIds}
                selectionEnabled={selectionEnabled}
                onRowSelect={onRowSelect}
                onToggleProduct={onToggleProduct}
                onToggleAll={onToggleAll}
              />
            ) : (
              <p className="rounded-lg border border-dashed border-admin-border px-4 py-10 text-center text-sm text-admin-text-secondary">
                Không có sản phẩm phù hợp bộ lọc.
              </p>
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}

import {
  REVIEW_MODERATION_LIST_SORT_OPTIONS,
  REVIEW_MODERATION_PAGE_SIZE_OPTIONS,
} from "../constants/reviewModerationListConstants.js";
import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../auth/admin/components/ui";
import { ErrorState } from "../../../shared/ui/PageState.jsx";
import { AdminCommerceAccessDenied } from "./AdminCommerceAccessDenied";
import { ReviewModerationBulkActionBar } from "./ReviewModerationBulkActionBar.jsx";
import { ReviewModerationFilterBar } from "./ReviewModerationFilterBar.jsx";
import { ReviewModerationStatsBar } from "./ReviewModerationStatsBar.jsx";
import { ReviewModerationTable } from "./ReviewModerationTable.jsx";

function sortColumnLabel(sortField) {
  const option = REVIEW_MODERATION_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
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
        {REVIEW_MODERATION_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

export function ReviewModerationListView({
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
  selectedReviewId,
  selectedReviewIds,
  selectionEnabled,
  canHideReview,
  canRestoreReview,
  bulkSubmitting,
  onToggleReview,
  onToggleAll,
  onOpenBulkHide,
  onOpenBulkRestore,
  onClearBulkSelection,
  onPageChange,
  onPageSizeChange,
  onRowSelect,
  drawer,
}) {
  const summary =
    status === "ready"
      ? `${pagination?.total_items ?? pagination?.totalItems ?? 0} đánh giá · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${pagination?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Kiểm duyệt nội dung"
        title="Kiểm duyệt đánh giá"
        subtitle="Lọc danh sách, mở drawer để xem ngữ cảnh buyer/seller/sản phẩm và ẩn hoặc khôi phục đánh giá."
      />

      <ReviewModerationStatsBar stats={stats} status={statsStatus} onPresetClick={onStatPresetClick} />

      <AdminSurfaceCard padding="none" className="overflow-hidden">
        <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-5">
          <ReviewModerationFilterBar
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
            <ReviewModerationBulkActionBar
              selectedCount={selectedReviewIds?.length || 0}
              canHideReview={canHideReview}
              canRestoreReview={canRestoreReview}
              disabled={bulkSubmitting}
              onHide={onOpenBulkHide}
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
              <ReviewModerationTable
                items={items}
                selectedReviewId={selectedReviewId}
                selectedReviewIds={selectedReviewIds}
                selectionEnabled={selectionEnabled}
                onRowSelect={onRowSelect}
                onToggleReview={onToggleReview}
                onToggleAll={onToggleAll}
              />
            ) : (
              <p className="rounded-lg border border-dashed border-admin-border px-4 py-10 text-center text-sm text-admin-text-secondary">
                Không có đánh giá phù hợp bộ lọc.
              </p>
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}

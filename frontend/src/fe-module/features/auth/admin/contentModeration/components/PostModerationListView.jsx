import {
  POST_MODERATION_LIST_SORT_OPTIONS,
  POST_MODERATION_PAGE_SIZE_OPTIONS,
} from "../constants/postModerationListConstants.js";
import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { PostModerationFilterBar } from "./PostModerationFilterBar.jsx";
import { PostModerationBulkActionBar } from "./PostModerationBulkActionBar.jsx";
import { PostModerationStatsBar } from "./PostModerationStatsBar.jsx";
import { PostModerationTable } from "./PostModerationTable.jsx";

function sortColumnLabel(sortField) {
  const option = POST_MODERATION_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Ngày tạo (mới nhất)";
}

function PostListSkeleton() {
  return (
    <div className="space-y-3 p-4 lg:p-5">
      {Array.from({ length: 6 }, (_, index) => (
        <div key={index} className="h-14 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );
}

function PostPageSizeSelect({ value, onChange, disabled }) {
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
        {POST_MODERATION_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

export function PostModerationListView({
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
  authorSummaries,
  pagination,
  currentPage,
  totalPages,
  pageSize,
  activeSort,
  selectedPostId,
  selectedPostIds,
  selectionEnabled,
  canModeratePost,
  canRestorePost,
  bulkSubmitting,
  onTogglePost,
  onToggleAll,
  onOpenBulkModerate,
  onOpenBulkRestore,
  onClearBulkSelection,
  onPageChange,
  onPageSizeChange,
  onRowSelect,
  drawer,
}) {
  const summary =
    status === "ready"
      ? `${pagination?.total_items ?? 0} bài viết · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${pagination?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Kiểm duyệt nội dung"
        title="Kiểm duyệt bài viết"
        subtitle="Lọc danh sách, xem thống kê, chọn nhiều bài để kiểm duyệt hoặc khôi phục hàng loạt."
      />

      <PostModerationStatsBar
        stats={stats}
        status={statsStatus}
        onPresetClick={onStatPresetClick}
      />

      <AdminSurfaceCard padding="none" className="overflow-hidden">
        <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-5">
          <PostModerationFilterBar
            appliedFilters={appliedFilters}
            draftFilters={draftFilters}
            onDraftFiltersChange={onDraftFiltersChange}
            onApply={onApplyFilters}
            onClear={onClearFilters}
            onQuickFilter={onQuickFilter}
            onRemoveFilterChip={onRemoveFilterChip}
          />
        </div>

        {status === "loading" ? <PostListSkeleton /> : null}
        {status === "forbidden" ? (
          <div className="p-4 lg:p-5">
            <ErrorState message={errorMessage} />
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
            <PostModerationBulkActionBar
              selectedCount={selectedPostIds?.length || 0}
              canModeratePost={canModeratePost}
              canRestorePost={canRestorePost}
              disabled={bulkSubmitting}
              onModerate={onOpenBulkModerate}
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
              <PostPageSizeSelect
                value={pageSize}
                onChange={onPageSizeChange}
                disabled={totalPages === 0 && !items?.length}
              />
            </div>

            {items?.length ? (
              <PostModerationTable
                items={items}
                authorSummaries={authorSummaries}
                selectedPostId={selectedPostId}
                selectedPostIds={selectedPostIds}
                selectionEnabled={selectionEnabled}
                onRowSelect={onRowSelect}
                onTogglePost={onTogglePost}
                onToggleAll={onToggleAll}
              />
            ) : (
              <p className="rounded-lg border border-dashed border-admin-border px-4 py-10 text-center text-sm text-admin-text-secondary">
                Không có bài viết phù hợp bộ lọc.
              </p>
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}

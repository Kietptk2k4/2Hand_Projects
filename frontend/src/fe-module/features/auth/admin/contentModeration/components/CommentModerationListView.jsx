import {
  COMMENT_MODERATION_LIST_SORT_OPTIONS,
  COMMENT_MODERATION_PAGE_SIZE_OPTIONS,
} from "../constants/commentModerationListConstants.js";
import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { CommentModerationBulkActionBar } from "./CommentModerationBulkActionBar.jsx";
import { CommentModerationFilterBar } from "./CommentModerationFilterBar.jsx";
import { CommentModerationStatsBar } from "./CommentModerationStatsBar.jsx";
import { CommentModerationTable } from "./CommentModerationTable.jsx";

function sortColumnLabel(sortField) {
  const option = COMMENT_MODERATION_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Ngày tạo (mới nhất)";
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
        {COMMENT_MODERATION_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

export function CommentModerationListView({
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
  selectedCommentId,
  selectedCommentIds,
  selectionEnabled,
  canModerateComment,
  canRestoreComment,
  bulkSubmitting,
  onToggleComment,
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
      ? `${pagination?.total_items ?? 0} bình luận · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${pagination?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Kiểm duyệt nội dung"
        title="Kiểm duyệt bình luận"
        subtitle="Lọc danh sách, mở drawer để xem ngữ cảnh bài viết và kiểm duyệt hoặc khôi phục."
      />

      <CommentModerationStatsBar stats={stats} status={statsStatus} onPresetClick={onStatPresetClick} />

      <AdminSurfaceCard padding="none" className="overflow-hidden">
        <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-5">
          <CommentModerationFilterBar
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
            <CommentModerationBulkActionBar
              selectedCount={selectedCommentIds?.length || 0}
              canModerateComment={canModerateComment}
              canRestoreComment={canRestoreComment}
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
              <PageSizeSelect
                value={pageSize}
                onChange={onPageSizeChange}
                disabled={totalPages === 0 && !items?.length}
              />
            </div>

            {items?.length ? (
              <CommentModerationTable
                items={items}
                authorSummaries={authorSummaries}
                selectedCommentId={selectedCommentId}
                selectedCommentIds={selectedCommentIds}
                selectionEnabled={selectionEnabled}
                onRowSelect={onRowSelect}
                onToggleComment={onToggleComment}
                onToggleAll={onToggleAll}
              />
            ) : (
              <p className="rounded-lg border border-dashed border-admin-border px-4 py-10 text-center text-sm text-admin-text-secondary">
                Không có bình luận phù hợp bộ lọc.
              </p>
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}

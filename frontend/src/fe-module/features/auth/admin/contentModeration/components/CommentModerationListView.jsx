import { COMMENT_MODERATION_LIST_SORT_OPTIONS } from "../constants/commentModerationListConstants.js";
import {
  AdminFilterButton,
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { CommentModerationFilterBar } from "./CommentModerationFilterBar.jsx";
import { CommentModerationTable } from "./CommentModerationTable.jsx";

function sortColumnLabel(sortField) {
  const option = COMMENT_MODERATION_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Ngày tạo (mới nhất)";
}

function ListSkeleton() {
  return (
    <AdminSurfaceCard padding="md" className="animate-pulse">
      <div className="h-40 rounded-lg bg-admin-surface-muted" />
    </AdminSurfaceCard>
  );
}

export function CommentModerationListView({
  status,
  errorMessage,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onClearFilters,
  onRetry,
  items,
  pagination,
  currentPage,
  totalPages,
  activeSort,
  selectedCommentId,
  onPageChange,
  onRowSelect,
}) {
  const summary =
    status === "ready"
      ? `${pagination?.total_items ?? 0} bình luận · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${pagination?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-4">
      <AdminSurfaceCard padding="lg">
        <CommentModerationFilterBar
          draftFilters={draftFilters}
          onDraftFiltersChange={onDraftFiltersChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </AdminSurfaceCard>

      {status === "loading" ? <ListSkeleton /> : null}
      {status === "forbidden" ? <ErrorState message={errorMessage} /> : null}

      {status === "error" ? (
        <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
          <ErrorState message={errorMessage} />
          <AdminFilterButton type="button" variant="primary" className="mt-4" onClick={onRetry}>
            Thử lại
          </AdminFilterButton>
        </AdminSurfaceCard>
      ) : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
          <AdminPagination
            currentPage={currentPage}
            totalPages={totalPages}
            summary={summary}
            onPrevious={() => onPageChange?.(currentPage - 1)}
            onNext={() => onPageChange?.(currentPage + 1)}
          />
          <div className="mt-4">
            <CommentModerationTable
              items={items}
              selectedCommentId={selectedCommentId}
              onRowSelect={onRowSelect}
            />
          </div>
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}

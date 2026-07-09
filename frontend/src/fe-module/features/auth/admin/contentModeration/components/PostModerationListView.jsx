import { POST_MODERATION_LIST_SORT_OPTIONS } from "../constants/postModerationListConstants.js";
import {
  AdminFilterButton,
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { PostModerationFilterBar } from "./PostModerationFilterBar.jsx";
import { PostModerationTable } from "./PostModerationTable.jsx";

function sortColumnLabel(sortField) {
  const option = POST_MODERATION_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Ngày tạo (mới nhất)";
}

function MetricSkeleton() {
  return (
    <AdminSurfaceCard padding="md" className="animate-pulse">
      <div className="h-40 rounded-lg bg-admin-surface-muted" />
    </AdminSurfaceCard>
  );
}

export function PostModerationListView({
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
  selectedPostId,
  onPageChange,
  onRowSelect,
}) {
  const summary =
    status === "ready"
      ? `${pagination?.total_items ?? 0} bài viết · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${pagination?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-4">
      <AdminSurfaceCard padding="lg">
        <PostModerationFilterBar
          draftFilters={draftFilters}
          onDraftFiltersChange={onDraftFiltersChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </AdminSurfaceCard>

      {status === "loading" ? <MetricSkeleton /> : null}
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
            <PostModerationTable
              items={items}
              selectedPostId={selectedPostId}
              onRowSelect={onRowSelect}
            />
          </div>
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}

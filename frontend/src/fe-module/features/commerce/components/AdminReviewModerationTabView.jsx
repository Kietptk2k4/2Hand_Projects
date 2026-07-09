import {
  AdminFilterButton,
  AdminPageHeader,
  AdminSurfaceCard,
} from "../../auth/admin/components/ui";
import { AdminCommerceAccessDenied } from "./AdminCommerceAccessDenied";
import { AdminReviewModerationFilters } from "./AdminReviewModerationFilters";
import { AdminReviewModerationPagination } from "./AdminReviewModerationPagination";
import { AdminReviewModerationTable } from "./AdminReviewModerationTable";

function ListSkeleton() {
  return (
    <div className="space-y-3">
      {[1, 2, 3, 4, 5].map((key) => (
        <div key={key} className="h-16 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );
}

export function AdminReviewModerationTabView({
  forbidden,
  isLoading,
  errorMessage,
  isEmpty,
  items,
  pagination,
  page,
  totalPages,
  totalItems,
  rangeStart,
  rangeEnd,
  disabled,
  activeStatusTabId,
  onStatusChange,
  ratingFilter,
  onRatingChange,
  searchInput,
  onSearchInputChange,
  onSearchSubmit,
  onModerate,
  onRetry,
  onPrevPage,
  onNextPage,
  onGoToPage,
}) {
  return (
    <div className="mx-auto max-w-[1440px] space-y-4">
      <AdminPageHeader
        title="Kiểm duyệt đánh giá"
        subtitle="Ẩn hoặc khôi phục đánh giá sản phẩm trên marketplace. Rating shop chỉ tính từ review công khai (VISIBLE)."
      />

      {forbidden ? <AdminCommerceAccessDenied /> : null}

      {!forbidden ? (
        <>
          <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
            <AdminReviewModerationFilters
              activeStatusTabId={activeStatusTabId}
              onStatusChange={onStatusChange}
              ratingFilter={ratingFilter}
              onRatingChange={onRatingChange}
              searchInput={searchInput}
              onSearchInputChange={onSearchInputChange}
              onSearchSubmit={onSearchSubmit}
              disabled={disabled}
            />
          </AdminSurfaceCard>

          <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
            {isLoading ? <ListSkeleton /> : null}

            {!isLoading && errorMessage ? (
              <div className="py-6 text-center">
                <p className="text-sm text-admin-danger">{errorMessage}</p>
                <AdminFilterButton type="button" variant="primary" className="mt-4" onClick={onRetry}>
                  Thử lại
                </AdminFilterButton>
              </div>
            ) : null}

            {!isLoading && !errorMessage && isEmpty ? (
              <div className="py-10 text-center">
                <span className="material-symbols-outlined mb-2 text-4xl text-admin-text-muted" aria-hidden="true">
                  rate_review
                </span>
                <p className="text-sm text-admin-text-secondary">
                  Không có đánh giá phù hợp bộ lọc.
                </p>
              </div>
            ) : null}

            {!isLoading && !errorMessage && items.length > 0 ? (
              <AdminReviewModerationTable items={items} disabled={disabled} onModerate={onModerate} />
            ) : null}

            {!isLoading && !errorMessage && pagination ? (
              <AdminReviewModerationPagination
                page={page}
                totalPages={totalPages}
                rangeStart={rangeStart}
                rangeEnd={rangeEnd}
                totalItems={totalItems}
                disabled={disabled}
                onPrev={onPrevPage}
                onNext={onNextPage}
                onGoToPage={onGoToPage}
              />
            ) : null}
          </AdminSurfaceCard>
        </>
      ) : null}
    </div>
  );
}

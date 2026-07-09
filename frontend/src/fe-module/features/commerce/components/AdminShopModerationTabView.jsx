import {
  AdminFilterButton,
  AdminPageHeader,
  AdminSurfaceCard,
} from "../../auth/admin/components/ui";
import { AdminCommerceAccessDenied } from "./AdminCommerceAccessDenied";
import { AdminShopModerationFilters } from "./AdminShopModerationFilters";
import { AdminShopModerationPagination } from "./AdminShopModerationPagination";
import { AdminShopModerationTable } from "./AdminShopModerationTable";

function ListSkeleton() {
  return (
    <div className="space-y-3">
      {[1, 2, 3, 4].map((key) => (
        <div key={key} className="h-16 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );
}

export function AdminShopModerationTabView({
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
  sort,
  onSortChange,
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
        title="Kiểm duyệt cửa hàng"
        subtitle="Xem, tạm ngưng hoặc khôi phục shop trên marketplace."
        actions={
          <button
            type="button"
            disabled
            title="Sắp có"
            className="inline-flex min-h-11 cursor-not-allowed items-center gap-2 rounded-lg border border-admin-border bg-admin-surface-muted px-4 py-2 text-sm font-medium text-admin-text-muted opacity-70"
          >
            <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
              add
            </span>
            Mời shop mới
          </button>
        }
      />

      {forbidden ? <AdminCommerceAccessDenied /> : null}

      {!forbidden ? (
        <>
          <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
            <AdminShopModerationFilters
              activeStatusTabId={activeStatusTabId}
              onStatusChange={onStatusChange}
              sort={sort}
              onSortChange={onSortChange}
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
                  storefront
                </span>
                <p className="text-sm text-admin-text-secondary">Không có shop phù hợp bộ lọc.</p>
              </div>
            ) : null}

            {!isLoading && !errorMessage && items.length > 0 ? (
              <AdminShopModerationTable items={items} disabled={disabled} onModerate={onModerate} />
            ) : null}

            {!isLoading && !errorMessage && pagination ? (
              <AdminShopModerationPagination
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

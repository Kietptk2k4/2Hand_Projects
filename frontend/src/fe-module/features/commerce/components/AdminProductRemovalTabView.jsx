import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminPageHeader,
  AdminSurfaceCard,
} from "../../auth/admin/components/ui";
import { AdminCommerceAccessDenied } from "./AdminCommerceAccessDenied";
import { AdminProductRemovalFilters } from "./AdminProductRemovalFilters";
import { AdminProductRemovalPagination } from "./AdminProductRemovalPagination";
import { AdminProductRemovalTable } from "./AdminProductRemovalTable";

function ListSkeleton() {
  return (
    <div className="space-y-3">
      {[1, 2, 3, 4, 5].map((key) => (
        <div key={key} className="h-16 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );
}

function HeaderSearch({ searchInput, onSearchInputChange, onSearchSubmit, disabled }) {
  return (
    <form
      className="w-full sm:w-80"
      onSubmit={(event) => {
        event.preventDefault();
        onSearchSubmit?.();
      }}
    >
      <AdminFilterField label="Tìm kiếm" htmlFor="product-mod-search" className="mb-0">
        <AdminFilterInput
          id="product-mod-search"
          type="search"
          value={searchInput}
          onChange={(event) => onSearchInputChange(event.target.value)}
          disabled={disabled}
          placeholder="Tên sản phẩm hoặc Shop ID…"
        />
      </AdminFilterField>
    </form>
  );
}

export function AdminProductRemovalTabView({
  showHistory,
  historyPanel,
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
  searchInput,
  onSearchInputChange,
  onSearchSubmit,
  onRemove,
  onRestore,
  onViewCase,
  onViewHistory,
  onRetry,
  onPrevPage,
  onNextPage,
  onGoToPage,
}) {
  return (
    <div className="mx-auto max-w-[1440px] space-y-4">
      <AdminPageHeader
        title="Kiểm duyệt sản phẩm"
        subtitle="Rà soát và gỡ listing vi phạm qua admin-service; danh sách vẫn lấy từ Commerce."
        actions={
          !showHistory ? (
            <HeaderSearch
              searchInput={searchInput}
              onSearchInputChange={onSearchInputChange}
              onSearchSubmit={onSearchSubmit}
              disabled={disabled}
            />
          ) : null
        }
      />

      {showHistory ? historyPanel : null}

      {!showHistory && forbidden ? <AdminCommerceAccessDenied /> : null}

      {!showHistory && !forbidden ? (
        <AdminSurfaceCard padding="none" className="max-w-full min-w-0 overflow-hidden">
          <div className="px-4 pt-2 lg:px-6">
            <AdminProductRemovalFilters
              activeStatusTabId={activeStatusTabId}
              onStatusChange={onStatusChange}
              disabled={disabled}
            />
          </div>

          <div className="p-4 lg:p-6">
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
                  inventory_2
                </span>
                <p className="text-sm text-admin-text-secondary">
                  Không có sản phẩm phù hợp bộ lọc.
                </p>
              </div>
            ) : null}

            {!isLoading && !errorMessage && items.length > 0 ? (
              <AdminProductRemovalTable
                items={items}
                disabled={disabled}
                onRemove={onRemove}
                onRestore={onRestore}
                onViewCase={onViewCase}
                onViewHistory={onViewHistory}
              />
            ) : null}

            {!isLoading && !errorMessage && pagination ? (
              <AdminProductRemovalPagination
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
          </div>
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}

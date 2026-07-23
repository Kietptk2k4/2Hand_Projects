import {
  ORDER_LIST_PAGE_SIZE_OPTIONS,
  ORDER_LIST_SORT_OPTIONS,
} from "../constants/orderSupportListConstants.js";
import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import {
  ORDER_SUPPORT_ORDER_SUBTITLE,
  ORDER_SUPPORT_ORDER_TITLE,
} from "../constants/orderSupportUiStrings.js";
import { OrderSupportActiveFilterChips } from "./OrderSupportActiveFilterChips.jsx";
import { OrderSupportFilterBar } from "./OrderSupportFilterBar.jsx";
import { OrderSupportQuickFilterChips } from "./OrderSupportQuickFilterChips.jsx";
import { OrderSupportStatsBar } from "./OrderSupportStatsBar.jsx";
import { OrderSupportTable } from "./OrderSupportTable.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";

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
        {ORDER_LIST_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

function sortColumnLabel(sortField) {
  const option = ORDER_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Tạo gần nhất";
}

function OrderListEmptyState() {
  return (
    <div className="rounded-xl border border-dashed border-admin-border px-6 py-12 text-center">
      <span
        className="material-symbols-outlined mx-auto text-[40px] text-admin-text-muted"
        aria-hidden="true"
      >
        shopping_bag
      </span>
      <p className="mt-4 text-sm font-medium text-admin-text">Không có đơn hàng phù hợp</p>
      <p className="mx-auto mt-2 max-w-md text-sm text-admin-text-secondary">
        Thử điều chỉnh bộ lọc hoặc tra cứu trực tiếp bằng UUID đơn hàng.
      </p>
    </div>
  );
}

export function OrderSupportListView({
  canReadOrder,
  listStatus,
  listErrorMessage,
  listResult,
  appliedFilters,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onClearFilters,
  onQuickFilter,
  onRemoveFilterChip,
  onRetryList,
  stats,
  statsStatus,
  lookupValue,
  onLookupChange,
  onLookupSubmit,
  lookupError,
  currentPage,
  totalPages,
  pageSize,
  activeSort,
  selectedOrderId,
  onOrderSelect,
  onPageChange,
  onPageSizeChange,
  onCopied,
  formatVndPrice,
  drawer,
}) {
  if (!canReadOrder) {
    return (
      <div className="mb-6 max-w-full min-w-0 space-y-6">
        <AdminPageHeader
          eyebrow="Hỗ trợ đơn hàng"
          title={ORDER_SUPPORT_ORDER_TITLE}
          subtitle={ORDER_SUPPORT_ORDER_SUBTITLE}
        />
        <SupportForbiddenState message="Tài khoản thiếu quyền ORDER_SUPPORT_READ." />
      </div>
    );
  }

  const summary =
    listStatus === "ready"
      ? `${listResult?.total_elements ?? 0} đơn hàng · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${listResult?.page ?? currentPage}/${Math.max(totalPages, 1)}`
      : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Hỗ trợ đơn hàng"
        title={ORDER_SUPPORT_ORDER_TITLE}
        subtitle={ORDER_SUPPORT_ORDER_SUBTITLE}
      />

      <OrderSupportStatsBar
        stats={stats}
        status={statsStatus}
        onPresetClick={onQuickFilter}
      />

      <AdminSurfaceCard padding="none" className="overflow-hidden">
        <div className="space-y-3 border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-5">
          <OrderSupportQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
          <OrderSupportActiveFilterChips filters={appliedFilters} onRemoveChip={onRemoveFilterChip} />
          <OrderSupportFilterBar
            draftFilters={draftFilters}
            onDraftFiltersChange={onDraftFiltersChange}
            onApply={onApplyFilters}
            onClear={onClearFilters}
            lookupValue={lookupValue}
            onLookupChange={onLookupChange}
            onLookupSubmit={onLookupSubmit}
            lookupError={lookupError}
          />
        </div>

        {listStatus === "loading" ? <SupportListSkeleton /> : null}
        {listStatus === "forbidden" ? (
          <div className="p-4 lg:p-5">
            <SupportForbiddenState message={listErrorMessage} />
          </div>
        ) : null}
        {listStatus === "unavailable" ? (
          <div className="p-4 lg:p-5">
            <SupportUnavailableState message={listErrorMessage} />
          </div>
        ) : null}
        {listStatus === "error" ? (
          <div className="p-4 lg:p-5">
            <SupportRetryPanel message={listErrorMessage} onRetry={onRetryList} />
          </div>
        ) : null}

        {listStatus === "ready" ? (
          <div className="p-4 lg:p-5">
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
                disabled={totalPages === 0 && !listResult?.orders?.length}
              />
            </div>

            {listResult?.orders?.length ? (
              <OrderSupportTable
                orders={listResult.orders}
                selectedOrderId={selectedOrderId}
                onOrderSelect={onOrderSelect}
                formatVndPrice={formatVndPrice}
                onCopied={onCopied}
              />
            ) : (
              <OrderListEmptyState />
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}

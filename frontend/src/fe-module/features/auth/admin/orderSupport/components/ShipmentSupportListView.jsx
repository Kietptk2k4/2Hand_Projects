import { SHIPMENT_LIST_PAGE_SIZE_OPTIONS } from "../constants/shipmentSupportListConstants.js";
import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import {
  ORDER_SUPPORT_SHIPMENT_SUBTITLE,
  ORDER_SUPPORT_SHIPMENT_TITLE,
} from "../constants/orderSupportUiStrings.js";
import { SHIPMENT_LIST_SORT_OPTIONS } from "../constants/shipmentSupportListConstants.js";
import { ShipmentSupportActiveFilterChips } from "./ShipmentSupportActiveFilterChips.jsx";
import { ShipmentSupportFilterBar } from "./ShipmentSupportFilterBar.jsx";
import { ShipmentSupportQuickFilterChips } from "./ShipmentSupportQuickFilterChips.jsx";
import { ShipmentSupportStatsBar } from "./ShipmentSupportStatsBar.jsx";
import { ShipmentSupportTable } from "./ShipmentSupportTable.jsx";
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
        {SHIPMENT_LIST_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

function sortColumnLabel(sortField) {
  const option = SHIPMENT_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Cập nhật gần nhất";
}

function ShipmentListEmptyState() {
  return (
    <div className="rounded-xl border border-dashed border-admin-border px-6 py-12 text-center">
      <span
        className="material-symbols-outlined mx-auto text-[40px] text-admin-text-muted"
        aria-hidden="true"
      >
        local_shipping
      </span>
      <p className="mt-4 text-sm font-medium text-admin-text">Không có vận đơn phù hợp</p>
      <p className="mx-auto mt-2 max-w-md text-sm text-admin-text-secondary">
        Thử điều chỉnh bộ lọc hoặc tra cứu trực tiếp bằng UUID vận đơn.
      </p>
    </div>
  );
}

export function ShipmentSupportListView({
  canReadShipment,
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
  selectedShipmentId,
  onShipmentSelect,
  onPageChange,
  onPageSizeChange,
  onCopied,
  drawer,
}) {
  if (!canReadShipment) {
    return (
      <div className="mb-6 w-full min-w-0 flex-1 space-y-6">
        <AdminPageHeader
          eyebrow="Hỗ trợ đơn hàng"
          title={ORDER_SUPPORT_SHIPMENT_TITLE}
          subtitle={ORDER_SUPPORT_SHIPMENT_SUBTITLE}
        />
        <SupportForbiddenState message="Tài khoản thiếu quyền SHIPMENT_SUPPORT_READ." />
      </div>
    );
  }

  const summary =
    listStatus === "ready"
      ? `${listResult?.total_elements ?? 0} vận đơn · Sắp xếp: ${sortColumnLabel(appliedFilters?.sort || "updated_at")} · Trang ${listResult?.page ?? currentPage}/${Math.max(totalPages, 1)}`
      : "";

  return (
    <div className="mb-6 w-full min-w-0 flex-1 space-y-6">
      <AdminPageHeader
        eyebrow="Hỗ trợ đơn hàng"
        title={ORDER_SUPPORT_SHIPMENT_TITLE}
        subtitle={ORDER_SUPPORT_SHIPMENT_SUBTITLE}
      />

      <ShipmentSupportStatsBar stats={stats} status={statsStatus} onPresetClick={onQuickFilter} />

      <AdminSurfaceCard padding="none" className="w-full overflow-hidden">
        <div className="w-full space-y-3 border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-6">
          <ShipmentSupportQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
          <ShipmentSupportActiveFilterChips
            filters={appliedFilters}
            onRemoveChip={onRemoveFilterChip}
          />
          <ShipmentSupportFilterBar
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
          <div className="p-4 lg:p-6">
            <SupportForbiddenState message={listErrorMessage} />
          </div>
        ) : null}
        {listStatus === "unavailable" ? (
          <div className="p-4 lg:p-6">
            <SupportUnavailableState message={listErrorMessage} />
          </div>
        ) : null}
        {listStatus === "error" ? (
          <div className="p-4 lg:p-6">
            <SupportRetryPanel message={listErrorMessage} onRetry={onRetryList} />
          </div>
        ) : null}

        {listStatus === "ready" ? (
          <div className="w-full p-4 lg:p-6">
            {summary ? (
              <p className="mb-3 text-sm text-admin-text-secondary">{summary}</p>
            ) : null}
            <div className="mb-4 flex w-full flex-col gap-3 sm:flex-row sm:items-center sm:justify-end">
              <AdminPagination
                currentPage={currentPage}
                totalPages={totalPages}
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
                disabled={totalPages === 0 && !listResult?.shipments?.length}
              />
            </div>

            {listResult?.shipments?.length ? (
              <div className="w-full min-w-0 overflow-x-auto">
                <ShipmentSupportTable
                  shipments={listResult.shipments}
                  selectedShipmentId={selectedShipmentId}
                  onShipmentSelect={onShipmentSelect}
                  onCopied={onCopied}
                />
              </div>
            ) : (
              <ShipmentListEmptyState />
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}

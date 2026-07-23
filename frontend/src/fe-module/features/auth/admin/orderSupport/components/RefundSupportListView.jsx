import { REFUND_LIST_PAGE_SIZE_OPTIONS } from "../constants/refundSupportListConstants.js";
import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import { RefundSupportActiveFilterChips } from "./RefundSupportActiveFilterChips.jsx";
import { RefundSupportFilterBar } from "./RefundSupportFilterBar.jsx";
import { RefundSupportQuickFilterChips } from "./RefundSupportQuickFilterChips.jsx";
import { RefundSupportStatsBar } from "./RefundSupportStatsBar.jsx";
import { RefundSupportTable } from "./RefundSupportTable.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";

const REFUND_TITLE = "Duyệt hoàn tiền";
const REFUND_SUBTITLE =
  "Xác nhận sau khi đã hoàn tiền thủ công trên VNPay. Không có nút hủy đơn trực tiếp.";

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
        {REFUND_LIST_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

function RefundListEmptyState() {
  return (
    <div className="rounded-xl border border-dashed border-admin-border px-6 py-12 text-center">
      <span
        className="material-symbols-outlined mx-auto text-[40px] text-admin-text-muted"
        aria-hidden="true"
      >
        currency_exchange
      </span>
      <p className="mt-4 text-sm font-medium text-admin-text">Không có yêu cầu hoàn tiền phù hợp</p>
      <p className="mx-auto mt-2 max-w-md text-sm text-admin-text-secondary">
        Thử điều chỉnh bộ lọc hoặc tra cứu trực tiếp bằng UUID yêu cầu hoàn tiền.
      </p>
    </div>
  );
}

export function RefundSupportListView({
  canReadRefund,
  canApproveRefund,
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
  selectedRefundId,
  actionId,
  onRefundSelect,
  onConfirm,
  onReject,
  onPageChange,
  onPageSizeChange,
  onCopied,
  formatVndPrice,
  drawer,
}) {
  if (!canReadRefund) {
    return (
      <div className="mb-6 w-full min-w-0 flex-1 space-y-6">
        <AdminPageHeader eyebrow="Hỗ trợ đơn hàng" title={REFUND_TITLE} subtitle={REFUND_SUBTITLE} />
        <SupportForbiddenState message="Tài khoản thiếu quyền REFUND_SUPPORT_READ." />
      </div>
    );
  }

  const summary =
    listStatus === "ready"
      ? `${listResult?.pagination?.totalItems ?? 0} yêu cầu · Trang ${listResult?.pagination?.page ?? currentPage}/${Math.max(totalPages, 1)}`
      : "";

  return (
    <div className="mb-6 w-full min-w-0 flex-1 space-y-6">
      <AdminPageHeader eyebrow="Hỗ trợ đơn hàng" title={REFUND_TITLE} subtitle={REFUND_SUBTITLE} />

      <RefundSupportStatsBar stats={stats} status={statsStatus} onPresetClick={onQuickFilter} />

      <AdminSurfaceCard padding="none" className="w-full overflow-hidden">
        <div className="w-full space-y-3 border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-6">
          <RefundSupportQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
          <RefundSupportActiveFilterChips filters={appliedFilters} onRemoveChip={onRemoveFilterChip} />
          <RefundSupportFilterBar
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
            {summary ? <p className="mb-3 text-sm text-admin-text-secondary">{summary}</p> : null}
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
                disabled={totalPages === 0 && !listResult?.items?.length}
              />
            </div>

            {listResult?.items?.length ? (
              <div className="w-full min-w-0 overflow-x-auto">
                <RefundSupportTable
                  items={listResult.items}
                  selectedRefundId={selectedRefundId}
                  actionId={actionId}
                  canApproveRefund={canApproveRefund}
                  onRefundSelect={onRefundSelect}
                  onConfirm={onConfirm}
                  onReject={onReject}
                  formatVndPrice={formatVndPrice}
                  onCopied={onCopied}
                />
              </div>
            ) : (
              <RefundListEmptyState />
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}

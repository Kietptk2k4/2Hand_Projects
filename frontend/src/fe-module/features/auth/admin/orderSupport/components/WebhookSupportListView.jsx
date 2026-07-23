import { WEBHOOK_LIST_PAGE_SIZE_OPTIONS } from "../constants/webhookSupportListConstants.js";
import {
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
  AdminFilterButton,
} from "../../components/ui";
import { WebhookSupportActiveFilterChips } from "./WebhookSupportActiveFilterChips.jsx";
import { WebhookSupportFilterBar } from "./WebhookSupportFilterBar.jsx";
import { WebhookSupportQuickFilterChips } from "./WebhookSupportQuickFilterChips.jsx";
import { WebhookSupportStatsBar } from "./WebhookSupportStatsBar.jsx";
import { WebhookSupportTable } from "./WebhookSupportTable.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";

const WEBHOOK_TITLE = "Nhật ký webhook";
const WEBHOOK_SUBTITLE =
  "Tra cứu webhook PayOS/GHN để debug callback và idempotency.";

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
        {WEBHOOK_LIST_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

export function WebhookSupportListView({
  canReadWebhook,
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
  currentPage,
  totalPages,
  pageSize,
  selectedLogId,
  onLogSelect,
  onPageChange,
  onPageSizeChange,
  onExport,
  exportPending,
  formatDateTime,
}) {
  if (!canReadWebhook) {
    return (
      <div className="mb-6 w-full min-w-0 flex-1 space-y-6">
        <AdminPageHeader eyebrow="Hỗ trợ đơn hàng" title={WEBHOOK_TITLE} subtitle={WEBHOOK_SUBTITLE} />
        <SupportForbiddenState message="Tài khoản thiếu quyền WEBHOOK_SUPPORT_READ." />
      </div>
    );
  }

  const summary =
    listStatus === "ready"
      ? `${listResult?.total_elements ?? 0} bản ghi · Trang ${listResult?.page ?? currentPage}/${Math.max(totalPages, 1)}`
      : "";

  return (
    <div className="mb-6 w-full min-w-0 flex-1 space-y-6">
      <AdminPageHeader eyebrow="Hỗ trợ đơn hàng" title={WEBHOOK_TITLE} subtitle={WEBHOOK_SUBTITLE} />

      <WebhookSupportStatsBar stats={stats} status={statsStatus} onPresetClick={onQuickFilter} />

      <AdminSurfaceCard padding="none" className="w-full overflow-hidden">
        <div className="w-full space-y-3 border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-6">
          <WebhookSupportQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
          <WebhookSupportActiveFilterChips filters={appliedFilters} onRemoveChip={onRemoveFilterChip} />
          <WebhookSupportFilterBar
            draftFilters={draftFilters}
            onDraftFiltersChange={onDraftFiltersChange}
            onApply={onApplyFilters}
            onClear={onClearFilters}
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
            <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              {summary ? <p className="text-sm text-admin-text-secondary">{summary}</p> : <span />}
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-end">
                <AdminFilterButton
                  type="button"
                  variant="secondary"
                  disabled={exportPending}
                  onClick={onExport}
                >
                  {exportPending ? "Đang xuất..." : "Xuất CSV"}
                </AdminFilterButton>
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
                  value={String(pageSize)}
                  onChange={onPageSizeChange}
                  disabled={totalPages === 0 && !listResult?.logs?.length}
                />
              </div>
            </div>

            <div className="w-full min-w-0 overflow-x-auto">
              <WebhookSupportTable
                logs={listResult?.logs}
                selectedLogId={selectedLogId}
                onLogSelect={onLogSelect}
                formatDateTime={formatDateTime}
              />
            </div>
          </div>
        ) : null}
      </AdminSurfaceCard>
    </div>
  );
}

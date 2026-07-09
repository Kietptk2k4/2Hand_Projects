import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterSelect,
  AdminPageHeader,
  AdminSurfaceCard,
} from "../../components/ui";
import { AdminRefundApprovalQueueTable } from "./AdminRefundApprovalQueueTable.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";

export function AdminRefundApprovalsView({
  statusFilter,
  statusOptions,
  statusLabels,
  requestedByLabels,
  loadStatus,
  errorMessage,
  queue,
  actionId,
  onStatusFilterChange,
  onRefresh,
  onSelectDetail,
  onConfirm,
  onReject,
  formatVndPrice,
  formatDateTime,
}) {
  return (
    <div className="mx-auto max-w-[1440px] space-y-4">
      <AdminSurfaceCard padding="lg">
        <AdminPageHeader
          title="Duyệt hoàn tiền"
          subtitle="Xác nhận sau khi đã hoàn tiền thủ công trên VNPay. Không có nút hủy đơn trực tiếp."
          className="mb-0"
        />

        <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-end">
          <AdminFilterField label="Trạng thái" htmlFor="refund-status-filter" className="mb-0 sm:min-w-[200px]">
            <AdminFilterSelect
              id="refund-status-filter"
              value={statusFilter}
              onChange={(event) => onStatusFilterChange(event.target.value)}
            >
              {statusOptions.map((value) => (
                <option key={value || "all"} value={value}>
                  {value ? statusLabels[value] || value : "Tất cả"}
                </option>
              ))}
            </AdminFilterSelect>
          </AdminFilterField>
          <AdminFilterButton
            type="button"
            variant="secondary"
            disabled={loadStatus === "loading"}
            onClick={onRefresh}
            className="w-full sm:w-auto"
          >
            Làm mới
          </AdminFilterButton>
        </div>

        {loadStatus === "loading" ? <div className="mt-4"><SupportListSkeleton rows={4} /></div> : null}
        {loadStatus === "error" ? (
          <div className="mt-4">
            <SupportRetryPanel message={errorMessage} onRetry={onRefresh} />
          </div>
        ) : null}

        {loadStatus === "ready" ? (
          <div className="mt-4 max-w-full min-w-0">
            <AdminRefundApprovalQueueTable
              items={queue.items}
              actionId={actionId}
              statusLabels={statusLabels}
              requestedByLabels={requestedByLabels}
              formatVndPrice={formatVndPrice}
              formatDateTime={formatDateTime}
              onSelectDetail={onSelectDetail}
              onConfirm={onConfirm}
              onReject={onReject}
            />
            <p className="mt-3 text-sm text-admin-text-secondary">
              Tổng {queue.pagination.totalItems} yêu cầu
            </p>
          </div>
        ) : null}
      </AdminSurfaceCard>
    </div>
  );
}

import { AdminPageHeader, AdminPagination, AdminSurfaceCard } from "../../components/ui";
import { FinanceDateRangeToolbar } from "./FinanceDateRangeToolbar.jsx";
import { FinancePayoutDonutChart } from "./FinancePayoutDonutChart.jsx";
import { PayoutQueueHeroStrip } from "./PayoutQueueHeroStrip.jsx";
import { PayoutQueueStatusChips } from "./PayoutQueueStatusChips.jsx";
import { PayoutQueueTable } from "./PayoutQueueTable.jsx";
import { PayoutActionDialog } from "./PayoutActionDialog.jsx";
import { PayoutRequestDetailDrawer } from "./PayoutRequestDetailDrawer.jsx";
import { CommerceFinanceEmptyState } from "./ui/CommerceFinanceEmptyState.jsx";
import { CommerceFinanceListSkeleton } from "./ui/CommerceFinanceListSkeleton.jsx";
import { CommerceFinanceRetryPanel } from "./ui/CommerceFinanceRetryPanel.jsx";
import {
  buildPayoutPaginationSummary,
  getPayoutEmptyMessage,
} from "../utils/payoutQueueHelpers.js";

function formatPeriodLabel(from, to) {
  if (!from || !to) return null;
  const fromDate = new Date(from);
  const toDate = new Date(to);
  if (Number.isNaN(fromDate.getTime()) || Number.isNaN(toDate.getTime())) return null;
  const inclusiveEnd = new Date(toDate);
  inclusiveEnd.setUTCDate(inclusiveEnd.getUTCDate() - 1);
  const formatter = new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
  return `${formatter.format(fromDate)} – ${formatter.format(inclusiveEnd)}`;
}

export function AdminFinancePayoutQueueView({
  statusFilter,
  from,
  to,
  activeRangeId,
  heroMetrics,
  isOverviewLoading,
  overviewPanel,
  listPanel,
  items,
  pagination,
  actionId,
  actionRequest,
  actionPending,
  detailItem,
  onStatusChange,
  onRangeChange,
  onRefresh,
  onKpiStatusClick,
  onPageChange,
  onRetryList,
  onRetryOverview,
  onActionRequest,
  onActionConfirm,
  onActionClose,
  onOpenDetail,
  onCloseDetail,
  onSellerDetail,
}) {
  const isListLoading = listPanel?.status === "loading" || listPanel?.status === "idle";
  const periodLabel = formatPeriodLabel(from, to);
  const overviewStatus =
    isOverviewLoading ? "loading" : overviewPanel?.status === "error" ? "error" : "ready";
  const listErrorOnly = listPanel?.status === "error" && !items?.length;
  const emptyMessage = getPayoutEmptyMessage(statusFilter);

  if (listErrorOnly) {
    return (
      <div className="mb-6 max-w-full min-w-0 space-y-4">
        <AdminPageHeader
          eyebrow="Tài chính thương mại"
          title="Hàng đợi rút tiền"
          subtitle="Duyệt, từ chối hoặc ghi nhận chuyển khoản cho seller."
        />
        <CommerceFinanceRetryPanel message={listPanel.errorMessage} onRetry={onRetryList} />
      </div>
    );
  }

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Tài chính thương mại"
        title="Hàng đợi rút tiền"
        subtitle={
          periodLabel
            ? `Duyệt và ghi nhận chuyển khoản · KPI kỳ ${periodLabel}`
            : "Duyệt, từ chối hoặc ghi nhận chuyển khoản cho seller."
        }
      />

      <FinanceDateRangeToolbar
        activeRangeId={activeRangeId}
        onRangeChange={onRangeChange}
        onRefresh={onRefresh}
        isLoading={isListLoading || isOverviewLoading}
        showGranularity={false}
      />

      <PayoutQueueHeroStrip
        metrics={heroMetrics}
        activeStatusFilter={statusFilter}
        isLoading={isOverviewLoading}
        onStatusClick={onKpiStatusClick}
      />

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-12">
        <div className="xl:col-span-5">
          <FinancePayoutDonutChart
            payoutOverview={overviewPanel?.data}
            status={overviewStatus}
            errorMessage={overviewPanel?.errorMessage}
            onRetry={onRetryOverview}
          />
        </div>
        <div className="xl:col-span-7">
          <AdminSurfaceCard padding="md" className="h-full">
            <PayoutQueueStatusChips
              statusFilter={statusFilter}
              onStatusChange={onStatusChange}
              disabled={isListLoading}
            />
            <p className="mt-3 text-xs text-admin-text-muted">
              Bộ lọc danh sách theo trạng thái. KPI và biểu đồ phản ánh kỳ đã chọn phía trên.
            </p>
          </AdminSurfaceCard>
        </div>
      </div>

      {isListLoading ? <CommerceFinanceListSkeleton rows={5} /> : null}

      {listPanel?.status === "error" && items?.length ? (
        <CommerceFinanceRetryPanel message={listPanel.errorMessage} onRetry={onRetryList} />
      ) : null}

      {!isListLoading && listPanel?.status === "ready" && !items?.length ? (
        <CommerceFinanceEmptyState message={emptyMessage} />
      ) : null}

      {!isListLoading && items?.length ? (
        <AdminSurfaceCard padding="md" className="max-w-full min-w-0">
          <PayoutQueueTable
            items={items}
            actionId={actionId}
            onAction={onActionRequest}
            onOpenDetail={onOpenDetail}
            onSellerDetail={onSellerDetail}
          />
          <AdminPagination
            className="mt-4"
            currentPage={pagination.page}
            totalPages={pagination.totalPages}
            summary={buildPayoutPaginationSummary({
              page: pagination.page,
              limit: pagination.limit,
              totalItems: pagination.totalItems,
            })}
            onPrevious={() => onPageChange?.(pagination.page - 1)}
            onNext={() => onPageChange?.(pagination.page + 1)}
            disabled={actionPending}
          />
        </AdminSurfaceCard>
      ) : null}

      <PayoutActionDialog
        request={actionRequest}
        pending={actionPending}
        onConfirm={onActionConfirm}
        onClose={onActionClose}
      />

      <PayoutRequestDetailDrawer
        open={Boolean(detailItem)}
        item={detailItem}
        actionId={actionId}
        onClose={onCloseDetail}
        onSellerDetail={onSellerDetail}
        onAction={onActionRequest}
      />
    </div>
  );
}

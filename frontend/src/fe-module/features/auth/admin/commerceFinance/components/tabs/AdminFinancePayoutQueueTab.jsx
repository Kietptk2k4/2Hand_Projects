import { useCallback, useState } from "react";
import {
  approveAdminPayoutRequest,
  markAdminPayoutRequestPaid,
  rejectAdminPayoutRequest,
} from "../../api/adminFinancePayoutApi.js";
import {
  applyPayoutQueueAfterAction,
  mapPayoutQueueItem,
} from "../../utils/adminFinancePayoutMapper.js";
import { useAdminFinancePayoutQueue } from "../../hooks/useAdminFinancePayoutQueue.js";
import { AdminFinancePayoutQueueView } from "../AdminFinancePayoutQueueView.jsx";

export function AdminFinancePayoutQueueTab({ onNotify }) {
  const {
    statusFilter,
    resolvedRange,
    rangeId,
    queue,
    setQueueState,
    listPanel,
    overviewPanel,
    heroMetrics,
    isOverviewLoading,
    handleStatusChange,
    handlePageChange,
    handleRangeChange,
    handleKpiStatusClick,
    refreshAll,
    retryList,
    retryOverview,
    navigateToSellerDetail,
  } = useAdminFinancePayoutQueue();

  const [actionId, setActionId] = useState("");
  const [actionPending, setActionPending] = useState(false);
  const [actionRequest, setActionRequest] = useState(null);
  const [detailItem, setDetailItem] = useState(null);

  const handleActionRequest = useCallback((item, action) => {
    if (!item?.id) return;
    setActionRequest({ item, action });
  }, []);

  const handleActionClose = useCallback(() => {
    if (actionPending) return;
    setActionRequest(null);
  }, [actionPending]);

  const runConfirmedAction = useCallback(
    async (payload = {}) => {
      const request = actionRequest;
      if (!request?.item?.id) return;

      const { item, action } = request;
      setActionPending(true);
      setActionId(item.id);

      try {
        let updatedItem = null;

        if (action === "approve") {
          updatedItem = await approveAdminPayoutRequest(item.id);
          onNotify?.({ variant: "success", message: "Đã duyệt yêu cầu rút tiền." });
        }

        if (action === "reject") {
          const note = payload.adminNote?.trim();
          if (!note) {
            onNotify?.({ variant: "error", message: "Vui lòng nhập lý do từ chối." });
            return;
          }
          updatedItem = await rejectAdminPayoutRequest(item.id, note);
          onNotify?.({ variant: "success", message: "Đã từ chối yêu cầu rút tiền." });
        }

        if (action === "mark-paid") {
          const ref = payload.bankTransferRef?.trim();
          if (!ref) {
            onNotify?.({ variant: "error", message: "Vui lòng nhập mã tham chiếu chuyển khoản." });
            return;
          }
          updatedItem = await markAdminPayoutRequestPaid(item.id, ref);
          onNotify?.({ variant: "success", message: "Đã ghi nhận chuyển khoản." });
        }

        if (updatedItem) {
          const mapped = mapPayoutQueueItem(updatedItem);
          setQueueState((prev) => applyPayoutQueueAfterAction(prev, updatedItem, statusFilter));
          setDetailItem((current) => (current?.id === item.id ? mapped : current));
        }

        setActionRequest(null);
        await Promise.all([retryList(), retryOverview()]);
      } catch (error) {
        onNotify?.({ variant: "error", message: error?.message || "Thao tác thất bại." });
      } finally {
        setActionPending(false);
        setActionId("");
      }
    },
    [actionRequest, onNotify, retryList, retryOverview, setQueueState, statusFilter],
  );

  const handleOpenDetail = useCallback((item) => {
    setDetailItem(item);
  }, []);

  const handleCloseDetail = useCallback(() => {
    setDetailItem(null);
  }, []);

  return (
    <AdminFinancePayoutQueueView
      statusFilter={statusFilter}
      from={resolvedRange.from}
      to={resolvedRange.to}
      activeRangeId={rangeId}
      heroMetrics={heroMetrics}
      isOverviewLoading={isOverviewLoading}
      overviewPanel={overviewPanel}
      listPanel={listPanel}
      items={queue.items}
      pagination={queue.pagination}
      actionId={actionId}
      actionRequest={actionRequest}
      actionPending={actionPending}
      detailItem={detailItem}
      onStatusChange={handleStatusChange}
      onRangeChange={handleRangeChange}
      onRefresh={refreshAll}
      onKpiStatusClick={handleKpiStatusClick}
      onPageChange={handlePageChange}
      onRetryList={retryList}
      onRetryOverview={retryOverview}
      onActionRequest={handleActionRequest}
      onActionConfirm={runConfirmedAction}
      onActionClose={handleActionClose}
      onOpenDetail={handleOpenDetail}
      onCloseDetail={handleCloseDetail}
      onSellerDetail={navigateToSellerDetail}
    />
  );
}

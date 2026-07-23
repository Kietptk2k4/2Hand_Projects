import { useCallback, useEffect, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice.js";
import {
  confirmAdminRefundApproval,
  fetchAdminRefundApprovals,
  rejectAdminRefundApproval,
} from "../api/adminRefundApprovalApi.js";
import {
  REFUND_DEFAULT_STATUS_FILTER,
  REFUND_LIST_PAGE_SIZE,
  REFUND_SUPPORT_VIEW_MODES,
} from "../constants/refundSupportListConstants.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { useRefundSupportDetail } from "../hooks/useRefundSupportDetail.js";
import { useRefundSupportStats } from "../hooks/useRefundSupportStats.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { mapRefundApprovalQueueResponse } from "../utils/adminRefundApprovalMapper.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import {
  buildRefundSupportQuickFilter,
  removeRefundSupportFilterChip,
} from "../utils/refundSupportFilterHelpers.js";
import { isValidUuid } from "../utils/supportNavigation.js";
import { RefundSupportActionDialog } from "./RefundSupportActionDialog.jsx";
import { RefundSupportDrawer } from "./RefundSupportDrawer.jsx";
import { RefundSupportListView } from "./RefundSupportListView.jsx";

export function RefundSupportListPanel({
  refundListFilters,
  onFiltersChange,
  refundRequestId,
  refundView,
  onRefundSelectionChange,
  onNavigate,
  onNotify,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadRefund, canApproveRefund } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");
  const [toastMessage, setToastMessage] = useState("");
  const [lookupValue, setLookupValue] = useState(refundRequestId || "");
  const [lookupError, setLookupError] = useState("");
  const [actionId, setActionId] = useState("");
  const [actionDialog, setActionDialog] = useState({ open: false, action: "", targetId: "" });

  const filterPage = Number(refundListFilters?.page) || 1;
  const filterSize = Number(refundListFilters?.limit) || REFUND_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    q: refundListFilters?.q || "",
    status: refundListFilters?.status ?? REFUND_DEFAULT_STATUS_FILTER,
    requested_by: refundListFilters?.requested_by || "",
    payment_method: refundListFilters?.payment_method || "",
    from: refundListFilters?.from || "",
    to: refundListFilters?.to || "",
  });

  const { stats, status: statsStatus, refetch: refetchStats } = useRefundSupportStats({
    enabled: canReadRefund,
  });

  const {
    detail,
    status: detailStatus,
    errorMessage: detailErrorMessage,
    refetch: refetchDetail,
  } = useRefundSupportDetail(refundRequestId, { enabled: canReadRefund && Boolean(refundRequestId) });

  useEffect(() => {
    setDraftFilters({
      q: refundListFilters?.q || "",
      status: refundListFilters?.status ?? REFUND_DEFAULT_STATUS_FILTER,
      requested_by: refundListFilters?.requested_by || "",
      payment_method: refundListFilters?.payment_method || "",
      from: refundListFilters?.from || "",
      to: refundListFilters?.to || "",
    });
  }, [refundListFilters]);

  useEffect(() => {
    setLookupValue(refundRequestId || "");
  }, [refundRequestId]);

  const fetchList = useCallback(async () => {
    if (!canReadRefund) return;

    setListStatus("loading");
    setListErrorMessage("");

    try {
      const raw = await fetchAdminRefundApprovals({
        q: refundListFilters?.q || undefined,
        status: refundListFilters?.status || undefined,
        requested_by: refundListFilters?.requested_by || undefined,
        payment_method: refundListFilters?.payment_method || undefined,
        from: refundListFilters?.from || undefined,
        to: refundListFilters?.to || undefined,
        page: Number(refundListFilters?.page) || 1,
        limit: Number(refundListFilters?.limit) || REFUND_LIST_PAGE_SIZE,
      });
      setListResult(mapRefundApprovalQueueResponse(raw));
      setListStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus: setListStatus,
        setErrorMessage: setListErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_REFUND,
        actionLabel: "xem danh sách duyệt hoàn tiền",
        fallbackMessage: "Không tải được danh sách duyệt hoàn tiền.",
      });
    }
  }, [canReadRefund, refundListFilters, showSessionExpired]);

  const refreshAll = useCallback(() => {
    fetchList();
    refetchStats();
    if (refundRequestId) refetchDetail();
  }, [fetchList, refundRequestId, refetchDetail, refetchStats]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const applyFiltersPatch = useCallback(
    (patch) => {
      onFiltersChange?.({
        ...refundListFilters,
        ...patch,
      });
    },
    [onFiltersChange, refundListFilters],
  );

  const handleApplyFilters = (event) => {
    event.preventDefault();
    applyFiltersPatch({
      ...draftFilters,
      page: "1",
      limit: String(filterSize),
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      q: "",
      status: REFUND_DEFAULT_STATUS_FILTER,
      requested_by: "",
      payment_method: "",
      from: "",
      to: "",
      page: "1",
      limit: String(REFUND_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildRefundSupportQuickFilter(preset);
    setDraftFilters({
      q: next.q,
      status: preset === "all" ? "" : next.status || REFUND_DEFAULT_STATUS_FILTER,
      requested_by: next.requested_by,
      payment_method: next.payment_method,
      from: next.from,
      to: next.to,
    });
    applyFiltersPatch({
      ...next,
      status: preset === "all" ? "" : next.status,
      limit: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeRefundSupportFilterChip(refundListFilters, chipKey);
    setDraftFilters({
      q: next.q || "",
      status: chipKey === "status" ? "" : (next.status ?? REFUND_DEFAULT_STATUS_FILTER),
      requested_by: next.requested_by || "",
      payment_method: next.payment_method || "",
      from: next.from || "",
      to: next.to || "",
    });
    applyFiltersPatch({
      ...next,
      limit: String(filterSize),
    });
  };

  const handleRefundSelect = (nextRefundId) => {
    if (!nextRefundId) return;
    if (nextRefundId === refundRequestId) {
      onRefundSelectionChange?.({ refundRequestId: null, refundView: null });
      return;
    }
    onRefundSelectionChange?.({
      refundRequestId: nextRefundId,
      refundView: REFUND_SUPPORT_VIEW_MODES.SUMMARY,
    });
  };

  const handleLookupSubmit = (event) => {
    event.preventDefault();
    const trimmed = lookupValue.trim();
    if (!trimmed) {
      onRefundSelectionChange?.({ refundRequestId: null, refundView: null });
      setLookupError("");
      return;
    }
    if (!isValidUuid(trimmed)) {
      setLookupError("UUID không hợp lệ.");
      return;
    }
    setLookupError("");
    onRefundSelectionChange?.({
      refundRequestId: trimmed,
      refundView: REFUND_SUPPORT_VIEW_MODES.SUMMARY,
    });
  };

  const openActionDialog = (targetId, action) => {
    if (!canApproveRefund) return;
    setActionDialog({ open: true, action, targetId });
  };

  const runAction = useCallback(
    async (adminNote = "") => {
      const { targetId, action } = actionDialog;
      if (!targetId || !action) return;

      setActionId(targetId);
      setActionDialog({ open: false, action: "", targetId: "" });

      try {
        if (action === "confirm") {
          await confirmAdminRefundApproval(targetId, adminNote);
          onNotify?.({ variant: "success", message: "Đã xác nhận hoàn tiền." });
        }
        if (action === "reject") {
          await rejectAdminRefundApproval(targetId, adminNote);
          onNotify?.({ variant: "success", message: "Đã từ chối yêu cầu hoàn tiền." });
        }
        await refreshAll();
        if (refundRequestId === targetId && action === "reject") {
          onRefundSelectionChange?.({ refundRequestId: targetId, refundView: REFUND_SUPPORT_VIEW_MODES.SUMMARY });
        }
      } catch (error) {
        onNotify?.({ variant: "error", message: error?.message || "Thao tác thất bại." });
      } finally {
        setActionId("");
      }
    },
    [actionDialog, onNotify, onRefundSelectionChange, refreshAll, refundRequestId],
  );

  const currentPage = filterPage || listResult?.pagination?.page || 1;
  const totalPages = listResult?.pagination?.totalPages || 1;
  const dialogItem = listResult?.items?.find((item) => item.id === actionDialog.targetId) || detail;

  return (
    <div className="w-full">
      <RefundSupportListView
        canReadRefund={canReadRefund}
        canApproveRefund={canApproveRefund}
        listStatus={listStatus}
        listErrorMessage={listErrorMessage}
        listResult={listResult}
        appliedFilters={refundListFilters}
        draftFilters={draftFilters}
        onDraftFiltersChange={setDraftFilters}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveFilterChip}
        onRetryList={refreshAll}
        stats={stats}
        statsStatus={statsStatus}
        lookupValue={lookupValue}
        onLookupChange={setLookupValue}
        onLookupSubmit={handleLookupSubmit}
        lookupError={lookupError}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={String(filterSize)}
        selectedRefundId={refundRequestId}
        actionId={actionId}
        onRefundSelect={handleRefundSelect}
        onConfirm={(id) => openActionDialog(id, "confirm")}
        onReject={(id) => openActionDialog(id, "reject")}
        onPageChange={(nextPage) =>
          applyFiltersPatch({
            page: String(nextPage),
            limit: String(filterSize),
          })
        }
        onPageSizeChange={(nextSize) =>
          applyFiltersPatch({
            page: "1",
            limit: String(nextSize),
          })
        }
        onCopied={() => setToastMessage("Đã sao chép UUID.")}
        formatVndPrice={formatVndPrice}
        drawer={
          refundRequestId ? (
            <RefundSupportDrawer
              refundRequestId={refundRequestId}
              detail={detail}
              loading={detailStatus === "loading"}
              errorMessage={detailErrorMessage}
              status={detailStatus}
              refundView={refundView || REFUND_SUPPORT_VIEW_MODES.SUMMARY}
              canReadRefund={canReadRefund}
              canApproveRefund={canApproveRefund}
              actionPending={Boolean(actionId)}
              formatDateTime={formatDateTime}
              formatVndPrice={formatVndPrice}
              onClose={() => onRefundSelectionChange?.({ refundRequestId: null, refundView: null })}
              onViewChange={(nextView) =>
                onRefundSelectionChange?.({ refundRequestId, refundView: nextView })
              }
              onNavigate={onNavigate}
              onRetry={refetchDetail}
              onConfirm={() => openActionDialog(refundRequestId, "confirm")}
              onReject={() => openActionDialog(refundRequestId, "reject")}
              onCopied={() => setToastMessage("Đã sao chép UUID.")}
            />
          ) : null
        }
      />

      <RefundSupportActionDialog
        open={actionDialog.open}
        action={actionDialog.action}
        pending={Boolean(actionId)}
        itemLabel={
          dialogItem
            ? `${formatVndPrice(dialogItem.amount)} · ${dialogItem.orderId || ""}`
            : ""
        }
        onClose={() => setActionDialog({ open: false, action: "", targetId: "" })}
        onConfirm={runAction}
      />

      <FeedToast message={toastMessage} onClose={() => setToastMessage("")} />
    </div>
  );
}

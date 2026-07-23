import { useCallback, useEffect, useState } from "react";
import {
  exportWebhookLogsForSupport,
  getWebhookLogsForSupport,
} from "../api/orderSupportApi.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import { WEBHOOK_LIST_PAGE_SIZE } from "../constants/webhookSupportListConstants.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { useWebhookSupportDetail } from "../hooks/useWebhookSupportDetail.js";
import { useWebhookSupportStats } from "../hooks/useWebhookSupportStats.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import {
  buildWebhookSupportQuickFilter,
  removeWebhookSupportFilterChip,
} from "../utils/webhookSupportFilterHelpers.js";
import { datetimeLocalToIso, isoToDatetimeLocal } from "../utils/webhookSupportDateUtils.js";
import { WebhookSupportListView } from "./WebhookSupportListView.jsx";
import { WebhookSupportDrawer } from "./WebhookSupportDrawer.jsx";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";

function toApiFilters(filters) {
  return {
    provider: filters.provider || undefined,
    reference_id: filters.reference_id || undefined,
    q: filters.q || undefined,
    event_type: filters.event_type || undefined,
    status: filters.status || undefined,
    from: datetimeLocalToIso(filters.from) || filters.from || undefined,
    to: datetimeLocalToIso(filters.to) || filters.to || undefined,
    page: Number(filters.page) || 1,
    size: Number(filters.size) || WEBHOOK_LIST_PAGE_SIZE,
  };
}

export function WebhookSupportListPanel({
  webhookFilters,
  onFiltersChange,
  webhookLogId,
  webhookLogProvider,
  onWebhookSelectionChange,
  onNavigate,
  onNotify,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadWebhook } = useOrderSupportPermissions();
  const [listResult, setListResult] = useState(null);
  const [listStatus, setListStatus] = useState("idle");
  const [listErrorMessage, setListErrorMessage] = useState("");
  const [exportPending, setExportPending] = useState(false);
  const [toastMessage, setToastMessage] = useState("");

  const [draftFilters, setDraftFilters] = useState({
    provider: webhookFilters?.provider || "",
    reference_id: webhookFilters?.reference_id || "",
    q: webhookFilters?.q || "",
    event_type: webhookFilters?.event_type || "",
    status: webhookFilters?.status || "",
    from: isoToDatetimeLocal(webhookFilters?.from || ""),
    to: isoToDatetimeLocal(webhookFilters?.to || ""),
  });

  const { stats, status: statsStatus } = useWebhookSupportStats({
    enabled: canReadWebhook,
    webhookFilters,
  });

  const selectedProvider =
    webhookLogProvider || listResult?.logs?.find((log) => log.log_id === webhookLogId)?.provider || "";

  const {
    detail,
    status: detailStatus,
    errorMessage: detailErrorMessage,
    refetch: refetchDetail,
  } = useWebhookSupportDetail(webhookLogId, selectedProvider, { enabled: canReadWebhook && Boolean(webhookLogId) });

  useEffect(() => {
    setDraftFilters({
      provider: webhookFilters?.provider || "",
      reference_id: webhookFilters?.reference_id || "",
      q: webhookFilters?.q || "",
      event_type: webhookFilters?.event_type || "",
      status: webhookFilters?.status || "",
      from: isoToDatetimeLocal(webhookFilters?.from || ""),
      to: isoToDatetimeLocal(webhookFilters?.to || ""),
    });
  }, [webhookFilters]);

  const filterSignature = JSON.stringify(webhookFilters || {});

  const fetchList = useCallback(async () => {
    if (!canReadWebhook) {
      setListStatus("idle");
      return;
    }

    setListStatus("loading");
    setListErrorMessage("");

    try {
      const data = await getWebhookLogsForSupport(toApiFilters(webhookFilters || {}));
      setListResult(data);
      setListStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus: setListStatus,
        setErrorMessage: setListErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_WEBHOOK,
        actionLabel: "xem nhật ký webhook",
        fallbackMessage: "Không tải được nhật ký webhook.",
      });
    }
  }, [filterSignature, canReadWebhook, showSessionExpired, webhookFilters]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const handleApplyFilters = (event) => {
    event?.preventDefault?.();
    onFiltersChange?.({
      ...draftFilters,
      from: draftFilters.from ? datetimeLocalToIso(draftFilters.from) || draftFilters.from : "",
      to: draftFilters.to ? datetimeLocalToIso(draftFilters.to) || draftFilters.to : "",
      page: 1,
      size: webhookFilters?.size || WEBHOOK_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      provider: "",
      reference_id: "",
      q: "",
      event_type: "",
      status: "",
      from: "",
      to: "",
      page: 1,
      size: webhookFilters?.size || WEBHOOK_LIST_PAGE_SIZE,
    };
    setDraftFilters({ ...cleared, from: "", to: "" });
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (presetId) => {
    const next = buildWebhookSupportQuickFilter(presetId, webhookFilters);
    setDraftFilters({
      provider: next.provider || "",
      reference_id: next.reference_id || "",
      q: next.q || "",
      event_type: next.event_type || "",
      status: next.status || "",
      from: isoToDatetimeLocal(next.from || ""),
      to: isoToDatetimeLocal(next.to || ""),
    });
    onFiltersChange?.(next);
  };

  const handleRemoveChip = (chipKey) => {
    const next = removeWebhookSupportFilterChip(webhookFilters, chipKey);
    onFiltersChange?.(next);
  };

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...webhookFilters,
      page: nextPage,
    });
  };

  const handlePageSizeChange = (nextSize) => {
    onFiltersChange?.({
      ...webhookFilters,
      page: 1,
      size: nextSize,
    });
  };

  const handleLogSelect = (log) => {
    if (!log?.log_id) return;
    onWebhookSelectionChange?.({
      webhookLogId: log.log_id,
      webhookLogProvider: log.provider,
    });
  };

  const handleDrawerClose = () => {
    onWebhookSelectionChange?.({ webhookLogId: "", webhookLogProvider: "" });
  };

  const handleExport = async () => {
    setExportPending(true);
    try {
      const blob = await exportWebhookLogsForSupport(toApiFilters(webhookFilters || {}));
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = "webhook-logs.csv";
      anchor.click();
      URL.revokeObjectURL(url);
      setToastMessage("Đã xuất CSV.");
    } catch {
      onNotify?.("Không xuất được CSV. Vui lòng thử lại.", "error");
    } finally {
      setExportPending(false);
    }
  };

  const currentPage = Number(webhookFilters?.page) || 1;
  const pageSize = Number(webhookFilters?.size) || WEBHOOK_LIST_PAGE_SIZE;
  const totalPages = listResult?.total_pages || 0;

  const drawerDetail = detail || listResult?.logs?.find((log) => log.log_id === webhookLogId) || null;

  return (
    <>
      <WebhookSupportListView
        canReadWebhook={canReadWebhook}
        listStatus={listStatus}
        listErrorMessage={listErrorMessage}
        listResult={listResult}
        appliedFilters={webhookFilters}
        draftFilters={draftFilters}
        onDraftFiltersChange={setDraftFilters}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveChip}
        onRetryList={fetchList}
        stats={stats}
        statsStatus={statsStatus}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={pageSize}
        selectedLogId={webhookLogId}
        onLogSelect={handleLogSelect}
        onPageChange={handlePageChange}
        onPageSizeChange={handlePageSizeChange}
        onExport={handleExport}
        exportPending={exportPending}
        formatDateTime={formatDateTime}
      />

      <WebhookSupportDrawer
        webhookLogId={webhookLogId}
        detail={drawerDetail}
        loading={detailStatus === "loading"}
        errorMessage={detailErrorMessage}
        status={detailStatus}
        canReadWebhook={canReadWebhook}
        formatDateTime={formatDateTime}
        onClose={handleDrawerClose}
        onRetry={refetchDetail}
        onNavigate={onNavigate}
        onCopied={() => setToastMessage("Đã sao chép.")}
      />

      {toastMessage ? (
        <div className="fixed bottom-4 right-4 z-[120] rounded-lg bg-admin-surface px-4 py-2 text-sm shadow-lg">
          {toastMessage}
        </div>
      ) : null}
    </>
  );
}

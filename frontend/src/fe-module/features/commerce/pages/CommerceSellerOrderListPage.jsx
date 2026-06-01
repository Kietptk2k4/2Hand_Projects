import { useCallback, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { SellerOrderListEmptyState } from "../components/SellerOrderListEmptyState";
import { SellerOrderListHeader } from "../components/SellerOrderListHeader";
import { SellerOrderListSkeleton } from "../components/SellerOrderListSkeleton";
import { SellerOrderPagination } from "../components/SellerOrderPagination";
import { SellerOrderProcessConfirmDialog } from "../components/SellerOrderProcessConfirmDialog";
import { SellerOrderShipmentFilter } from "../components/SellerOrderShipmentFilter";
import { SellerOrderStatusTabs } from "../components/SellerOrderStatusTabs";
import { SellerOrderTable } from "../components/SellerOrderTable";
import { buildProcessSuccessToast } from "../constants/sellerOrderConstants";
import { useProcessSellerOrderItems } from "../hooks/useProcessSellerOrderItems";
import { useSellerOrderList } from "../hooks/useSellerOrderList";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceSellerOrderListPage() {
  const [toastMessage, setToastMessage] = useState("");
  const [selectedIds, setSelectedIds] = useState(() => new Set());
  const [confirmItems, setConfirmItems] = useState(null);

  const {
    filteredItems,
    activeTabId,
    changeStatusFilter,
    shipmentStatusFilter,
    changeShipmentFilter,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    noShop,
    isEmpty,
    isFilterEmpty,
    isSearchEmpty,
    goToPage,
    retry,
    clientSearch,
    setClientSearch,
    pendingCount,
  } = useSellerOrderList();

  const handleProcessSuccess = useCallback(
    (result) => {
      setSelectedIds(new Set());
      setConfirmItems(null);
      retry();

      if (activeTabId === "pending" && result.newlyProcessedCount > 0) {
        changeStatusFilter("PROCESSING");
      }

      setToastMessage(
        buildProcessSuccessToast({
          newlyProcessedCount: result.newlyProcessedCount,
          alreadyProcessingCount: result.alreadyProcessingCount,
        }),
      );
    },
    [activeTabId, changeStatusFilter, retry],
  );

  const { isProcessing, processError, process, clearError } = useProcessSellerOrderItems({
    onSuccess: handleProcessSuccess,
  });

  const pendingSelectedCount = useMemo(() => {
    return [...selectedIds].filter((id) =>
      filteredItems.some((item) => item.orderItemId === id && item.itemStatus === "PENDING"),
    ).length;
  }, [filteredItems, selectedIds]);

  const bulkLabel =
    pendingSelectedCount > 0
      ? `Xác nhận chuẩn bị hàng (${pendingSelectedCount})`
      : "Xác nhận chuẩn bị hàng";

  const showComingSoon = useCallback((message) => {
    setToastMessage(message || "Tính năng đang được phát triển.");
  }, []);

  const handleToggleSelect = useCallback((orderItemId) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(orderItemId)) next.delete(orderItemId);
      else next.add(orderItemId);
      return next;
    });
  }, []);

  const handleToggleSelectAllPending = useCallback((pendingIds, selectAll) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (selectAll) {
        pendingIds.forEach((id) => next.add(id));
      } else {
        pendingIds.forEach((id) => next.delete(id));
      }
      return next;
    });
  }, []);

  const openConfirmForItems = useCallback(
    (items) => {
      const pending = items.filter((item) => item.itemStatus === "PENDING");
      if (pending.length === 0) return;
      clearError();
      setConfirmItems(pending);
    },
    [clearError],
  );

  const handleBulkPrepare = useCallback(() => {
    const pending = filteredItems.filter(
      (item) => selectedIds.has(item.orderItemId) && item.itemStatus === "PENDING",
    );
    openConfirmForItems(pending);
  }, [filteredItems, openConfirmForItems, selectedIds]);

  const handlePrepareRow = useCallback(
    (item) => {
      openConfirmForItems([item]);
    },
    [openConfirmForItems],
  );

  const handleConfirmProcess = useCallback(async () => {
    if (!confirmItems?.length) return;
    const ids = confirmItems.map((item) => item.orderItemId);
    await process(ids);
  }, [confirmItems, process]);

  const handleCancelConfirm = useCallback(() => {
    if (isProcessing) return;
    setConfirmItems(null);
    clearError();
  }, [clearError, isProcessing]);

  const disabled = isLoading || noShop || isProcessing;

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mx-auto w-full max-w-[1280px]">
        <SellerOrderListHeader
          totalItems={totalItems}
          clientSearch={clientSearch}
          onSearchChange={setClientSearch}
          onBulkPrepare={handleBulkPrepare}
          bulkDisabled={disabled || pendingSelectedCount === 0}
          bulkLabel={bulkLabel}
          isProcessing={isProcessing}
          searchDisabled={disabled}
        />

        {noShop ? (
          <div className="mb-6 rounded-xl border border-outline-variant bg-surface-container-high p-6">
            <p className="text-body-md text-on-surface">
              Bạn chưa có cửa hàng. Tạo shop để nhận và quản lý đơn bán.
            </p>
            <Link
              to={APP_ROUTES.commerceCreateShop}
              className="mt-4 inline-flex rounded-lg bg-primary px-5 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Tạo shop
            </Link>
          </div>
        ) : null}

        <SellerOrderStatusTabs
          activeTabId={activeTabId}
          pendingCount={pendingCount}
          onChange={changeStatusFilter}
          disabled={disabled}
        />

        <SellerOrderShipmentFilter
          value={shipmentStatusFilter || ""}
          onChange={changeShipmentFilter}
          disabled={disabled}
        />

        {isLoading ? <SellerOrderListSkeleton /> : null}

        {!isLoading && errorMessage ? (
          <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
            <p className="text-body-md text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-lg bg-primary px-4 py-2 text-label-md text-on-primary"
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {!isLoading && !errorMessage && !noShop && (isEmpty || isFilterEmpty || isSearchEmpty) ? (
          <SellerOrderListEmptyState
            variant={isSearchEmpty ? "search" : isFilterEmpty ? "filter" : "none"}
          />
        ) : null}

        {!isLoading && !errorMessage && !noShop && filteredItems.length > 0 ? (
          <>
            <SellerOrderTable
              items={filteredItems}
              disabled={disabled}
              selectedIds={selectedIds}
              isProcessing={isProcessing}
              onToggleSelect={handleToggleSelect}
              onToggleSelectAllPending={handleToggleSelectAllPending}
              onPrepareRow={handlePrepareRow}
            />
            <SellerOrderPagination
              page={page}
              totalPages={totalPages}
              rangeStart={rangeStart}
              rangeEnd={rangeEnd}
              totalItems={totalItems}
              disabled={disabled}
              onPrev={() => goToPage(page - 1)}
              onNext={() => goToPage(page + 1)}
              onGoToPage={goToPage}
            />
          </>
        ) : null}
      </div>

      <SellerOrderProcessConfirmDialog
        open={Boolean(confirmItems?.length)}
        items={confirmItems || []}
        isProcessing={isProcessing}
        errorMessage={processError}
        onCancel={handleCancelConfirm}
        onConfirm={handleConfirmProcess}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}

import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { SellerOrderListEmptyState } from "../components/SellerOrderListEmptyState";
import { SellerOrderListHeader } from "../components/SellerOrderListHeader";
import { SellerOrderListSkeleton } from "../components/SellerOrderListSkeleton";
import { SellerOrderPagination } from "../components/SellerOrderPagination";
import { SellerOrderShipmentFilter } from "../components/SellerOrderShipmentFilter";
import { SellerOrderStatusTabs } from "../components/SellerOrderStatusTabs";
import { SellerOrderTable } from "../components/SellerOrderTable";
import { useSellerOrderList } from "../hooks/useSellerOrderList";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceSellerOrderListPage() {
  const [toastMessage, setToastMessage] = useState("");
  const [selectedIds, setSelectedIds] = useState(() => new Set());

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
  } = useSellerOrderList();

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

  const handleBulkPrepare = useCallback(() => {
    showComingSoon("Xác nhận chuẩn bị hàng sẽ có trong bản cập nhật (ProcessSellerOrderItem).");
  }, [showComingSoon]);

  const disabled = isLoading || noShop;

  return (
    <CommerceShell showHomeSidebar={false} onComingSoon={showComingSoon}>
      <div className="mx-auto w-full max-w-[1280px]">
        <SellerOrderListHeader
          totalItems={totalItems}
          clientSearch={clientSearch}
          onSearchChange={setClientSearch}
          onBulkPrepare={handleBulkPrepare}
          bulkDisabled={disabled || selectedIds.size === 0}
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
              onToggleSelect={handleToggleSelect}
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

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}

import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { OrderListCard } from "../components/OrderListCard";
import { OrderListEmptyState } from "../components/OrderListEmptyState";
import { OrderListFilters } from "../components/OrderListFilters";
import { OrderListSkeleton } from "../components/OrderListSkeleton";
import { useOrderList } from "../hooks/useOrderList";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceOrderListPage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");

  const {
    orders,
    activeFilterId,
    changeStatusFilter,
    isInitialLoading,
    isLoadingMore,
    isEmpty,
    hasNext,
    totalItems,
    errorMessage,
    loadMore,
    retry,
  } = useOrderList();

  const showComingSoon = useCallback((message) => {
    setToastMessage(message || "Tính năng đang được phát triển.");
  }, []);

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const handleOrderClick = useCallback(
    (targetOrderId) => {
      if (!targetOrderId) return;
      navigate(APP_ROUTES.commerceOrderDetail.replace(":orderId", targetOrderId));
    },
    [navigate]
  );

  const handleStatusFilterChange = useCallback(
    (nextStatus) => {
      changeStatusFilter(nextStatus);
    },
    [changeStatusFilter]
  );

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mx-auto w-full max-w-[1280px]">
        <header className="mb-6">
          <h1 className="text-headline-md font-bold text-on-surface">Đơn hàng của tôi</h1>
          <p className="mt-1 text-body-sm text-on-surface-variant">
            Theo dõi và quản lý đơn hàng
            {totalItems > 0 ? (
              <span className="text-on-surface">
                {" "}
                · {totalItems} đơn
              </span>
            ) : null}
          </p>
        </header>

        <div className="mb-6">
          <OrderListFilters
            activeFilterId={activeFilterId}
            onChange={handleStatusFilterChange}
            disabled={isInitialLoading}
          />
        </div>

        {isInitialLoading ? <OrderListSkeleton /> : null}

        {!isInitialLoading && errorMessage && orders.length === 0 ? (
          <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
            <p className="text-sm text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {!isInitialLoading && !errorMessage && isEmpty ? <OrderListEmptyState /> : null}

        {!isInitialLoading && !errorMessage && !isEmpty ? (
          <div className="flex flex-col gap-4">
            {orders.map((order) => (
              <OrderListCard
                key={order.orderId}
                order={order}
                onOrderClick={handleOrderClick}
              />
            ))}
          </div>
        ) : null}

        {hasNext && !isInitialLoading && !errorMessage ? (
          <div className="mt-8 flex justify-center">
            {isLoadingMore ? (
              <div
                className="h-8 w-8 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                aria-label="Đang tải thêm"
              />
            ) : (
              <button
                type="button"
                onClick={loadMore}
                className="rounded-md border-2 border-primary px-8 py-3 text-label-md font-bold text-primary transition-colors hover:bg-primary hover:text-on-primary"
              >
                Xem thêm
              </button>
            )}
          </div>
        ) : null}
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}

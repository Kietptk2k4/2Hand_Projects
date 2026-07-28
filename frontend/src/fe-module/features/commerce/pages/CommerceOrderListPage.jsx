import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { OrderListCard } from "../components/OrderListCard";
import { OrderListEmptyState } from "../components/OrderListEmptyState";
import { OrderListFilters } from "../components/OrderListFilters";
import { OrderListSkeleton } from "../components/OrderListSkeleton";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { useOrderList } from "../hooks/useOrderList";
import { fetchShopProducts } from "../api/shopProductsApi";
import { mapShopProductsResponse } from "../utils/shopProductsMapper";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceOrderListPage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [shopNamesMap, setShopNamesMap] = useState({});

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

  // Dynamically resolve real shop names for all orders in list
  useEffect(() => {
    if (!orders?.length) return;

    const shopIds = Array.from(
      new Set(orders.map((order) => order.shopId || order.sellerId).filter(Boolean))
    );

    shopIds.forEach((shopId) => {
      const existing = orders.find(
        (o) => (o.shopId === shopId || o.sellerId === shopId) && o.shopName && !o.shopName.startsWith("Shop ")
      );
      if (existing?.shopName) {
        setShopNamesMap((prev) => ({ ...prev, [shopId]: existing.shopName }));
        return;
      }

      fetchShopProducts({ shopId, limit: 1 })
        .then((raw) => {
          const mapped = mapShopProductsResponse(raw);
          if (mapped?.shop?.shopName) {
            setShopNamesMap((prev) => ({
              ...prev,
              [shopId]: mapped.shop.shopName,
            }));
          }
        })
        .catch(() => {});
    });
  }, [orders]);

  const filteredOrders = useMemo(() => {
    if (!searchQuery.trim()) return orders;
    const q = searchQuery.toLowerCase().trim();
    return orders.filter(
      (order) =>
        order.orderId?.toLowerCase().includes(q) ||
        order.previewProductName?.toLowerCase().includes(q) ||
        (shopNamesMap[order.shopId] || order.shopName)?.toLowerCase().includes(q)
    );
  }, [orders, searchQuery, shopNamesMap]);

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
        {/* Header */}
        <header className="mb-6 border-b border-outline-variant/60 pb-4">
          <h1 className="text-xl font-black text-on-surface sm:text-2xl">
            ĐƠN HÀNG CỦA TÔI
          </h1>
          <p className="mt-1 text-xs font-semibold text-on-surface-variant">
            Theo dõi và quản lý các đơn hàng đã đặt
            {totalItems > 0 ? ` · ${totalItems} đơn` : null}
          </p>
        </header>

        {/* Search & Status Filters */}
        <div className="mb-6 space-y-4">
          {/* Search bar */}
          <div className="relative">
            <span className="material-symbols-outlined absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-lg">
              search
            </span>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Tìm đơn hàng theo Mã đơn, Tên sản phẩm hoặc Tên shop..."
              className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-3 pl-10 pr-10 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm"
            />
            {searchQuery ? (
              <button
                type="button"
                onClick={() => setSearchQuery("")}
                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
              >
                <span className="material-symbols-outlined text-base">close</span>
              </button>
            ) : null}
          </div>

          {/* Status Tabs */}
          <OrderListFilters
            activeFilterId={activeFilterId}
            onChange={handleStatusFilterChange}
            disabled={isInitialLoading}
          />
        </div>

        {isInitialLoading ? <OrderListSkeleton /> : null}

        {!isInitialLoading && errorMessage && orders.length === 0 ? (
          <div className="rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center shadow-xs">
            <p className="text-sm font-semibold text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại ngay
            </button>
          </div>
        ) : null}

        {!isInitialLoading && !errorMessage && (isEmpty || filteredOrders.length === 0) ? (
          <OrderListEmptyState />
        ) : null}

        {!isInitialLoading && !errorMessage && filteredOrders.length > 0 ? (
          <div className="flex flex-col gap-4">
            {filteredOrders.map((order) => (
              <OrderListCard
                key={order.orderId}
                order={order}
                shopNamesMap={shopNamesMap}
                onOrderClick={handleOrderClick}
              />
            ))}
          </div>
        ) : null}

        {hasNext && !isInitialLoading && !errorMessage ? (
          <div className="mt-8 flex justify-center">
            {isLoadingMore ? (
              <div
                className="h-8 w-8 animate-spin rounded-full border-4 border-primary/20 border-t-primary"
                aria-label="Đang tải thêm"
              />
            ) : (
              <button
                type="button"
                onClick={loadMore}
                className="rounded-xl border-2 border-primary px-8 py-3 text-xs font-bold text-primary transition-all hover:bg-primary hover:text-on-primary shadow-xs cursor-pointer"
              >
                Xem thêm đơn hàng
              </button>
            )}
          </div>
        ) : null}

        {/* E-Commerce Trust Badges Footer */}
        <CommerceFooterTrustSection />
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}

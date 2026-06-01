import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import { CommerceShell } from "../components/CommerceShell";
import { OrderDetailItemsSection } from "../components/OrderDetailItemsSection";
import { OrderDetailPaymentSummary } from "../components/OrderDetailPaymentSummary";
import { OrderDetailShipmentsSection } from "../components/OrderDetailShipmentsSection";
import { OrderDetailShippingAddress } from "../components/OrderDetailShippingAddress";
import { OrderDetailSkeleton } from "../components/OrderDetailSkeleton";
import { OrderDetailTimeline } from "../components/OrderDetailTimeline";
import {
  ORDER_STATUS_BADGE_CLASS,
  ORDER_STATUS_LABELS,
} from "../constants/orderListConstants";
import { useOrderDetail } from "../hooks/useOrderDetail";
import { useOrderTrackStatus } from "../hooks/useOrderTrackStatus";
import { formatOrderDate, formatShortOrderId } from "../utils/formatOrderDate";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceOrderDetailPage() {
  const { orderId } = useParams();

  const { order, isLoading, isNotFound, isError, errorMessage, retry } = useOrderDetail(orderId);

  const {
    timelineEvents,
    isLoading: isTrackLoading,
    isRefreshing,
    errorMessage: trackErrorMessage,
    refresh: refreshTrack,
  } = useOrderTrackStatus(orderId, {
    enabled: Boolean(order) && !isNotFound,
    pollWhileProcessing: true,
  });

  const showPayNow =
    order?.orderStatus === "AWAITING_PAYMENT" &&
    order?.paymentMethod === "PAYOS" &&
    order?.payment?.paymentId;

  const shippingAddress = order?.shipments?.[0]?.shippingAddress;

  const statusLabel = order ? ORDER_STATUS_LABELS[order.orderStatus] || order.orderStatus : "";
  const statusClass =
    ORDER_STATUS_BADGE_CLASS[order?.orderStatus] ||
    "bg-surface-container-high text-on-surface-variant";

  const handleRefresh = useCallback(() => {
    refreshTrack();
  }, [refreshTrack]);

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[1280px]">
        {isLoading ? <OrderDetailSkeleton /> : null}

        {isNotFound ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              receipt_long
            </span>
            <p className="text-sm text-on-surface-variant">
              {errorMessage || "Không tìm thấy đơn hàng."}
            </p>
            <Link
              to={APP_ROUTES.commerceOrders}
              className="mt-4 inline-block rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Quay lại danh sách đơn
            </Link>
          </div>
        ) : null}

        {!isLoading && isError ? (
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

        {!isLoading && !isNotFound && !isError && order ? (
          <>
            <header className="mb-6">
              <Link
                to={APP_ROUTES.commerceOrders}
                className="mb-3 inline-flex items-center text-label-md text-on-surface-variant transition-colors hover:text-primary"
              >
                <span className="material-symbols-outlined mr-1 text-[20px]" aria-hidden="true">
                  arrow_back
                </span>
                Quay lại đơn hàng
              </Link>

              <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div>
                  <div className="flex flex-wrap items-center gap-3">
                    <h1 className="text-headline-md font-bold text-on-surface">Chi tiết đơn hàng</h1>
                    <span className={`rounded-full px-2.5 py-0.5 text-label-sm ${statusClass}`}>
                      {statusLabel}
                    </span>
                  </div>
                  <p className="mt-1 text-body-sm text-on-surface-variant">
                    {formatShortOrderId(order.orderId)} · Đặt lúc {formatOrderDate(order.createdAt)}
                  </p>
                </div>

                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    onClick={handleRefresh}
                    disabled={isTrackLoading || isRefreshing}
                    className="rounded-lg border border-outline-variant px-4 py-2 text-label-md text-on-surface hover:bg-surface-container-low disabled:opacity-60"
                  >
                    {isRefreshing ? "Đang làm mới..." : "Làm mới"}
                  </button>
                  {showPayNow ? (
                    <Link
                      to={`${APP_ROUTES.commerceCheckoutPaymentResult}?paymentId=${order.payment.paymentId}`}
                      className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
                    >
                      Thanh toán ngay
                    </Link>
                  ) : null}
                </div>
              </div>

              {trackErrorMessage ? (
                <p className="mt-2 text-body-sm text-error">{trackErrorMessage}</p>
              ) : null}
            </header>

            <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-12">
              <div className="flex flex-col gap-6 lg:col-span-8">
                <OrderDetailItemsSection orderId={order.orderId} items={order.items} />
                <OrderDetailShipmentsSection orderId={order.orderId} shipments={order.shipments} />
              </div>

              <aside className="flex flex-col gap-6 lg:col-span-4 lg:sticky lg:top-20">
                <OrderDetailTimeline
                  events={timelineEvents}
                  isLoading={isTrackLoading && !timelineEvents.length}
                />
                <OrderDetailPaymentSummary order={order} shipments={order.shipments} />
                <OrderDetailShippingAddress address={shippingAddress} />
              </aside>
            </div>
          </>
        ) : null}
      </div>
    </CommerceShell>
  );
}

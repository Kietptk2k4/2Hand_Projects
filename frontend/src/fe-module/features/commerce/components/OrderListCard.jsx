import { Link } from "react-router-dom";
import { formatVndPrice } from "../../social/utils/formatPrice";
import {
  ORDER_PAYMENT_STATUS_LABELS,
  ORDER_STATUS_BADGE_CLASS,
  ORDER_STATUS_LABELS,
  PAYMENT_METHOD_LABELS,
  PAYMENT_STATUS_BADGE_CLASS,
} from "../constants/orderListConstants";
import { formatOrderDate, formatShortOrderId } from "../utils/formatOrderDate";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { ONLINE_PAYMENT_METHODS } from "../constants/checkoutConstants";
import { RetryVnpayPaymentButton } from "./RetryVnpayPaymentButton";

function ShipmentHint({ shipmentSummary }) {
  if (!shipmentSummary?.shipmentCount) return null;

  const statusLabel = shipmentSummary.statuses?.[0] || "Đang giao";
  return (
    <p className="mt-2 text-xs text-on-surface-variant">
      {shipmentSummary.shipmentCount} lô hàng · {statusLabel}
    </p>
  );
}

export function OrderListCard({ order, onOrderClick, onPayNow }) {
  const statusLabel = ORDER_STATUS_LABELS[order.orderStatus] || order.orderStatus;
  const statusClass =
    ORDER_STATUS_BADGE_CLASS[order.orderStatus] || "bg-surface-container-high text-on-surface-variant";

  const paymentStatusLabel =
    ORDER_PAYMENT_STATUS_LABELS[order.orderPaymentStatus] || order.orderPaymentStatus;
  const paymentStatusClass =
    PAYMENT_STATUS_BADGE_CLASS[order.orderPaymentStatus] ||
    "bg-surface-container-high text-on-surface-variant";

  const paymentMethodLabel =
    PAYMENT_METHOD_LABELS[order.paymentMethod] || order.paymentMethod;

  const extraCount = order.itemCount > 1 ? order.itemCount - 1 : 0;
  const showPayNow =
    order.orderStatus === "AWAITING_PAYMENT" &&
    ONLINE_PAYMENT_METHODS.has(order.paymentMethod) &&
    order.payment?.paymentId;
  const isVnpay = order.paymentMethod === "VNPAY";

  const handleCardClick = () => {
    onOrderClick?.(order.orderId);
  };

  const handlePayClick = (event) => {
    event.stopPropagation();
    onPayNow?.(order);
  };

  const handleDetailClick = (event) => {
    event.stopPropagation();
    onOrderClick?.(order.orderId);
  };

  return (
    <article className="rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm transition-shadow hover:shadow-md">
      <button
        type="button"
        onClick={handleCardClick}
        className="w-full rounded-xl p-4 text-left"
      >
        <div className="flex flex-wrap items-center justify-between gap-2 text-body-sm text-on-surface-variant">
          <span className="font-mono font-medium text-on-surface">
            {formatShortOrderId(order.orderId)}
          </span>
          <time dateTime={order.createdAt}>{formatOrderDate(order.createdAt)}</time>
        </div>

        <div className="mt-3 flex flex-wrap items-center gap-2">
          <span className={`rounded-full px-2.5 py-0.5 text-label-sm ${statusClass}`}>
            {statusLabel}
          </span>
          <span className={`rounded-full px-2.5 py-0.5 text-label-sm ${paymentStatusClass}`}>
            {paymentStatusLabel}
          </span>
          {paymentMethodLabel ? (
            <span className="text-label-sm text-on-surface-variant">{paymentMethodLabel}</span>
          ) : null}
          {order.pendingReview ? (
            <span className="rounded-full bg-amber-100 px-2.5 py-0.5 text-label-sm text-amber-900">
              Chờ đánh giá
            </span>
          ) : null}
        </div>

        <div className="mt-4 flex gap-3">
          <div className="h-16 w-16 shrink-0 overflow-hidden rounded-lg bg-surface-container-high">
            {order.previewImageUrl ? (
              <img
                src={order.previewImageUrl}
                alt=""
                className="h-full w-full object-cover"
                loading="lazy"
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center">
                <span className="material-symbols-outlined text-2xl text-outline" aria-hidden="true">
                  inventory_2
                </span>
              </div>
            )}
          </div>

          <div className="min-w-0 flex-1">
            <p className="line-clamp-2 text-body-sm font-medium text-on-surface">
              {order.previewProductName || "Sản phẩm"}
              {extraCount > 0 ? (
                <span className="font-normal text-on-surface-variant">
                  {" "}
                  và {extraCount} sản phẩm khác
                </span>
              ) : null}
            </p>
            <ShipmentHint shipmentSummary={order.shipmentSummary} />
          </div>

          <p className="shrink-0 text-headline-sm font-semibold text-primary">
            {formatVndPrice(order.finalAmount)}
          </p>
        </div>
      </button>

      <div className="flex flex-wrap items-center justify-end gap-2 border-t border-outline-variant/60 px-4 py-3">
        {showPayNow && isVnpay ? (
          <RetryVnpayPaymentButton
            orderId={order.orderId}
            label="Thanh toán ngay"
            onClick={handlePayClick}
          />
        ) : null}
        {showPayNow && !isVnpay ? (
          <Link
            to={`${APP_ROUTES.commerceCheckoutPaymentResult}?paymentId=${order.payment.paymentId}`}
            onClick={handlePayClick}
            className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary hover:bg-[#0050cb]"
          >
            Thanh toán ngay
          </Link>
        ) : null}
        <button
          type="button"
          onClick={handleDetailClick}
          className="rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-surface-container-low"
        >
          Xem chi tiết
        </button>
      </div>
    </article>
  );
}

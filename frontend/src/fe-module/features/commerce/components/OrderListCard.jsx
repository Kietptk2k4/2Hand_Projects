import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
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
import { buildCommerceShopPath } from "../utils/commerceRoutes";
import { getDisplayShopName } from "../utils/cartDisplay";

// Fallback image pool for order preview images when missing or broken
const FALLBACK_ORDER_IMAGES = [
  "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=80",
];

function getRandomOrderFallback(orderId) {
  if (!orderId) return FALLBACK_ORDER_IMAGES[0];
  let hash = 0;
  const str = String(orderId);
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  return FALLBACK_ORDER_IMAGES[Math.abs(hash) % FALLBACK_ORDER_IMAGES.length];
}

function ShipmentHint({ shipmentSummary }) {
  if (!shipmentSummary?.shipmentCount) return null;

  const statusLabel = shipmentSummary.statuses?.[0] || "Đang giao";
  return (
    <div className="mt-1.5 flex items-center gap-1.5 text-xs text-emerald-700 font-medium">
      <span className="material-symbols-outlined text-sm">local_shipping</span>
      <span>{shipmentSummary.shipmentCount} lô hàng · {statusLabel}</span>
    </div>
  );
}

export function OrderListCard({ order, shopNamesMap = {}, onOrderClick, onPayNow }) {
  const [imgError, setImgError] = useState(false);
  const navigate = useNavigate();

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

  const shopName = getDisplayShopName(order, shopNamesMap);
  const shopId = order.shopId || order.shop_id || order.sellerId;

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

  const handleOpenShop = (event) => {
    event.stopPropagation();
    event.preventDefault();
    const targetShopId = shopId || order.shopId || order.shop_id || order.sellerId;
    if (targetShopId) {
      navigate(buildCommerceShopPath(targetShopId));
    } else {
      onOrderClick?.(order.orderId);
    }
  };

  const displayImageSrc =
    !imgError && order.previewImageUrl
      ? order.previewImageUrl
      : getRandomOrderFallback(order.orderId);

  return (
    <article className="overflow-hidden rounded-2xl border border-outline-variant/80 bg-surface-container-lowest shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md">
      {/* Shop Header Row (Shopee / E-Commerce Style) */}
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-outline-variant/50 bg-surface-container-low px-4 py-3">
        <div className="flex items-center gap-2">
          <span className="material-symbols-outlined text-lg text-primary" aria-hidden="true">
            storefront
          </span>
          <button
            type="button"
            onClick={handleOpenShop}
            className="relative z-10 flex items-center gap-1 text-xs font-bold text-on-surface hover:text-primary transition-colors cursor-pointer"
          >
            <span>{shopName}</span>
            <span className="material-symbols-outlined text-sm">chevron_right</span>
          </button>
          <span className="text-[11px] text-slate-400 font-mono">
            {formatShortOrderId(order.orderId)}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <time className="text-[11px] text-slate-400" dateTime={order.createdAt}>
            {formatOrderDate(order.createdAt)}
          </time>
          <span className={`rounded-lg px-2.5 py-0.5 text-xs font-bold ${statusClass}`}>
            {statusLabel}
          </span>
        </div>
      </div>

      {/* Main Order Content */}
      <button
        type="button"
        onClick={handleCardClick}
        className="w-full p-4 text-left cursor-pointer"
      >
        <div className="flex items-start gap-3 sm:gap-4">
          {/* Thumbnail image with fallback handler */}
          <div className="h-20 w-20 shrink-0 overflow-hidden rounded-xl border border-outline-variant/60 bg-surface-container-low">
            <img
              src={displayImageSrc}
              alt={order.previewProductName || "Sản phẩm"}
              onError={() => setImgError(true)}
              className="h-full w-full object-cover transition-transform duration-300 hover:scale-105"
              loading="lazy"
            />
          </div>

          <div className="min-w-0 flex-1">
            <p className="line-clamp-2 text-xs font-bold text-on-surface sm:text-sm">
              {order.previewProductName || "Sản phẩm 2Hand"}
            </p>

            {extraCount > 0 ? (
              <span className="mt-1 inline-block rounded-md bg-surface-container-high px-2 py-0.5 text-[10px] font-semibold text-on-surface-variant">
                + {extraCount} sản phẩm khác
              </span>
            ) : null}

            <ShipmentHint shipmentSummary={order.shipmentSummary} />

            {/* Payment status badge */}
            <div className="mt-2 flex flex-wrap items-center gap-1.5 text-[11px]">
              <span className={`rounded-md px-2 py-0.5 font-semibold ${paymentStatusClass}`}>
                {paymentStatusLabel}
              </span>
              {paymentMethodLabel ? (
                <span className="text-slate-500 font-medium">({paymentMethodLabel})</span>
              ) : null}
              {order.pendingReview ? (
                <span className="rounded-md bg-amber-100 px-2 py-0.5 font-bold text-amber-800">
                  Chờ đánh giá
                </span>
              ) : null}
            </div>
          </div>

          <div className="text-right">
            <span className="text-xs text-slate-400">Thành tiền</span>
            <p className="text-sm font-black text-red-600 sm:text-base">
              {formatVndPrice(order.finalAmount)}
            </p>
          </div>
        </div>
      </button>

      {/* Order Actions Footer */}
      <div className="flex flex-wrap items-center justify-end gap-2.5 border-t border-outline-variant/50 bg-surface-container-lowest px-4 py-3">
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
            className="rounded-xl bg-primary px-5 py-2 text-xs font-bold text-on-primary hover:bg-[#0050cb] shadow-xs active:scale-95 cursor-pointer"
          >
            Thanh toán ngay
          </Link>
        ) : null}

        <button
          type="button"
          onClick={handleDetailClick}
          className="rounded-xl border border-primary px-5 py-2 text-xs font-bold text-primary transition-all hover:bg-primary hover:text-on-primary shadow-xs active:scale-95 cursor-pointer"
        >
          Xem chi tiết
        </button>
      </div>
    </article>
  );
}

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  MANUAL_NEXT_ACTIONS,
  SHIPMENT_STATUS_BADGE_CLASS,
  SHIPMENT_STATUS_LABELS,
} from "../constants/sellerShipmentConstants";
import { formatShortOrderId } from "../utils/formatOrderDate";
import { APP_ROUTES } from "../../../shared/constants/routes";

function formatDateTime(value) {
  if (!value) return "—";
  try {
    return new Date(value).toLocaleString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return value;
  }
}

function carrierLabel(carrier) {
  if (carrier === "GHN") return "Giao Hàng Nhanh (GHN)";
  if (carrier === "MANUAL") return "Tự giao hàng";
  if (carrier === "SELF_DELIVERY") return "Tự vận chuyển";
  return carrier;
}

export function SellerShipmentCard({ item, disabled, isUpdating, onRequestStatusUpdate }) {
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const isManual = item.carrier === "MANUAL" || item.carrier === "SELF_DELIVERY";
  const tracking = item.trackingNumber || item.ghnOrderCode || "—";
  const detailPath = APP_ROUTES.commerceSellerShipmentDetail.replace(
    ":shipmentId",
    item.shipmentId,
  );

  const openDetail = () => {
    if (!item.shipmentId) return;
    navigate(detailPath);
  };

  const handleCardKeyDown = (event) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openDetail();
    }
  };

  const handleCopyTracking = (e) => {
    e.stopPropagation();
    if (!tracking || tracking === "—") return;
    navigator.clipboard?.writeText(tracking).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const badgeClass =
    SHIPMENT_STATUS_BADGE_CLASS[item.status] || "bg-surface-container-high text-on-surface";

  const nextAction = MANUAL_NEXT_ACTIONS[item.status];
  const nextActions = Array.isArray(nextAction) ? nextAction : nextAction ? [nextAction] : [];

  return (
    <article
      onClick={openDetail}
      onKeyDown={handleCardKeyDown}
      tabIndex={0}
      role="link"
      aria-label={`Xem chi tiết vận đơn ${tracking}`}
      className={[
        "cursor-pointer rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-primary",
        isManual ? "border-l-4 border-l-secondary" : "",
      ].join(" ")}
    >
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0 flex-1">
          {/* Header Row: Carrier Badge & Status Badge */}
          <div className="flex flex-wrap items-center gap-2">
            <span
              className={[
                "inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1 text-xs font-bold",
                item.carrier === "GHN"
                  ? "bg-[#ff6600]/10 text-[#c44e00] border border-[#ff6600]/20"
                  : "bg-secondary/10 text-secondary border border-secondary/20",
              ].join(" ")}
            >
              <span className="material-symbols-outlined text-sm" aria-hidden="true">
                {item.carrier === "GHN" ? "local_shipping" : "two_wheeler"}
              </span>
              {carrierLabel(item.carrier)}
            </span>

            <span
              className={[
                "rounded-lg px-3 py-1 text-xs font-bold",
                badgeClass,
              ].join(" ")}
            >
              {SHIPMENT_STATUS_LABELS[item.status] || item.status}
            </span>
          </div>

          {/* Tracking Code Chip */}
          <div className="mt-3 flex flex-wrap items-center gap-2">
            <span className="text-xs font-semibold text-on-surface-variant">Mã vận đơn:</span>
            <div className="inline-flex items-center gap-1.5 rounded-lg bg-surface-container-high px-2.5 py-1">
              <span className="font-mono text-xs font-bold text-primary">{tracking}</span>
              <button
                type="button"
                onClick={handleCopyTracking}
                className="relative z-10 text-slate-400 transition-colors hover:text-primary cursor-pointer"
                title="Sao chép mã vận đơn"
              >
                <span className="material-symbols-outlined text-sm">
                  {copied ? "check" : "content_copy"}
                </span>
              </button>
            </div>
            {copied ? (
              <span className="text-[11px] font-bold text-emerald-600">Đã sao chép!</span>
            ) : null}
          </div>

          {/* Delivery Address Summary */}
          <div className="mt-2.5 flex items-start gap-1.5 text-xs text-on-surface">
            <span className="material-symbols-outlined text-slate-400 text-sm mt-0.5" aria-hidden="true">
              location_on
            </span>
            <p className="line-clamp-2 font-medium">
              {item.deliveryAddressSummary || "Chưa có địa chỉ chi tiết"}
            </p>
          </div>

          {/* Order Meta Info */}
          <div className="mt-3 flex flex-wrap gap-4 text-xs font-semibold text-on-surface-variant">
            <span>
              Đơn hàng:{" "}
              <span className="font-mono font-bold text-on-surface">
                {formatShortOrderId(item.orderId)}
              </span>
            </span>
            <span>{item.orderItemCount} sản phẩm</span>
            <span>Tạo lúc: {formatDateTime(item.createdAt)}</span>
          </div>
        </div>

        {/* Action Buttons Right Column */}
        <div className="flex shrink-0 flex-wrap items-center gap-2">
          {isManual && nextActions.length > 0
            ? nextActions.map((action) => (
                <button
                  key={action.status}
                  type="button"
                  disabled={disabled || isUpdating}
                  onClick={(event) => {
                    event.stopPropagation();
                    onRequestStatusUpdate?.(item, action);
                  }}
                  className={[
                    "relative z-10 rounded-xl px-4 py-2 text-xs font-bold transition-all disabled:opacity-50 cursor-pointer shadow-xs active:scale-95",
                    action.status === "FAILED"
                      ? "border border-error/40 text-error hover:bg-error-container/30"
                      : "border border-secondary/40 text-secondary hover:bg-secondary/10",
                  ].join(" ")}
                >
                  {action.label}
                </button>
              ))
            : null}

          <span className="rounded-xl border border-primary px-5 py-2 text-xs font-bold text-primary transition-all hover:bg-primary hover:text-on-primary shadow-xs">
            Xem chi tiết
          </span>
        </div>
      </div>
    </article>
  );
}

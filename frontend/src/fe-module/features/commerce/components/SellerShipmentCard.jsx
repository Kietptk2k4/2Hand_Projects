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
  if (carrier === "GHN") return "GHN";
  if (carrier === "MANUAL") return "Tự giao";
  if (carrier === "SELF_DELIVERY") return "Tự vận chuyển";
  return carrier;
}

function copyText(text) {
  if (!text) return;
  navigator.clipboard?.writeText(text).catch(() => {});
}

export function SellerShipmentCard({ item, disabled, isUpdating, onRequestStatusUpdate }) {
  const navigate = useNavigate();
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
        "cursor-pointer rounded-xl border border-outline-variant/60 bg-surface-container-lowest p-5 shadow-sm transition-colors hover:bg-surface-container-low/40 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary",
        isManual ? "border-l-4 border-l-secondary" : "",
      ].join(" ")}
    >
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <span
              className={[
                "inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-label-sm font-semibold",
                item.carrier === "GHN"
                  ? "bg-[#ff6600]/10 text-[#c44e00]"
                  : "bg-secondary/10 text-secondary",
              ].join(" ")}
            >
              <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
                {item.carrier === "GHN" ? "local_shipping" : "two_wheeler"}
              </span>
              {carrierLabel(item.carrier)}
            </span>
            <span
              className={[
                "rounded-full px-2.5 py-0.5 text-label-sm font-medium",
                badgeClass,
              ].join(" ")}
            >
              {SHIPMENT_STATUS_LABELS[item.status] || item.status}
            </span>
          </div>

          <div className="mt-3 flex flex-wrap items-center gap-2">
            <span className="text-label-sm text-on-surface-variant">Mã vận đơn:</span>
            <button
              type="button"
              onClick={(event) => {
                event.stopPropagation();
                copyText(tracking);
              }}
              className="relative z-10 font-mono text-label-md font-semibold text-primary hover:underline"
              title="Sao chép"
            >
              {tracking}
            </button>
            <span className="material-symbols-outlined text-[16px] text-on-surface-variant">
              content_copy
            </span>
          </div>

          <p className="mt-2 line-clamp-2 text-body-sm text-on-surface">
            {item.deliveryAddressSummary || "—"}
          </p>

          <div className="mt-3 flex flex-wrap gap-4 text-label-sm text-on-surface-variant">
            <span>
              Đơn:{" "}
              <span className="font-mono text-on-surface">
                {formatShortOrderId(item.orderId)}
              </span>
            </span>
            <span>{item.orderItemCount} mục</span>
            <span>Tạo: {formatDateTime(item.createdAt)}</span>
            <span>Cập nhật: {formatDateTime(item.updatedAt)}</span>
          </div>
        </div>

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
                    "relative z-10 rounded-lg px-3 py-2 text-label-sm font-medium disabled:opacity-50",
                    action.status === "FAILED"
                      ? "border border-error text-error hover:bg-error-container/30"
                      : "border border-secondary text-secondary hover:bg-secondary/5",
                  ].join(" ")}
                >
                  {action.label}
                </button>
              ))
            : null}

          <span className="rounded-lg border border-primary px-4 py-2 text-label-sm font-medium text-primary">
            Chi tiết
          </span>
        </div>
      </div>
    </article>
  );
}

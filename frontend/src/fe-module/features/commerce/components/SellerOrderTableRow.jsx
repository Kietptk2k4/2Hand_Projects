import { formatVndPrice } from "../../social/utils/formatPrice";
import { ORDER_PAYMENT_STATUS_LABELS } from "../constants/sellerOrderConstants";
import { formatShortOrderId } from "../utils/formatOrderDate";
import { SellerOrderItemStatusBadge } from "./SellerOrderItemStatusBadge";
import { SellerOrderShipmentCell } from "./SellerOrderShipmentCell";

export function SellerOrderTableRow({ item, selected, onToggleSelect, disabled }) {
  const canSelect = item.itemStatus === "PENDING";
  const shortId = formatShortOrderId(item.orderId);
  const paymentLabel = ORDER_PAYMENT_STATUS_LABELS[item.orderPaymentStatus] || item.orderPaymentStatus;

  const handleCopyOrderId = () => {
    if (!item.orderId) return;
    navigator.clipboard?.writeText(item.orderId).catch(() => {});
  };

  return (
    <tr className="border-b border-outline-variant/60 hover:bg-surface-container-low/50">
      <td className="px-3 py-3">
        <input
          type="checkbox"
          checked={Boolean(selected)}
          disabled={disabled || !canSelect}
          onChange={() => onToggleSelect?.(item.orderItemId)}
          className="h-4 w-4 rounded border-outline-variant text-primary disabled:opacity-40"
          aria-label={canSelect ? "Chọn dòng" : "Chỉ chọn đơn chờ xử lý"}
        />
      </td>
      <td className="px-3 py-3">
        <div>
          <button
            type="button"
            onClick={handleCopyOrderId}
            className="font-mono text-label-md font-semibold text-primary hover:underline"
            title="Sao chép mã đơn"
          >
            {shortId}
          </button>
          {paymentLabel ? (
            <p className="mt-0.5 text-[11px] text-on-surface-variant">TT: {paymentLabel}</p>
          ) : null}
        </div>
      </td>
      <td className="px-3 py-3">
        <div className="flex min-w-0 items-center gap-3">
          {item.imageSnapshot ? (
            <img
              src={item.imageSnapshot}
              alt=""
              className="h-10 w-10 shrink-0 rounded-lg border border-outline-variant object-cover"
            />
          ) : (
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-surface-container-high">
              <span className="material-symbols-outlined text-on-surface-variant text-[20px]">
                image
              </span>
            </div>
          )}
          <span className="line-clamp-2 text-body-sm text-on-surface">{item.productNameSnapshot}</span>
        </div>
      </td>
      <td className="px-3 py-3 text-center text-body-sm text-on-surface">{item.quantity}</td>
      <td className="px-3 py-3 text-right text-body-sm font-medium text-on-surface">
        {formatVndPrice(item.finalPrice)}
      </td>
      <td className="px-3 py-3">
        <SellerOrderItemStatusBadge status={item.itemStatus} />
      </td>
      <td className="px-3 py-3">
        <SellerOrderShipmentCell shipmentSummary={item.shipmentSummary} />
      </td>
    </tr>
  );
}

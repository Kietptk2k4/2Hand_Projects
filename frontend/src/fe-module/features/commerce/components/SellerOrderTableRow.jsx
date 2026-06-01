import { Link } from "react-router-dom";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { formatOrderPaymentSubline } from "../constants/sellerOrderConstants";
import { formatShortOrderId } from "../utils/formatOrderDate";
import { SellerOrderItemStatusBadge } from "./SellerOrderItemStatusBadge";
import { SellerOrderShipmentCell } from "./SellerOrderShipmentCell";

export function SellerOrderTableRow({
  item,
  selected,
  onToggleSelect,
  onPrepareRow,
  disabled,
  isProcessing,
}) {
  const canSelect = item.itemStatus === "PENDING";
  const canCreateShipment =
    item.itemStatus === "PROCESSING" &&
    !item.shipmentSummary?.shipmentId &&
    item.orderStatus === "PROCESSING" &&
    (item.orderPaymentMethod === "COD" ||
      item.orderPaymentStatus === "PAID");
  const createShipmentHref = canCreateShipment
    ? `${APP_ROUTES.commerceSellerShipments}?create=1&orderId=${encodeURIComponent(item.orderId)}&orderItemIds=${encodeURIComponent(item.orderItemId)}`
    : null;
  const shortId = formatShortOrderId(item.orderId);
  const paymentSubline = formatOrderPaymentSubline(item.orderPaymentStatus, item.orderPaymentMethod);
  const isPaid = item.orderPaymentStatus === "PAID";

  const handleCopyOrderId = () => {
    if (!item.orderId) return;
    navigator.clipboard?.writeText(item.orderId).catch(() => {});
  };

  return (
    <tr
      className={[
        "border-b border-outline-variant/60 transition-colors",
        selected ? "bg-surface-container-low" : "hover:bg-surface-container-low/50",
      ].join(" ")}
    >
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
        <button
          type="button"
          onClick={handleCopyOrderId}
          className="font-mono text-label-md font-semibold text-primary hover:underline"
          title="Sao chép mã đơn"
        >
          {shortId}
        </button>
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
        <div className="flex flex-col gap-1">
          <SellerOrderItemStatusBadge status={item.itemStatus} />
          <div className="flex items-center gap-1 text-[11px] text-on-surface-variant">
            {isPaid ? (
              <span
                className="material-symbols-outlined text-[14px] text-emerald-600"
                aria-hidden="true"
              >
                verified
              </span>
            ) : null}
            <span>{paymentSubline}</span>
          </div>
        </div>
      </td>
      <td className="px-3 py-3">
        <SellerOrderShipmentCell shipmentSummary={item.shipmentSummary} />
      </td>
      <td className="px-3 py-3 text-right">
        {canSelect ? (
          <button
            type="button"
            disabled={disabled || isProcessing}
            onClick={() => onPrepareRow?.(item)}
            className="rounded-lg border border-primary px-2 py-1 text-label-sm font-medium text-primary hover:bg-surface-container-low disabled:opacity-50"
          >
            Chuẩn bị
          </button>
        ) : canCreateShipment ? (
          <Link
            to={createShipmentHref}
            className="inline-block rounded-lg border border-secondary px-2 py-1 text-label-sm font-medium text-secondary hover:bg-secondary/5"
          >
            Tạo vận đơn
          </Link>
        ) : (
          <span className="text-on-surface-variant">—</span>
        )}
      </td>
    </tr>
  );
}

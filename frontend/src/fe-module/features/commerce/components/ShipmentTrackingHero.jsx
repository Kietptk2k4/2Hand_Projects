import { useCallback, useState } from "react";
import { ORDER_STATUS_LABELS, ORDER_STATUS_BADGE_CLASS } from "../constants/orderListConstants";
import {
  SHIPMENT_STATUS_BADGE_CLASS,
  SHIPMENT_STATUS_LABELS,
} from "../constants/orderDetailConstants";
import { SHIPMENT_TYPE_LABELS } from "../constants/shipmentTrackingConstants";
import { formatOrderDate } from "../utils/formatOrderDate";

function CopyField({ label, value }) {
  const [copied, setCopied] = useState(false);

  const copy = useCallback(async () => {
    if (!value || !navigator?.clipboard) return;
    try {
      await navigator.clipboard.writeText(value);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // ignore
    }
  }, [value]);

  if (!value) return null;

  return (
    <div className="flex flex-wrap items-center gap-2 text-body-sm">
      <span className="text-on-surface-variant">{label}:</span>
      <span className="font-mono font-medium text-on-surface">{value}</span>
      <button type="button" onClick={copy} className="text-primary hover:underline">
        {copied ? "Đã sao chép" : "Sao chép"}
      </button>
    </div>
  );
}

export function ShipmentTrackingHero({ detail, tracking }) {
  const status = tracking?.status || detail?.status;
  const statusLabel = SHIPMENT_STATUS_LABELS[status] || status;
  const statusClass =
    SHIPMENT_STATUS_BADGE_CLASS[status] || "bg-surface-container-high text-on-surface-variant";

  const shipmentType = detail?.shipmentType || tracking?.shipmentType;
  const typeLabel = SHIPMENT_TYPE_LABELS[shipmentType] || shipmentType;

  const orderStatus = tracking?.orderStatus;
  const orderStatusLabel = orderStatus ? ORDER_STATUS_LABELS[orderStatus] || orderStatus : "";
  const orderStatusClass =
    ORDER_STATUS_BADGE_CLASS[orderStatus] ||
    "bg-surface-container-high text-on-surface-variant";

  const carrier = detail?.carrier || tracking?.carrier || "Đơn vị vận chuyển";
  const estimatedDelivery =
    detail?.estimatedDeliveryDate || tracking?.estimatedDeliveryDate;
  const shippedAt = detail?.shippedAt || tracking?.shippedAt;
  const deliveredAt = detail?.deliveredAt || tracking?.deliveredAt;

  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div className="flex gap-4">
          <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-xl bg-primary/10">
            <span className="material-symbols-outlined text-3xl text-primary" aria-hidden="true">
              local_shipping
            </span>
          </div>
          <div>
            <span className={`inline-flex rounded-full px-3 py-1 text-label-sm ${statusClass}`}>
              {statusLabel}
            </span>
            <h2 className="mt-2 text-headline-sm font-semibold text-on-surface">{carrier}</h2>
            {typeLabel ? (
              <p className="mt-1 text-body-sm text-on-surface-variant">{typeLabel}</p>
            ) : null}
          </div>
        </div>

        {orderStatus ? (
          <span className={`self-start rounded-full px-2.5 py-0.5 text-label-sm ${orderStatusClass}`}>
            Đơn: {orderStatusLabel}
          </span>
        ) : null}
      </div>

      <div className="mt-4 space-y-2 border-t border-outline-variant/60 pt-4">
        <CopyField label="Mã vận đơn" value={detail?.trackingNumber || tracking?.trackingNumber} />
        <CopyField label="Mã GHN" value={detail?.ghnOrderCode || tracking?.ghnOrderCode} />
      </div>

      <dl className="mt-3 grid gap-2 text-body-sm text-on-surface-variant sm:grid-cols-2">
        {estimatedDelivery ? (
          <div>
            <dt className="inline">Dự kiến giao: </dt>
            <dd className="inline text-on-surface">{estimatedDelivery}</dd>
          </div>
        ) : null}
        {shippedAt ? (
          <div>
            <dt className="inline">Đã gửi: </dt>
            <dd className="inline text-on-surface">{formatOrderDate(shippedAt)}</dd>
          </div>
        ) : null}
        {deliveredAt ? (
          <div>
            <dt className="inline">Đã giao: </dt>
            <dd className="inline text-on-surface">{formatOrderDate(deliveredAt)}</dd>
          </div>
        ) : null}
      </dl>
    </section>
  );
}

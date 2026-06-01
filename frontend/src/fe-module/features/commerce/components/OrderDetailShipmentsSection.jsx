import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { formatOrderDate } from "../utils/formatOrderDate";
import { SHIPMENT_STATUS_BADGE_CLASS, SHIPMENT_STATUS_LABELS } from "../constants/orderDetailConstants";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function OrderDetailShipmentsSection({ orderId, shipments }) {
  const [copiedId, setCopiedId] = useState(null);

  const copyTracking = useCallback(async (shipmentId, trackingNumber) => {
    if (!trackingNumber || !navigator?.clipboard) return;
    try {
      await navigator.clipboard.writeText(trackingNumber);
      setCopiedId(shipmentId);
      setTimeout(() => setCopiedId(null), 2000);
    } catch {
      // ignore
    }
  }, []);

  if (!shipments?.length) return null;

  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-6">
      <h2 className="mb-4 text-headline-sm font-semibold text-on-surface">Vận chuyển</h2>

      <div className="flex flex-col gap-4">
        {shipments.map((shipment) => {
          const statusLabel = SHIPMENT_STATUS_LABELS[shipment.status] || shipment.status;
          const statusClass =
            SHIPMENT_STATUS_BADGE_CLASS[shipment.status] ||
            "bg-surface-container-high text-on-surface-variant";

          return (
            <div
              key={shipment.shipmentId}
              className="rounded-lg border border-outline-variant/80 bg-surface-container-low p-4"
            >
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className="material-symbols-outlined text-primary" aria-hidden="true">
                    local_shipping
                  </span>
                  <span className="text-label-md font-medium text-on-surface">
                    {shipment.carrier || "Đơn vị vận chuyển"}
                  </span>
                </div>
                <span className={`rounded-full px-2.5 py-0.5 text-label-sm ${statusClass}`}>
                  {statusLabel}
                </span>
              </div>

              {shipment.trackingNumber ? (
                <div className="mt-3 flex flex-wrap items-center gap-2 text-body-sm">
                  <span className="text-on-surface-variant">Mã vận đơn:</span>
                  <span className="font-mono font-medium text-on-surface">
                    {shipment.trackingNumber}
                  </span>
                  <button
                    type="button"
                    onClick={() => copyTracking(shipment.shipmentId, shipment.trackingNumber)}
                    className="text-primary hover:underline"
                  >
                    {copiedId === shipment.shipmentId ? "Đã sao chép" : "Sao chép"}
                  </button>
                </div>
              ) : null}

              <dl className="mt-3 grid gap-1 text-body-sm text-on-surface-variant sm:grid-cols-2">
                {shipment.estimatedDeliveryDate ? (
                  <div>
                    <dt className="inline">Dự kiến giao: </dt>
                    <dd className="inline text-on-surface">{shipment.estimatedDeliveryDate}</dd>
                  </div>
                ) : null}
                {shipment.shippedAt ? (
                  <div>
                    <dt className="inline">Đã gửi: </dt>
                    <dd className="inline text-on-surface">{formatOrderDate(shipment.shippedAt)}</dd>
                  </div>
                ) : null}
                {shipment.deliveredAt ? (
                  <div>
                    <dt className="inline">Đã giao: </dt>
                    <dd className="inline text-on-surface">
                      {formatOrderDate(shipment.deliveredAt)}
                    </dd>
                  </div>
                ) : null}
              </dl>

              {shipment.shipmentId && orderId ? (
                <div className="mt-4 flex flex-wrap gap-2">
                  <Link
                    to={APP_ROUTES.commerceShipmentTracking
                      .replace(":orderId", orderId)
                      .replace(":shipmentId", shipment.shipmentId)}
                    className="rounded-lg border border-primary px-4 py-2 text-label-md font-medium text-primary hover:bg-surface-container-low"
                    onClick={(event) => event.stopPropagation()}
                  >
                    Theo dõi vận chuyển
                  </Link>
                </div>
              ) : null}
            </div>
          );
        })}
      </div>
    </section>
  );
}

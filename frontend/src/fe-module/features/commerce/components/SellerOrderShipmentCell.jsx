import { SHIPMENT_STATUS_LABELS } from "../constants/sellerOrderConstants";

export function SellerOrderShipmentCell({ shipmentSummary }) {
  if (!shipmentSummary?.status) {
    return <span className="text-on-surface-variant">—</span>;
  }

  const label = SHIPMENT_STATUS_LABELS[shipmentSummary.status] || shipmentSummary.status;

  return (
    <div className="min-w-0">
      <p className="text-body-sm font-medium text-on-surface">{label}</p>
      {shipmentSummary.trackingNumber ? (
        <p className="truncate text-body-sm text-on-surface-variant">
          {shipmentSummary.carrier}: {shipmentSummary.trackingNumber}
        </p>
      ) : shipmentSummary.carrier ? (
        <p className="text-body-sm text-on-surface-variant">{shipmentSummary.carrier}</p>
      ) : null}
    </div>
  );
}

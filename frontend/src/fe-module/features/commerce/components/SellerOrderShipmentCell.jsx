import { Link } from "react-router-dom";
import { SHIPMENT_STATUS_LABELS } from "../constants/sellerOrderConstants";

export function SellerOrderShipmentCell({ shipmentSummary, detailHref }) {
  if (!shipmentSummary?.status) {
    return <span className="text-on-surface-variant">—</span>;
  }

  const label = SHIPMENT_STATUS_LABELS[shipmentSummary.status] || shipmentSummary.status;

  const content = (
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

  if (!detailHref) {
    return content;
  }

  return (
    <Link
      to={detailHref}
      onClick={(event) => event.stopPropagation()}
      className="relative z-10 block rounded-lg transition-colors hover:bg-surface-container-low"
    >
      {content}
    </Link>
  );
}

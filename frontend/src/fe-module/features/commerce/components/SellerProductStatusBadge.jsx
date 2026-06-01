import { STATUS_BADGE_CLASS, STATUS_LABELS } from "../constants/sellerProductConstants";

export function SellerProductStatusBadge({ status }) {
  const label = STATUS_LABELS[status] || status;
  const className = STATUS_BADGE_CLASS[status] || STATUS_BADGE_CLASS.DRAFT;

  return (
    <span className={`inline-flex rounded-full px-2.5 py-0.5 text-label-sm font-medium ${className}`}>
      {label}
    </span>
  );
}

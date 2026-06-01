import { ITEM_STATUS_BADGE_CLASS, ITEM_STATUS_LABELS } from "../constants/sellerOrderConstants";

export function SellerOrderItemStatusBadge({ status }) {
  if (!status) return null;

  return (
    <span
      className={[
        "inline-flex rounded-full px-2.5 py-0.5 text-label-sm font-semibold",
        ITEM_STATUS_BADGE_CLASS[status] || ITEM_STATUS_BADGE_CLASS.PENDING,
      ].join(" ")}
    >
      {ITEM_STATUS_LABELS[status] || status}
    </span>
  );
}

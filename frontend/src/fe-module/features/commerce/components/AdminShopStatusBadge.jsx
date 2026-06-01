import {
  SHOP_STATUS_BADGE_CLASS,
  SHOP_STATUS_LABELS,
} from "../constants/adminShopModerationConstants";

export function AdminShopStatusBadge({ status }) {
  const label = SHOP_STATUS_LABELS[status] || status;
  const className =
    SHOP_STATUS_BADGE_CLASS[status] || "bg-surface-container-high text-on-surface-variant";

  return (
    <span
      className={[
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-label-sm font-semibold",
        className,
      ].join(" ")}
    >
      <span
        className={[
          "h-2 w-2 rounded-full",
          status === "ACTIVE"
            ? "bg-emerald-500"
            : status === "SUSPENDED"
              ? "bg-error"
              : "bg-outline",
        ].join(" ")}
        aria-hidden="true"
      />
      {label}
    </span>
  );
}

import {
  REVIEW_STATUS_BADGE_CLASS,
  REVIEW_STATUS_LABELS,
} from "../constants/adminReviewModerationConstants";

export function AdminReviewStatusBadge({ status }) {
  const label = REVIEW_STATUS_LABELS[status] || status;
  const className =
    REVIEW_STATUS_BADGE_CLASS[status] || "bg-surface-container-high text-on-surface-variant";

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
          status === "VISIBLE" ? "bg-emerald-500" : "bg-error",
        ].join(" ")}
        aria-hidden="true"
      />
      {label}
    </span>
  );
}

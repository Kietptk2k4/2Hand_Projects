import {
  STATUS_BADGE_CLASS,
  STATUS_LABELS,
} from "../constants/adminProductRemovalConstants";

export function AdminProductStatusBadge({ status }) {
  const label = STATUS_LABELS[status] || status;
  const className =
    STATUS_BADGE_CLASS[status] || "bg-surface-container-high text-on-surface-variant";

  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-0.5 text-label-sm font-medium ${className}`}
    >
      {label}
    </span>
  );
}

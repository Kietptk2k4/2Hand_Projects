export function truncateModerationId(id) {
  if (!id || id.length < 12) return id || "—";
  return `${id.slice(0, 8)}…${id.slice(-4)}`;
}

export function statusBadgeVariant(status) {
  const normalized = String(status || "").toUpperCase();
  if (normalized === "REMOVED" || normalized === "DELETED") return "danger";
  if (normalized === "HIDDEN" || normalized === "DRAFT" || normalized === "SUSPENDED") return "warning";
  if (normalized === "ACTIVE" || normalized === "NONE" || normalized === "VISIBLE") return "success";
  return "neutral";
}

export function moderationStatusBadgeVariant(status) {
  const normalized = String(status || "").toUpperCase();
  if (normalized === "REMOVED") return "danger";
  if (normalized === "HIDDEN") return "warning";
  if (normalized === "NONE") return "neutral";
  return statusBadgeVariant(status);
}

import { AdminStatusBadge } from "../../components/ui";
import { AUDIT_STATUS_LABELS } from "../constants/adminAuditConstants.js";

function getAuditStatusVariant(status) {
  const normalized = String(status || "").toUpperCase();
  if (normalized === "SUCCESS") return "success";
  if (normalized === "FAILURE") return "warning";
  return "neutral";
}

export function AuditStatusBadge({ status }) {
  const normalized = String(status || "").toUpperCase();
  const label = AUDIT_STATUS_LABELS[normalized] || normalized || "—";
  return (
    <AdminStatusBadge variant={getAuditStatusVariant(status)}>{label}</AdminStatusBadge>
  );
}

import { AdminStatusBadge } from "../../components/ui";

function getAuditStatusVariant(status) {
  const normalized = String(status || "").toUpperCase();
  if (normalized === "SUCCESS") return "success";
  if (normalized === "FAILURE") return "warning";
  return "neutral";
}

export function AuditStatusBadge({ status }) {
  const normalized = String(status || "").toUpperCase();
  return (
    <AdminStatusBadge variant={getAuditStatusVariant(status)}>
      {normalized || "—"}
    </AdminStatusBadge>
  );
}

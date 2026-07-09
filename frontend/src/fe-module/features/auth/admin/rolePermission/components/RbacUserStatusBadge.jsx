import { AdminStatusBadge } from "../../components/ui";

function getUserStatusVariant(status) {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "SUSPENDED":
      return "warning";
    case "PENDING_VERIFICATION":
      return "neutral";
    default:
      return "neutral";
  }
}

export function RbacUserStatusBadge({ status }) {
  return (
    <AdminStatusBadge variant={getUserStatusVariant(status)}>{status}</AdminStatusBadge>
  );
}

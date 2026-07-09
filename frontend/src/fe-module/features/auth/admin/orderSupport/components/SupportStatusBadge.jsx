import { AdminStatusBadge } from "../../components/ui";
import { supportStatusVariant } from "./ui/supportStatusVariant.js";

export function SupportStatusBadge({ status, className = "" }) {
  if (!status) return null;

  return (
    <AdminStatusBadge variant={supportStatusVariant(status)} className={className}>
      {status}
    </AdminStatusBadge>
  );
}

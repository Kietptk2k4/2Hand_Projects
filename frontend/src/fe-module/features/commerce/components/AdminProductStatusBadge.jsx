import { STATUS_LABELS } from "../constants/adminProductRemovalConstants";
import { AdminStatusBadge } from "../../auth/admin/components/ui";
import { productStatusBadgeVariant } from "../utils/adminCommerceDisplayUtils";

export function AdminProductStatusBadge({ status }) {
  const label = STATUS_LABELS[status] || status;

  return (
    <AdminStatusBadge variant={productStatusBadgeVariant(status)}>{label}</AdminStatusBadge>
  );
}

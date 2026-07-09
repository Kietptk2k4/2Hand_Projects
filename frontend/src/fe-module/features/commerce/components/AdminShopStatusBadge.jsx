import { SHOP_STATUS_LABELS } from "../constants/adminShopModerationConstants";
import { AdminStatusBadge } from "../../auth/admin/components/ui";
import { shopStatusBadgeVariant } from "../utils/adminCommerceDisplayUtils";

export function AdminShopStatusBadge({ status }) {
  const label = SHOP_STATUS_LABELS[status] || status;

  return (
    <AdminStatusBadge variant={shopStatusBadgeVariant(status)}>{label}</AdminStatusBadge>
  );
}

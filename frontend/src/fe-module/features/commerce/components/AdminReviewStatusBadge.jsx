import { REVIEW_STATUS_LABELS } from "../constants/adminReviewModerationConstants";
import { AdminStatusBadge } from "../../auth/admin/components/ui";
import { reviewStatusBadgeVariant } from "../utils/adminCommerceDisplayUtils";

export function AdminReviewStatusBadge({ status }) {
  const label = REVIEW_STATUS_LABELS[status] || status;

  return (
    <AdminStatusBadge variant={reviewStatusBadgeVariant(status)}>{label}</AdminStatusBadge>
  );
}

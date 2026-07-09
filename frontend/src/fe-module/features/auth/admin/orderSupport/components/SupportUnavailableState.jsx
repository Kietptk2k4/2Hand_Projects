import { AdminSurfaceCard } from "../../components/ui";
import { ORDER_SUPPORT_COMMERCE_UNAVAILABLE } from "../constants/orderSupportUiStrings.js";

export function SupportUnavailableState({ message = ORDER_SUPPORT_COMMERCE_UNAVAILABLE }) {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-border bg-admin-surface-muted/50">
      <p className="text-sm text-admin-text-secondary">{message}</p>
    </AdminSurfaceCard>
  );
}

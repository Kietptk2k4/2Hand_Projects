import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";
import { ORDER_SUPPORT_COMMERCE_UNAVAILABLE } from "../constants/orderSupportUiStrings.js";

export function SupportUnavailableState({ message = ORDER_SUPPORT_COMMERCE_UNAVAILABLE }) {
  return (
    <AccountCard className="border-outline-variant bg-surface-container-low">
      <p className="text-sm text-on-surface-variant">{message}</p>
    </AccountCard>
  );
}

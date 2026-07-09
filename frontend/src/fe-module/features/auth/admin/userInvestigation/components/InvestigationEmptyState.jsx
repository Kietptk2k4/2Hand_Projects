import { AdminSurfaceCard } from "../../components/ui";
import { INVESTIGATION_EMPTY_USER_MESSAGE } from "../constants/userInvestigationUiStrings.js";

export function InvestigationEmptyState({ message = INVESTIGATION_EMPTY_USER_MESSAGE }) {
  return (
    <AdminSurfaceCard padding="lg" className="text-center">
      <p className="text-sm text-admin-text-secondary">{message}</p>
    </AdminSurfaceCard>
  );
}

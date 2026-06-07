import { EmptyState } from "../../../../../shared/ui/PageState.jsx";
import { INVESTIGATION_EMPTY_USER_MESSAGE } from "../constants/userInvestigationUiStrings.js";

export function InvestigationEmptyState({ message = INVESTIGATION_EMPTY_USER_MESSAGE }) {
  return <EmptyState message={message} />;
}

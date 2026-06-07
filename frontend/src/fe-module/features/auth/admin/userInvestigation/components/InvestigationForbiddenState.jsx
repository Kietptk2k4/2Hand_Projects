import { InvestigationPermissionNotice } from "./InvestigationPermissionNotice.jsx";

export function InvestigationForbiddenState({ message }) {
  if (!message) return null;
  return <InvestigationPermissionNotice message={message} />;
}

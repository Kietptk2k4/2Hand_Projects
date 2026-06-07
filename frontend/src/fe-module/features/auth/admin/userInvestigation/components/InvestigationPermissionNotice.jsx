import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";

export function InvestigationPermissionNotice({ message }) {
  if (!message) return null;
  return (
    <AccountCard className="mb-6 border-amber-200 bg-amber-50">
      <p className="text-sm text-amber-900">{message}</p>
    </AccountCard>
  );
}
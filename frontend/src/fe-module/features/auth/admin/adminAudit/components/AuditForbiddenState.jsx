import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";

export function AuditForbiddenState({ message }) {
  return (
    <AccountCard className="border-amber-200 bg-amber-50/50">
      <p className="text-sm text-amber-900">{message}</p>
    </AccountCard>
  );
}
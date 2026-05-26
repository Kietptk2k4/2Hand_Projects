import { ASSIGNABLE_USERS } from "../../constants/assignableUsers.js";
import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";

export function AdminUserTargetBar({ userId, onUserIdChange }) {
  const selected = ASSIGNABLE_USERS.find((u) => u.id === userId);

  return (
    <AccountCard className="mb-6">
      <label htmlFor="investigation-user" className="mb-1.5 block text-xs font-semibold text-on-surface">
        Nguoi dung dieu tra
      </label>
      <select
        id="investigation-user"
        value={userId}
        onChange={(e) => onUserIdChange(e.target.value)}
        className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2.5 text-base outline-none focus:border-primary focus:ring-1 focus:ring-primary/30"
      >
        <option value="">Chon user...</option>
        {ASSIGNABLE_USERS.map((user) => (
          <option key={user.id} value={user.id}>
            {user.email} — {user.display_name}
          </option>
        ))}
      </select>
      {selected ? (
        <p className="mt-2 break-all text-xs text-on-surface-variant">User ID: {selected.id}</p>
      ) : null}
    </AccountCard>
  );
}

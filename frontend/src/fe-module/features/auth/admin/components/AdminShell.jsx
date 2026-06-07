import { useState } from "react";
import { AdminLogoutConfirmModal } from "../../components/AdminLogoutConfirmModal.jsx";
import { LogoutIcon } from "../../components/AdminAuthIcons.jsx";
import { useAdminLogout } from "../../hooks/useAdminLogout.js";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";

export function AdminShell({ children }) {
  const { user } = useAuthSession();
  const { performAdminLogout, isLoggingOut } = useAdminLogout();
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);

  const displayEmail = user?.email?.trim();

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-outline-variant bg-surface-container-lowest px-4 py-3 shadow-[0_4px_6px_-1px_rgba(0,0,0,0.08)] sm:px-6">
        <div>
          <p className="text-lg font-semibold text-primary">2Hands Admin</p>
          <p className="text-xs text-on-surface-variant">Admin Console</p>
        </div>

        <div className="flex items-center gap-3">
          {displayEmail ? (
            <p className="hidden text-sm text-on-surface-variant sm:block">{displayEmail}</p>
          ) : null}
          <button
            type="button"
            onClick={() => setIsConfirmOpen(true)}
            disabled={isLoggingOut}
            className="inline-flex items-center gap-2 rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm font-medium text-on-surface transition-colors hover:bg-surface-container-low disabled:cursor-not-allowed disabled:opacity-60"
          >
            <LogoutIcon className="h-4 w-4" />
            Đăng xuất
          </button>
        </div>
      </header>

      {children}

      <AdminLogoutConfirmModal
        open={isConfirmOpen}
        isLoggingOut={isLoggingOut}
        onCancel={() => setIsConfirmOpen(false)}
        onConfirm={async () => {
          await performAdminLogout();
          setIsConfirmOpen(false);
        }}
      />
    </div>
  );
}

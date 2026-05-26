import { useCallback, useState } from "react";
import { getUserPermissions } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { ASSIGNABLE_USERS } from "../../constants/assignableUsers.js";
import {
  AccountCard,
  AccountSkeleton,
  PrimaryButton,
  TabPanelHeader,
} from "../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../shared/ui/PageState.jsx";

function groupPermissionsByPrefix(codes) {
  const groups = {};
  codes.forEach((code) => {
    const prefix = code.includes("_") ? code.split("_")[0] : "OTHER";
    if (!groups[prefix]) groups[prefix] = [];
    groups[prefix].push(code);
  });
  return groups;
}

export function PermissionsOfUserTab() {
  const { showSessionExpired } = useAuthSession();
  const [userId, setUserId] = useState("");
  const [resolvedUserId, setResolvedUserId] = useState("");
  const [permissions, setPermissions] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const selectedUser = ASSIGNABLE_USERS.find((u) => u.id === userId);

  const load = useCallback(async () => {
    if (!userId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getUserPermissions(userId);
      setResolvedUserId(data?.user_id || userId);
      setPermissions(data?.permissions || []);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      if (error?.code === 403) {
        setStatus("forbidden");
        setErrorMessage(error?.message || "Ban khong co quyen truy cap.");
        return;
      }
      if (error?.code === 404) {
        setStatus("not_found");
        setErrorMessage(error?.message || "Khong tim thay user.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc permission.");
    }
  }, [userId, showSessionExpired]);

  const codes = permissions.map((p) => p.code);
  const grouped = groupPermissionsByPrefix(codes);

  return (
    <div>
      <TabPanelHeader
        title="Permission cua User"
        subtitle="Xem permission hieu luc cua user (chi ma permission)."
      />

      <AccountCard className="mb-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
          <div className="min-w-0 flex-1">
            <label htmlFor="user-perm-select" className="mb-1.5 block text-xs font-semibold text-on-surface">
              User
            </label>
            <select
              id="user-perm-select"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2.5 text-base outline-none focus:border-primary"
            >
              <option value="">Chon user...</option>
              {ASSIGNABLE_USERS.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.email}
                </option>
              ))}
            </select>
          </div>
          <PrimaryButton type="button" onClick={load} disabled={!userId}>
            Tai permission
          </PrimaryButton>
        </div>
      </AccountCard>

      {status === "loading" ? <AccountSkeleton /> : null}
      {status === "forbidden" || status === "not_found" || status === "error" ? (
        <ErrorState message={errorMessage} />
      ) : null}

      {status === "ready" ? (
        <>
          <AccountCard className="mb-4">
            <p className="text-sm text-on-surface-variant">User ID</p>
            <p className="mt-1 break-all font-mono text-sm text-on-surface">{resolvedUserId}</p>
            {selectedUser ? (
              <p className="mt-2 text-sm text-on-surface-variant">Email: {selectedUser.email}</p>
            ) : null}
          </AccountCard>

          {permissions.length === 0 ? (
            <EmptyState message="User chua co permission nao." />
          ) : (
            <AccountCard>
              <div className="flex flex-wrap gap-2">
                {codes.map((code) => (
                  <span
                    key={code}
                    className="inline-flex rounded-full bg-account-surface-low px-3 py-1 text-xs font-semibold text-on-surface"
                  >
                    {code}
                  </span>
                ))}
              </div>

              {Object.keys(grouped).length > 1 ? (
                <p className="mt-4 text-xs text-on-surface-variant">
                  Chi de hien thi — nhom theo prefix:{" "}
                  {Object.entries(grouped)
                    .map(([prefix, items]) => `${prefix} (${items.length})`)
                    .join(", ")}
                </p>
              ) : null}
            </AccountCard>
          )}
        </>
      ) : null}
    </div>
  );
}

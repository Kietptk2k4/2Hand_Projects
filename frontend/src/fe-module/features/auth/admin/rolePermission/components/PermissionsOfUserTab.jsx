import { useCallback, useEffect, useState } from "react";
import { getUserPermissions } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  AccountCard,
  AccountSkeleton,
  TabPanelHeader,
} from "../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { RbacUserListPanel } from "./RbacUserListPanel.jsx";

function groupPermissionsByPrefix(codes) {
  const groups = {};
  codes.forEach((code) => {
    const prefix = code.includes("_") ? code.split("_")[0] : "OTHER";
    if (!groups[prefix]) groups[prefix] = [];
    groups[prefix].push(code);
  });
  return groups;
}

export function PermissionsOfUserTab({
  rbacUserListFilters,
  rbacSelectedUserId,
  onRbacUserListFiltersChange,
  onRbacUserSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const [selectedUser, setSelectedUser] = useState(null);
  const [resolvedUserId, setResolvedUserId] = useState("");
  const [permissions, setPermissions] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const handleUserSelect = (userId, userRow) => {
    setSelectedUser(userRow || null);
    onRbacUserSelect?.(userId);
  };

  const load = useCallback(async () => {
    if (!rbacSelectedUserId) {
      setStatus("idle");
      setPermissions([]);
      setResolvedUserId("");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getUserPermissions(rbacSelectedUserId);
      setResolvedUserId(data?.user_id || rbacSelectedUserId);
      setPermissions(data?.permissions || []);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      if (error?.code === 403) {
        setStatus("forbidden");
        setErrorMessage(error?.message || "Bạn không có quyền truy cập.");
        return;
      }
      if (error?.code === 404) {
        setStatus("not_found");
        setErrorMessage(error?.message || "Không tìm thấy người dùng.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được quyền.");
    }
  }, [rbacSelectedUserId, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const codes = permissions.map((p) => p.code);
  const grouped = groupPermissionsByPrefix(codes);

  return (
    <div>
      <TabPanelHeader
        title="Quyền của người dùng"
        subtitle="Xem permission hiệu lực của người dùng (chỉ mã permission)."
      />

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,0.7fr)]">
        <RbacUserListPanel
          userListFilters={rbacUserListFilters}
          onFiltersChange={onRbacUserListFiltersChange}
          selectedUserId={rbacSelectedUserId}
          onUserSelect={handleUserSelect}
          onSelectedUserSync={setSelectedUser}
        />

        <div className="space-y-4">
          {!rbacSelectedUserId ? (
            <AccountCard>
              <p className="text-sm text-on-surface-variant">
                Chọn một người dùng từ danh sách bên trái để xem quyền.
              </p>
            </AccountCard>
          ) : null}

          {status === "loading" ? <AccountSkeleton /> : null}
          {status === "forbidden" || status === "not_found" || status === "error" ? (
            <ErrorState message={errorMessage} />
          ) : null}

          {status === "ready" ? (
            <>
              <AccountCard>
                <p className="text-sm text-on-surface-variant">User ID</p>
                <p className="mt-1 break-all font-mono text-sm text-on-surface">{resolvedUserId}</p>
                {selectedUser ? (
                  <p className="mt-2 text-sm text-on-surface-variant">Email: {selectedUser.email}</p>
                ) : null}
              </AccountCard>

              {permissions.length === 0 ? (
                <EmptyState message="Người dùng chưa có quyền nào." />
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
                      Chỉ để hiển thị — nhóm theo prefix:{" "}
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
      </div>
    </div>
  );
}

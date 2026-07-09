import { useCallback, useEffect, useMemo, useState } from "react";
import { getUserPermissions } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { PermissionsOfUserTabView } from "./PermissionsOfUserTabView.jsx";
import { RbacUserListPanel } from "./RbacUserListPanel.jsx";

const TITLE = "Quyền của người dùng";
const SUBTITLE = "Xem permission hiệu lực của người dùng (chỉ mã permission).";

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

  const groupedHint = useMemo(() => {
    const codes = permissions.map((permission) => permission.code);
    const grouped = groupPermissionsByPrefix(codes);
    if (Object.keys(grouped).length <= 1) return "";
    return `Chỉ để hiển thị — nhóm theo prefix: ${Object.entries(grouped)
      .map(([prefix, items]) => `${prefix} (${items.length})`)
      .join(", ")}`;
  }, [permissions]);

  return (
    <PermissionsOfUserTabView
      title={TITLE}
      subtitle={SUBTITLE}
      rbacSelectedUserId={rbacSelectedUserId}
      selectedUser={selectedUser}
      resolvedUserId={resolvedUserId}
      status={status}
      errorMessage={errorMessage}
      permissions={permissions}
      groupedHint={groupedHint}
      userListPanel={
        <RbacUserListPanel
          userListFilters={rbacUserListFilters}
          onFiltersChange={onRbacUserListFiltersChange}
          selectedUserId={rbacSelectedUserId}
          onUserSelect={handleUserSelect}
          onSelectedUserSync={setSelectedUser}
        />
      }
    />
  );
}

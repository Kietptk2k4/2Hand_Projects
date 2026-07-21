import { useCallback, useEffect, useState } from "react";
import { getUserPermissions } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { PermissionsOfUserTabView } from "./PermissionsOfUserTabView.jsx";
import { RbacUserListPanel } from "./RbacUserListPanel.jsx";

const TITLE = "Quyền của người dùng";
const SUBTITLE = "Xem quyền hiệu lực của người dùng theo mã permission từ vai trò được gán.";

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

  const handleClearUser = () => {
    setSelectedUser(null);
    setPermissions([]);
    setResolvedUserId("");
    setStatus("idle");
    onRbacUserSelect?.(null);
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

  useEffect(() => {
    if (!rbacSelectedUserId) {
      setSelectedUser(null);
    }
  }, [rbacSelectedUserId]);

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
      onClearUser={handleClearUser}
      onRetry={load}
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

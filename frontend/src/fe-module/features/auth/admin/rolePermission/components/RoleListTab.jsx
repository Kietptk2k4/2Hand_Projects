import { useCallback, useEffect, useState } from "react";
import { getAdminRoles } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { RoleListTabView } from "./RoleListTabView.jsx";

const TITLE = "Danh sách vai trò";
const SUBTITLE = "Xem tất cả vai trò trong hệ thống.";

export function RoleListTab({ onViewRolePermissions }) {
  const { showSessionExpired } = useAuthSession();
  const [roles, setRoles] = useState([]);
  const [status, setStatus] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const data = await getAdminRoles();
      setRoles(data?.roles || []);
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
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được danh sách vai trò.");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <RoleListTabView
      title={TITLE}
      subtitle={SUBTITLE}
      status={status}
      errorMessage={errorMessage}
      roles={roles}
      onViewRolePermissions={onViewRolePermissions}
      onRetry={load}
    />
  );
}

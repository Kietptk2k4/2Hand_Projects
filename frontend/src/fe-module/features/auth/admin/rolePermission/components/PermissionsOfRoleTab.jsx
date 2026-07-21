import { useCallback, useEffect, useMemo, useState } from "react";
import {
  assignPermissionToRole,
  getAdminRoles,
  getPermissionCatalog,
  getRolePermissions,
  revokePermissionFromRole,
} from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { PermissionsOfRoleTabView } from "./PermissionsOfRoleTabView.jsx";

const TITLE = "Quyền của vai trò";
const SUBTITLE = "Xem, gán và thu hồi quyền cho từng vai trò.";

export function PermissionsOfRoleTab({
  selectedRoleId,
  onSelectedRoleIdChange,
  onTabChange,
  onNotify,
}) {
  const { showSessionExpired } = useAuthSession();
  const [roles, setRoles] = useState([]);
  const [rolesStatus, setRolesStatus] = useState("loading");
  const [catalog, setCatalog] = useState([]);
  const [roleId, setRoleId] = useState(selectedRoleId || "");
  const [roleMeta, setRoleMeta] = useState(null);
  const [permissions, setPermissions] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [selectedPermissionCode, setSelectedPermissionCode] = useState("");
  const [isAssigning, setIsAssigning] = useState(false);
  const [revokingCode, setRevokingCode] = useState("");

  useEffect(() => {
    setRoleId(selectedRoleId || "");
  }, [selectedRoleId]);

  const handleRoleIdChange = useCallback(
    (nextRoleId) => {
      setRoleId(nextRoleId);
      onSelectedRoleIdChange?.(nextRoleId || undefined);
    },
    [onSelectedRoleIdChange],
  );

  const loadRoles = useCallback(async () => {
    setRolesStatus("loading");
    try {
      const data = await getAdminRoles();
      setRoles(data?.roles || []);
      setRolesStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setRolesStatus("error");
    }
  }, [showSessionExpired]);

  const loadCatalog = useCallback(async () => {
    try {
      const data = await getPermissionCatalog();
      setCatalog(data?.permissions || []);
    } catch (error) {
      if (error?.code === 401) showSessionExpired(error?.message);
    }
  }, [showSessionExpired]);

  const loadPermissions = useCallback(async () => {
    if (!roleId) {
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getRolePermissions(roleId);
      setRoleMeta(data?.role || null);
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
        setErrorMessage(error?.message || "Không tìm thấy vai trò.");
        return;
      }
      if (error?.code === 400) {
        setStatus("error");
        setErrorMessage(error?.message || "Mã vai trò không hợp lệ.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được permission.");
    }
  }, [roleId, showSessionExpired]);

  useEffect(() => {
    loadRoles();
    loadCatalog();
  }, [loadRoles, loadCatalog]);

  useEffect(() => {
    loadPermissions();
  }, [loadPermissions]);

  const assignedCodes = useMemo(
    () => new Set(permissions.map((perm) => perm.code)),
    [permissions],
  );

  const availablePermissions = useMemo(
    () => catalog.filter((perm) => !assignedCodes.has(perm.code)),
    [catalog, assignedCodes],
  );

  const isBusy = isAssigning || revokingCode !== "";

  useEffect(() => {
    if (!selectedPermissionCode && availablePermissions.length > 0) {
      setSelectedPermissionCode(availablePermissions[0].code);
      return;
    }
    if (selectedPermissionCode && !availablePermissions.some((perm) => perm.code === selectedPermissionCode)) {
      setSelectedPermissionCode(availablePermissions[0]?.code || "");
    }
  }, [availablePermissions, selectedPermissionCode]);

  const handleAssignPermission = async () => {
    if (!roleId || !selectedPermissionCode) return;

    setIsAssigning(true);
    try {
      await assignPermissionToRole(roleId, { permission_code: selectedPermissionCode });
      await loadPermissions();
      onNotify?.({
        variant: "success",
        message: "Gán quyền cho vai trò thành công. Người dùng cần đăng xuất và đăng nhập lại để cập nhật JWT.",
      });
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      onNotify?.({ variant: "error", message: error?.message || "Không thể gán quyền." });
    } finally {
      setIsAssigning(false);
    }
  };

  const handleRevokePermission = async (permissionCode) => {
    if (!roleId || !permissionCode) return;

    setRevokingCode(permissionCode);
    try {
      await revokePermissionFromRole(roleId, permissionCode);
      await loadPermissions();
      onNotify?.({
        variant: "success",
        message: "Thu hồi quyền thành công. Người dùng cần đăng xuất và đăng nhập lại để cập nhật JWT.",
      });
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      onNotify?.({ variant: "error", message: error?.message || "Không thể thu hồi quyền." });
    } finally {
      setRevokingCode("");
    }
  };

  return (
    <PermissionsOfRoleTabView
      title={TITLE}
      subtitle={SUBTITLE}
      roles={roles}
      rolesStatus={rolesStatus}
      roleId={roleId}
      roleMeta={roleMeta}
      status={status}
      errorMessage={errorMessage}
      permissions={permissions}
      availablePermissions={availablePermissions}
      selectedPermissionCode={selectedPermissionCode}
      isAssigning={isAssigning}
      revokingCode={revokingCode}
      isBusy={isBusy}
      onRoleIdChange={handleRoleIdChange}
      onLoadPermissions={loadPermissions}
      onPermissionCodeChange={setSelectedPermissionCode}
      onAssignPermission={handleAssignPermission}
      onRevokePermission={handleRevokePermission}
      onBackToRoleList={() => onTabChange?.("role-list")}
      onRetry={loadPermissions}
      onRolesRetry={loadRoles}
    />
  );
}

import { useCallback, useEffect, useState } from "react";
import { assignRoleToUser, getAdminRoles } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { resolveFieldErrors } from "../../utils/resolveFieldErrors.js";
import { AssignRoleTabView } from "./AssignRoleTabView.jsx";
import { AssignRoleConfirmModalView } from "./modals/AssignRoleConfirmModalView.jsx";
import { RbacUserListPanel } from "./RbacUserListPanel.jsx";

const TITLE = "Gán vai trò";
const SUBTITLE = "Gán vai trò cho người dùng trong hệ thống.";

export function AssignRoleTab({
  onNotify,
  rbacUserListFilters,
  rbacSelectedUserId,
  onRbacUserListFiltersChange,
  onRbacUserSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const [roles, setRoles] = useState([]);
  const [rolesStatus, setRolesStatus] = useState("loading");
  const [selectedUser, setSelectedUser] = useState(null);
  const [roleId, setRoleId] = useState("");
  const [fieldErrors, setFieldErrors] = useState({ userId: "", role_id: "" });
  const [globalError, setGlobalError] = useState("");
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [listRefreshKey, setListRefreshKey] = useState(0);

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

  useEffect(() => {
    loadRoles();
  }, [loadRoles]);

  useEffect(() => {
    if (!rbacSelectedUserId) {
      setSelectedUser(null);
      return;
    }
    setFieldErrors((prev) => ({ ...prev, userId: "" }));
  }, [rbacSelectedUserId]);

  const handleUserSelect = (userId, userRow) => {
    setSelectedUser(userRow || null);
    setRoleId("");
    setFieldErrors({ userId: "", role_id: "" });
    setGlobalError("");
    onRbacUserSelect?.(userId);
  };

  const handleClearUser = () => {
    setSelectedUser(null);
    setRoleId("");
    setFieldErrors({ userId: "", role_id: "" });
    setGlobalError("");
    onRbacUserSelect?.(null);
  };

  const selectedRole = roles.find((role) => role.id === roleId);

  const validateForm = () => {
    const next = { userId: "", role_id: "" };
    if (!rbacSelectedUserId) next.userId = "Vui lòng chọn người dùng.";
    if (!roleId) next.role_id = "Vui lòng chọn vai trò.";
    setFieldErrors(next);
    return !next.userId && !next.role_id;
  };

  const onRequestSubmit = (event) => {
    event.preventDefault();
    setGlobalError("");
    if (!validateForm()) return;
    setIsConfirmOpen(true);
  };

  const onConfirmAssign = async () => {
    setIsSubmitting(true);
    setGlobalError("");
    setFieldErrors({ userId: "", role_id: "" });

    try {
      await assignRoleToUser(rbacSelectedUserId, { role_id: roleId });
      setIsConfirmOpen(false);
      setRoleId("");
      setListRefreshKey((key) => key + 1);
      onNotify?.({ variant: "success", message: "Gán vai trò cho người dùng thành công." });
    } catch (error) {
      setIsConfirmOpen(false);
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      const serverErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 409 && serverErrors.role_id === "ALREADY_ASSIGNED") {
        setFieldErrors((prev) => ({
          ...prev,
          role_id: "Người dùng đã được gán vai trò này.",
        }));
      } else if (Object.keys(serverErrors).length > 0) {
        setFieldErrors((prev) => ({ ...prev, ...serverErrors }));
      }
      setGlobalError(error?.message || "Có lỗi xảy ra. Vui lòng thử lại.");
      onNotify?.({ variant: "error", message: error?.message });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AssignRoleTabView
      title={TITLE}
      subtitle={SUBTITLE}
      rolesStatus={rolesStatus}
      globalError={globalError}
      rbacSelectedUserId={rbacSelectedUserId}
      selectedUser={selectedUser}
      roles={roles}
      roleId={roleId}
      fieldErrors={fieldErrors}
      isSubmitting={isSubmitting}
      onRoleSelect={(nextRoleId) => {
        setRoleId(nextRoleId);
        setFieldErrors((prev) => ({ ...prev, role_id: "" }));
      }}
      onSubmit={onRequestSubmit}
      onRolesRetry={loadRoles}
      onClearUser={handleClearUser}
      userListPanel={
        <RbacUserListPanel
          userListFilters={rbacUserListFilters}
          onFiltersChange={onRbacUserListFiltersChange}
          selectedUserId={rbacSelectedUserId}
          onUserSelect={handleUserSelect}
          onSelectedUserSync={setSelectedUser}
          listRefreshKey={listRefreshKey}
        />
      }
      confirmModal={
        <AssignRoleConfirmModalView
          open={isConfirmOpen}
          selectedUser={selectedUser}
          selectedRole={selectedRole}
          isSubmitting={isSubmitting}
          onClose={() => setIsConfirmOpen(false)}
          onConfirm={onConfirmAssign}
        />
      }
    />
  );
}

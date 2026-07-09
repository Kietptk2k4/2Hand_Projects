import { useCallback, useEffect, useMemo, useState } from "react";
import { getAdminRoles, revokeRoleFromUser } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { resolveFieldErrors } from "../../utils/resolveFieldErrors.js";
import { RevokeRoleTabView } from "./RevokeRoleTabView.jsx";
import { RevokeRoleConfirmModalView } from "./modals/RevokeRoleConfirmModalView.jsx";
import { RbacUserListPanel } from "./RbacUserListPanel.jsx";

const TITLE = "Thu hồi vai trò";
const SUBTITLE = "Thu hồi vai trò đã gán cho người dùng.";

export function RevokeRoleTab({
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

  const userRoleCodes = selectedUser?.role_codes || [];
  const assignableRoles = useMemo(
    () => roles.filter((role) => userRoleCodes.includes(role.code)),
    [roles, userRoleCodes],
  );

  useEffect(() => {
    if (!rbacSelectedUserId) {
      setSelectedUser(null);
      setRoleId("");
      return;
    }
    if (assignableRoles.length === 1) {
      setRoleId(assignableRoles[0].id);
    } else {
      setRoleId("");
    }
    setFieldErrors((prev) => ({ ...prev, userId: "", role_id: "" }));
  }, [rbacSelectedUserId, assignableRoles]);

  const handleUserSelect = (userId, userRow) => {
    setSelectedUser(userRow || null);
    setRoleId("");
    setFieldErrors({ userId: "", role_id: "" });
    setGlobalError("");
    onRbacUserSelect?.(userId);
  };

  const selectedRole = roles.find((role) => role.id === roleId);

  const validateForm = () => {
    const next = { userId: "", role_id: "" };
    if (!rbacSelectedUserId) next.userId = "Vui lòng chọn người dùng.";
    if (assignableRoles.length === 0) next.role_id = "Người dùng chưa có vai trò nào.";
    else if (!roleId) next.role_id = "Vui lòng chọn vai trò.";
    setFieldErrors(next);
    return !next.userId && !next.role_id;
  };

  const onRequestSubmit = (event) => {
    event.preventDefault();
    setGlobalError("");
    if (!validateForm()) return;
    setIsConfirmOpen(true);
  };

  const onConfirmRevoke = async () => {
    setIsSubmitting(true);
    setGlobalError("");
    setFieldErrors({ userId: "", role_id: "" });

    try {
      await revokeRoleFromUser(rbacSelectedUserId, roleId);
      setIsConfirmOpen(false);
      setRoleId("");
      setListRefreshKey((key) => key + 1);
      onNotify?.({ variant: "success", message: "Thu hồi vai trò khỏi người dùng thành công." });
    } catch (error) {
      setIsConfirmOpen(false);
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      const serverErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 409 && serverErrors.role_id === "ROLE_NOT_ASSIGNED") {
        setRoleId("");
        setFieldErrors((prev) => ({
          ...prev,
          role_id: "Người dùng không còn vai trò này.",
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
    <RevokeRoleTabView
      title={TITLE}
      subtitle={SUBTITLE}
      rolesStatus={rolesStatus}
      globalError={globalError}
      rbacSelectedUserId={rbacSelectedUserId}
      selectedUser={selectedUser}
      assignableRoles={assignableRoles}
      roleId={roleId}
      fieldErrors={fieldErrors}
      isSubmitting={isSubmitting}
      onRoleSelect={(nextRoleId) => {
        setRoleId(nextRoleId);
        setFieldErrors((prev) => ({ ...prev, role_id: "" }));
      }}
      onSubmit={onRequestSubmit}
      onRolesRetry={loadRoles}
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
        <RevokeRoleConfirmModalView
          open={isConfirmOpen}
          selectedUser={selectedUser}
          selectedRole={selectedRole}
          isSubmitting={isSubmitting}
          onClose={() => setIsConfirmOpen(false)}
          onConfirm={onConfirmRevoke}
        />
      }
    />
  );
}

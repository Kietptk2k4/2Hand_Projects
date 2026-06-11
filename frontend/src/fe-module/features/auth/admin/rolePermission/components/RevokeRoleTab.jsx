import { useCallback, useEffect, useMemo, useState } from "react";
import { getAdminRoles, revokeRoleFromUser } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { resolveFieldErrors } from "../../utils/resolveFieldErrors.js";
import {
  AccountCard,
  AccountFieldLabel,
  AccountSkeleton,
  AuthAlert,
  PrimaryButton,
  SecondaryButton,
  TabPanelHeader,
} from "../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { RbacUserListPanel } from "./RbacUserListPanel.jsx";

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

  const selectedRole = roles.find((r) => r.id === roleId);

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

  if (rolesStatus === "loading") {
    return (
      <div>
        <TabPanelHeader title="Thu hồi vai trò" subtitle="Thu hồi vai trò đã gán cho người dùng." />
        <AccountSkeleton />
      </div>
    );
  }

  if (rolesStatus === "error") {
    return (
      <div>
        <TabPanelHeader title="Thu hồi vai trò" subtitle="Thu hồi vai trò đã gán cho người dùng." />
        <ErrorState message="Không tải được danh sách vai trò." />
        <PrimaryButton type="button" onClick={loadRoles} className="mt-4">
          Thử lại
        </PrimaryButton>
      </div>
    );
  }

  return (
    <div>
      <TabPanelHeader title="Thu hồi vai trò" subtitle="Thu hồi vai trò đã gán cho người dùng." />

      {globalError ? (
        <div className="mb-4">
          <AuthAlert variant="error" message={globalError} />
        </div>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,0.7fr)]">
        <RbacUserListPanel
          userListFilters={rbacUserListFilters}
          onFiltersChange={onRbacUserListFiltersChange}
          selectedUserId={rbacSelectedUserId}
          onUserSelect={handleUserSelect}
          onSelectedUserSync={setSelectedUser}
          listRefreshKey={listRefreshKey}
        />

        <AccountCard>
          <form onSubmit={onRequestSubmit} className="space-y-5" noValidate>
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
                Người dùng đã chọn
              </p>
              {rbacSelectedUserId ? (
                <div className="mt-2 text-sm text-on-surface">
                  <p className="font-medium">{selectedUser?.email || rbacSelectedUserId}</p>
                  {selectedUser?.display_name ? (
                    <p className="text-on-surface-variant">{selectedUser.display_name}</p>
                  ) : null}
                </div>
              ) : (
                <p className="mt-2 text-sm text-on-surface-variant">
                  Chọn một người dùng từ danh sách bên trái.
                </p>
              )}
              {fieldErrors.userId ? <p className="mt-1 text-sm text-error">{fieldErrors.userId}</p> : null}
            </div>

            <div className="flex flex-col gap-2">
              <AccountFieldLabel required>Vai trò cần thu hồi</AccountFieldLabel>
              {rbacSelectedUserId && assignableRoles.length === 0 ? (
                <p className="text-sm text-on-surface-variant">Người dùng chưa có vai trò nào.</p>
              ) : (
                <div className="space-y-2">
                  {assignableRoles.map((role) => (
                    <label
                      key={role.id}
                      className={`flex cursor-pointer items-start gap-3 rounded-lg border px-3 py-2.5 ${
                        roleId === role.id
                          ? "border-primary bg-primary/5"
                          : "border-outline-variant hover:bg-surface-container-low"
                      }`}
                    >
                      <input
                        type="radio"
                        name="revoke-role"
                        value={role.id}
                        checked={roleId === role.id}
                        onChange={() => {
                          setRoleId(role.id);
                          setFieldErrors((prev) => ({ ...prev, role_id: "" }));
                        }}
                        className="mt-1"
                      />
                      <span>
                        <span className="block text-sm font-medium text-on-surface">{role.code}</span>
                        <span className="block text-xs text-on-surface-variant">{role.name}</span>
                      </span>
                    </label>
                  ))}
                </div>
              )}
              {fieldErrors.role_id ? <p className="text-sm text-error">{fieldErrors.role_id}</p> : null}
            </div>

            <PrimaryButton
              type="submit"
              disabled={isSubmitting || !rbacSelectedUserId || assignableRoles.length === 0}
            >
              Thu hồi vai trò
            </PrimaryButton>
          </form>
        </AccountCard>
      </div>

      {isConfirmOpen ? (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-on-surface/40 p-4 backdrop-blur-sm"
          role="dialog"
          aria-modal="true"
          aria-labelledby="revoke-role-title"
          onClick={(e) => {
            if (e.target === e.currentTarget && !isSubmitting) setIsConfirmOpen(false);
          }}
        >
          <div className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-lg">
            <div className="p-6">
              <h3 id="revoke-role-title" className="text-lg font-semibold text-on-surface">
                Thu hồi vai trò?
              </h3>
              <p className="mt-2 text-sm text-on-surface-variant">
                Người dùng có thể mất quyền truy cập liên quan đến vai trò này.
              </p>
              {selectedUser && selectedRole ? (
                <p className="mt-3 text-sm text-on-surface">
                  <span className="font-medium">{selectedUser.email}</span>
                  {" → "}
                  <span className="font-medium">{selectedRole.code}</span>
                </p>
              ) : null}
            </div>
            <div className="flex justify-end gap-3 border-t border-outline-variant bg-account-surface-low px-6 py-4">
              <SecondaryButton type="button" disabled={isSubmitting} onClick={() => setIsConfirmOpen(false)}>
                Hủy
              </SecondaryButton>
              <PrimaryButton type="button" loading={isSubmitting} onClick={onConfirmRevoke}>
                Xác nhận
              </PrimaryButton>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

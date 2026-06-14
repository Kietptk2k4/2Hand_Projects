import { useCallback, useEffect, useMemo, useState } from "react";
import {
  assignPermissionToRole,
  getAdminRoles,
  getPermissionCatalog,
  getRolePermissions,
  revokePermissionFromRole,
} from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  AccountCard,
  AccountSkeleton,
  AuthAlert,
  PrimaryButton,
  SecondaryButton,
  TabPanelHeader,
} from "../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../shared/ui/PageState.jsx";

export function PermissionsOfRoleTab({
  selectedRoleId,
  onSelectedRoleIdChange,
  onTabChange,
  onNotify,
}) {
  const { showSessionExpired } = useAuthSession();
  const [roles, setRoles] = useState([]);
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
    if (selectedRoleId) setRoleId(selectedRoleId);
  }, [selectedRoleId]);

  const loadRoles = useCallback(async () => {
    try {
      const data = await getAdminRoles();
      setRoles(data?.roles || []);
    } catch (error) {
      if (error?.code === 401) showSessionExpired(error?.message);
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
    onSelectedRoleIdChange?.(roleId);

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
        setErrorMessage(error?.message || "Ban khong co quyen truy cap.");
        return;
      }
      if (error?.code === 404) {
        setStatus("not_found");
        setErrorMessage(error?.message || "Khong tim thay role.");
        return;
      }
      if (error?.code === 400) {
        setStatus("error");
        setErrorMessage(error?.message || "Ma vai tro khong hop le.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc permission.");
    }
  }, [roleId, onSelectedRoleIdChange, showSessionExpired]);

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
    <div>
      <TabPanelHeader
        title="Quyền của vai trò"
        subtitle="Xem, gán và thu hồi quyền cho từng vai trò."
      />

      <AccountCard className="mb-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
          <div className="min-w-0 flex-1">
            <label htmlFor="role-perm-select" className="mb-1.5 block text-xs font-semibold text-on-surface">
              Chọn vai trò
            </label>
            <select
              id="role-perm-select"
              value={roleId}
              onChange={(e) => setRoleId(e.target.value)}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2.5 text-base outline-none focus:border-primary"
            >
              <option value="">Chọn vai trò...</option>
              {roles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.code} — {role.name}
                </option>
              ))}
            </select>
          </div>
          <PrimaryButton type="button" onClick={loadPermissions} disabled={!roleId}>
            Tải permission
          </PrimaryButton>
        </div>
      </AccountCard>

      {status === "loading" ? <AccountSkeleton /> : null}

      {status === "forbidden" ? <ErrorState message={errorMessage} /> : null}

      {status === "not_found" ? (
        <AccountCard>
          <ErrorState message={errorMessage} />
          <SecondaryButton
            type="button"
            className="mt-4"
            onClick={() => onTabChange?.("role-list")}
          >
            Quay lại danh sách vai trò
          </SecondaryButton>
        </AccountCard>
      ) : null}

      {status === "error" ? (
        <AccountCard>
          <ErrorState message={errorMessage} />
          <PrimaryButton type="button" onClick={loadPermissions} className="mt-4">
            Thử lại
          </PrimaryButton>
        </AccountCard>
      ) : null}

      {status === "ready" && roleMeta ? (
        <>
          <AccountCard className="mb-4">
            <p className="text-sm text-on-surface-variant">Role</p>
            <p className="mt-1 text-lg font-semibold text-on-surface">
              {roleMeta.code} — {roleMeta.name}
            </p>
          </AccountCard>

          <AccountCard className="mb-4">
            <p className="text-sm font-semibold text-on-surface">Gán quyền mới</p>
            {availablePermissions.length === 0 ? (
              <p className="mt-2 text-sm text-on-surface-variant">
                Vai trò đã có tất cả quyền trong catalog.
              </p>
            ) : (
              <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-end">
                <div className="min-w-0 flex-1">
                  <label htmlFor="permission-assign-select" className="mb-1.5 block text-xs font-semibold text-on-surface">
                    Chọn quyền
                  </label>
                  <select
                    id="permission-assign-select"
                    value={selectedPermissionCode}
                    onChange={(e) => setSelectedPermissionCode(e.target.value)}
                    className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2.5 text-base outline-none focus:border-primary"
                  >
                    {availablePermissions.map((perm) => (
                      <option key={perm.code} value={perm.code}>
                        {perm.code}
                        {perm.description ? ` — ${perm.description}` : ""}
                      </option>
                    ))}
                  </select>
                </div>
                <PrimaryButton
                  type="button"
                  onClick={handleAssignPermission}
                  disabled={!selectedPermissionCode || isAssigning}
                >
                  {isAssigning ? "Đang gán..." : "Gán quyền"}
                </PrimaryButton>
              </div>
            )}
            <div className="mt-4">
              <AuthAlert
                variant="info"
                message="Sau khi gán hoặc thu hồi quyền, người dùng thuộc vai trò cần đăng xuất và đăng nhập lại để JWT nhận quyền mới."
              />
            </div>
          </AccountCard>

          {permissions.length === 0 ? (
            <EmptyState message="Vai trò chưa có quyền." />
          ) : (
            <AccountCard className="!p-0">
              <ul className="divide-y divide-outline-variant/50">
                {permissions.map((perm) => (
                  <li key={perm.code} className="flex flex-col gap-3 px-4 py-4 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <p className="font-medium text-on-surface">{perm.code}</p>
                      {perm.description ? (
                        <p className="mt-1 text-sm text-on-surface-variant">{perm.description}</p>
                      ) : null}
                    </div>
                    <SecondaryButton
                      type="button"
                      onClick={() => handleRevokePermission(perm.code)}
                      disabled={revokingCode === perm.code}
                    >
                      {revokingCode === perm.code ? "Đang thu hồi..." : "Thu hồi"}
                    </SecondaryButton>
                  </li>
                ))}
              </ul>
            </AccountCard>
          )}
        </>
      ) : null}
    </div>
  );
}

import { useCallback, useEffect, useState } from "react";
import { getAdminRoles, getRolePermissions } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  AccountCard,
  AccountSkeleton,
  PrimaryButton,
  SecondaryButton,
  TabPanelHeader,
} from "../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../shared/ui/PageState.jsx";

export function PermissionsOfRoleTab({ selectedRoleId, onSelectedRoleIdChange, onTabChange }) {
  const { showSessionExpired } = useAuthSession();
  const [roles, setRoles] = useState([]);
  const [roleId, setRoleId] = useState(selectedRoleId || "");
  const [roleMeta, setRoleMeta] = useState(null);
  const [permissions, setPermissions] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

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
        setErrorMessage(error?.message || "Role ID khong hop le.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc permission.");
    }
  }, [roleId, onSelectedRoleIdChange, showSessionExpired]);

  useEffect(() => {
    loadRoles();
  }, [loadRoles]);

  useEffect(() => {
    loadPermissions();
  }, [loadPermissions]);

  return (
    <div>
      <TabPanelHeader
        title="Permission cua Role"
        subtitle="Xem danh sach permission gan voi tung role."
      />

      <AccountCard className="mb-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
          <div className="min-w-0 flex-1">
            <label htmlFor="role-perm-select" className="mb-1.5 block text-xs font-semibold text-on-surface">
              Chon role
            </label>
            <select
              id="role-perm-select"
              value={roleId}
              onChange={(e) => setRoleId(e.target.value)}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2.5 text-base outline-none focus:border-primary"
            >
              <option value="">Chon role...</option>
              {roles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.code} — {role.name}
                </option>
              ))}
            </select>
          </div>
          <PrimaryButton type="button" onClick={loadPermissions} disabled={!roleId}>
            Tai permission
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
            Quay lai danh sach role
          </SecondaryButton>
        </AccountCard>
      ) : null}

      {status === "error" ? (
        <AccountCard>
          <ErrorState message={errorMessage} />
          <PrimaryButton type="button" onClick={loadPermissions} className="mt-4">
            Thu lai
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

          {permissions.length === 0 ? (
            <EmptyState message="Role chua co permission." />
          ) : (
            <AccountCard className="!p-0">
              <ul className="divide-y divide-outline-variant/50">
                {permissions.map((perm) => (
                  <li key={perm.code} className="px-4 py-4">
                    <p className="font-medium text-on-surface">{perm.code}</p>
                    {perm.description ? (
                      <p className="mt-1 text-sm text-on-surface-variant">{perm.description}</p>
                    ) : null}
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

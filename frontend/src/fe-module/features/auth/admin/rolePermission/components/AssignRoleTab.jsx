import { useCallback, useEffect, useState } from "react";
import { assignRoleToUser, getAdminRoles } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { ASSIGNABLE_USERS } from "../../constants/assignableUsers.js";
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

export function AssignRoleTab({ onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [roles, setRoles] = useState([]);
  const [rolesStatus, setRolesStatus] = useState("loading");
  const [userId, setUserId] = useState("");
  const [roleId, setRoleId] = useState("");
  const [fieldErrors, setFieldErrors] = useState({ userId: "", role_id: "" });
  const [globalError, setGlobalError] = useState("");
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

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

  const selectedUser = ASSIGNABLE_USERS.find((u) => u.id === userId);
  const selectedRole = roles.find((r) => r.id === roleId);

  const validateForm = () => {
    const next = { userId: "", role_id: "" };
    if (!userId) next.userId = "Vui long chon user.";
    if (!roleId) next.role_id = "Vui long chon role.";
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
      await assignRoleToUser(userId, { role_id: roleId });
      setIsConfirmOpen(false);
      setUserId("");
      setRoleId("");
      onNotify?.({ variant: "success", message: "Gan role cho user thanh cong." });
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
          role_id: "User da duoc gan role nay.",
        }));
      } else if (Object.keys(serverErrors).length > 0) {
        setFieldErrors((prev) => ({ ...prev, ...serverErrors }));
      }
      setGlobalError(error?.message || "Co loi xay ra. Vui long thu lai.");
      onNotify?.({ variant: "error", message: error?.message });
    } finally {
      setIsSubmitting(false);
    }
  };

  if (rolesStatus === "loading") {
    return (
      <div>
        <TabPanelHeader title="Gan Role" subtitle="Gan role cho user trong he thong." />
        <AccountSkeleton />
      </div>
    );
  }

  if (rolesStatus === "error") {
    return (
      <div>
        <TabPanelHeader title="Gan Role" subtitle="Gan role cho user trong he thong." />
        <ErrorState message="Khong tai duoc danh sach role." />
        <PrimaryButton type="button" onClick={loadRoles} className="mt-4">
          Thu lai
        </PrimaryButton>
      </div>
    );
  }

  return (
    <div>
      <TabPanelHeader title="Gan Role" subtitle="Gan role cho user trong he thong." />

      {globalError ? (
        <div className="mb-4">
          <AuthAlert variant="error" message={globalError} />
        </div>
      ) : null}

      <AccountCard>
        <form onSubmit={onRequestSubmit} className="space-y-5" noValidate>
          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="assign-user" required>
              User
            </AccountFieldLabel>
            <select
              id="assign-user"
              value={userId}
              onChange={(e) => {
                setUserId(e.target.value);
                setFieldErrors((prev) => ({ ...prev, userId: "" }));
              }}
              className={[
                "w-full rounded-lg border bg-white px-3 py-2.5 text-base outline-none",
                fieldErrors.userId ? "border-error" : "border-outline-variant focus:border-primary",
              ].join(" ")}
            >
              <option value="">Chon user...</option>
              {ASSIGNABLE_USERS.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.email} ({user.display_name})
                </option>
              ))}
            </select>
            {fieldErrors.userId ? <p className="text-sm text-error">{fieldErrors.userId}</p> : null}
          </div>

          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="assign-role" required>
              Role
            </AccountFieldLabel>
            <select
              id="assign-role"
              value={roleId}
              onChange={(e) => {
                setRoleId(e.target.value);
                setFieldErrors((prev) => ({ ...prev, role_id: "" }));
              }}
              className={[
                "w-full rounded-lg border bg-white px-3 py-2.5 text-base outline-none",
                fieldErrors.role_id ? "border-error" : "border-outline-variant focus:border-primary",
              ].join(" ")}
            >
              <option value="">Chon role...</option>
              {roles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.code} — {role.name}
                </option>
              ))}
            </select>
            {fieldErrors.role_id ? <p className="text-sm text-error">{fieldErrors.role_id}</p> : null}
          </div>

          <PrimaryButton type="submit" disabled={isSubmitting}>
            Gan role
          </PrimaryButton>
        </form>
      </AccountCard>

      {isConfirmOpen ? (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-on-surface/40 p-4 backdrop-blur-sm"
          role="dialog"
          aria-modal="true"
          aria-labelledby="assign-role-title"
          onClick={(e) => {
            if (e.target === e.currentTarget && !isSubmitting) setIsConfirmOpen(false);
          }}
        >
          <div className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-lg">
            <div className="p-6">
              <h3 id="assign-role-title" className="text-lg font-semibold text-on-surface">
                Gan role cho user?
              </h3>
              <p className="mt-2 text-sm text-on-surface-variant">
                Thao tac nay se cap nhat quyen truy cap cua user.
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
                Huy
              </SecondaryButton>
              <PrimaryButton type="button" loading={isSubmitting} onClick={onConfirmAssign}>
                Xac nhan
              </PrimaryButton>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

import { useCallback, useEffect, useState } from "react";
import { getAdminRoles, revokeRoleFromUser } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { ASSIGNABLE_USERS } from "../../constants/assignableUsers.js";
import { USER_ROLE_ASSIGNMENTS } from "../../constants/userRoleAssignments.js";
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

export function RevokeRoleTab({ onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [roles, setRoles] = useState([]);
  const [rolesStatus, setRolesStatus] = useState("loading");
  const [assignments, setAssignments] = useState(() => ({ ...USER_ROLE_ASSIGNMENTS }));
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

  const assignedRoleId = userId ? assignments[userId] : "";
  const assignableRoles = assignedRoleId ? roles.filter((r) => r.id === assignedRoleId) : [];
  const selectedUser = ASSIGNABLE_USERS.find((u) => u.id === userId);
  const selectedRole = roles.find((r) => r.id === roleId);

  const onUserChange = (nextUserId) => {
    setUserId(nextUserId);
    setFieldErrors((prev) => ({ ...prev, userId: "", role_id: "" }));
    setGlobalError("");
    const nextRoleId = nextUserId ? assignments[nextUserId] || "" : "";
    setRoleId(nextRoleId);
  };

  const validateForm = () => {
    const next = { userId: "", role_id: "" };
    if (!userId) next.userId = "Vui long chon user.";
    if (!assignedRoleId) next.role_id = "User chua co role nao.";
    else if (!roleId) next.role_id = "Vui long chon role.";
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
      await revokeRoleFromUser(userId, roleId);
      setIsConfirmOpen(false);
      setAssignments((prev) => {
        const next = { ...prev };
        delete next[userId];
        return next;
      });
      setUserId("");
      setRoleId("");
      onNotify?.({ variant: "success", message: "Thu hoi role khoi user thanh cong." });
    } catch (error) {
      setIsConfirmOpen(false);
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      const serverErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 409 && serverErrors.role_id === "ROLE_NOT_ASSIGNED") {
        setAssignments((prev) => {
          const next = { ...prev };
          delete next[userId];
          return next;
        });
        setRoleId("");
        setFieldErrors((prev) => ({
          ...prev,
          role_id: "User khong con role nay.",
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
        <TabPanelHeader title="Thu hoi Role" subtitle="Thu hoi role da gan cho user." />
        <AccountSkeleton />
      </div>
    );
  }

  if (rolesStatus === "error") {
    return (
      <div>
        <TabPanelHeader title="Thu hoi Role" subtitle="Thu hoi role da gan cho user." />
        <ErrorState message="Khong tai duoc danh sach role." />
        <PrimaryButton type="button" onClick={loadRoles} className="mt-4">
          Thu lai
        </PrimaryButton>
      </div>
    );
  }

  return (
    <div>
      <TabPanelHeader title="Thu hoi Role" subtitle="Thu hoi role da gan cho user." />

      {globalError ? (
        <div className="mb-4">
          <AuthAlert variant="error" message={globalError} />
        </div>
      ) : null}

      <AccountCard>
        <form onSubmit={onRequestSubmit} className="space-y-5" noValidate>
          <div className="flex flex-col gap-1.5">
            <AccountFieldLabel htmlFor="revoke-user" required>
              User
            </AccountFieldLabel>
            <select
              id="revoke-user"
              value={userId}
              onChange={(e) => onUserChange(e.target.value)}
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
            <AccountFieldLabel htmlFor="revoke-role" required>
              Role
            </AccountFieldLabel>
            <select
              id="revoke-role"
              value={roleId}
              onChange={(e) => {
                setRoleId(e.target.value);
                setFieldErrors((prev) => ({ ...prev, role_id: "" }));
              }}
              disabled={!userId || !assignedRoleId}
              className={[
                "w-full rounded-lg border bg-white px-3 py-2.5 text-base outline-none disabled:cursor-not-allowed disabled:bg-account-surface-low",
                fieldErrors.role_id ? "border-error" : "border-outline-variant focus:border-primary",
              ].join(" ")}
            >
              <option value="">Chon role...</option>
              {assignableRoles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.code} — {role.name}
                </option>
              ))}
            </select>
            {userId && !assignedRoleId ? (
              <p className="text-sm text-on-surface-variant">User chua co role nao.</p>
            ) : null}
            {fieldErrors.role_id ? <p className="text-sm text-error">{fieldErrors.role_id}</p> : null}
          </div>

          <PrimaryButton type="submit" disabled={isSubmitting || !assignedRoleId}>
            Thu hoi role
          </PrimaryButton>
        </form>
      </AccountCard>

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
                Thu hoi role?
              </h3>
              <p className="mt-2 text-sm text-on-surface-variant">
                Nguoi dung co the mat quyen truy cap lien quan den role nay.
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
              <PrimaryButton type="button" loading={isSubmitting} onClick={onConfirmRevoke}>
                Xac nhan
              </PrimaryButton>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

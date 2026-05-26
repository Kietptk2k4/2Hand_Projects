import { useCallback, useEffect, useState } from "react";
import { getAdminRoles } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AccountCard, AccountSkeleton, PrimaryButton, TabPanelHeader } from "../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../shared/ui/PageState.jsx";

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
        setErrorMessage(error?.message || "Ban khong co quyen truy cap.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc danh sach role.");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader title="Danh sach Role" subtitle="Xem tat ca role trong he thong." />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <TabPanelHeader title="Danh sach Role" subtitle="Xem tat ca role trong he thong." />
        <ErrorState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader title="Danh sach Role" subtitle="Xem tat ca role trong he thong." />
        <AccountCard>
          <ErrorState message={errorMessage} />
          <PrimaryButton type="button" onClick={load} className="mt-4">
            Thu lai
          </PrimaryButton>
        </AccountCard>
      </div>
    );
  }

  return (
    <div>
      <TabPanelHeader title="Danh sach Role" subtitle="Xem tat ca role trong he thong." />

      {roles.length === 0 ? (
        <EmptyState message="Khong co role nao." />
      ) : (
        <AccountCard className="overflow-x-auto !p-0">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead className="border-b border-outline-variant bg-account-surface-low">
              <tr>
                <th className="px-4 py-3 font-semibold text-on-surface">Code</th>
                <th className="px-4 py-3 font-semibold text-on-surface">Ten</th>
                <th className="px-4 py-3 font-semibold text-on-surface">Tao luc</th>
                <th className="px-4 py-3 font-semibold text-on-surface">Cap nhat</th>
                <th className="px-4 py-3 font-semibold text-on-surface">Thao tac</th>
              </tr>
            </thead>
            <tbody>
              {roles.map((role) => (
                <tr key={role.id} className="border-b border-outline-variant/50 last:border-0">
                  <td className="px-4 py-3 font-medium text-on-surface">{role.code}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{role.name}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{formatDateTime(role.created_at)}</td>
                  <td className="px-4 py-3 text-on-surface-variant">{formatDateTime(role.updated_at)}</td>
                  <td className="px-4 py-3">
                    <button
                      type="button"
                      onClick={() => onViewRolePermissions?.(role.id)}
                      className="text-sm font-medium text-primary hover:underline"
                    >
                      Xem permission
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </AccountCard>
      )}
    </div>
  );
}

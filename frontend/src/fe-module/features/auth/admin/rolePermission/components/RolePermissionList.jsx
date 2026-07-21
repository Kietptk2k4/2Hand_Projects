import { useMemo, useState } from "react";
import {
  AdminFilterButton,
  AdminFilterInput,
  AdminSurfaceCard,
} from "../../components/ui";
import { groupPermissionsByDomain } from "../utils/groupPermissionsByDomain.js";

export function RolePermissionList({
  permissions,
  revokingCode,
  onRevoke,
}) {
  const [query, setQuery] = useState("");

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return permissions || [];
    return (permissions || []).filter((perm) => {
      const haystack = `${perm.code || ""} ${perm.description || ""}`.toLowerCase();
      return haystack.includes(normalized);
    });
  }, [permissions, query]);

  const groups = useMemo(() => groupPermissionsByDomain(filtered), [filtered]);

  if (!permissions?.length) {
    return (
      <AdminSurfaceCard padding="lg" className="text-center">
        <span
          className="material-symbols-outlined text-3xl text-admin-text-muted"
          aria-hidden="true"
        >
          key_off
        </span>
        <p className="mt-3 text-sm font-medium text-admin-text">Vai trò chưa có quyền</p>
        <p className="mt-1 text-sm text-admin-text-muted">
          Dùng form “Gán quyền mới” phía trên để thêm quyền từ catalog.
        </p>
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <div className="flex flex-col gap-3 border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:px-5">
        <div>
          <p className="text-sm font-semibold text-admin-text">Quyền đã gán</p>
          <p className="mt-0.5 text-xs text-admin-text-muted">
            <span className="tabular-nums">{filtered.length}</span>
            {query.trim() ? ` / ${permissions.length}` : ""} quyền
            {groups.length > 1 ? ` · ${groups.length} nhóm` : ""}
          </p>
        </div>
        <div className="w-full sm:max-w-xs">
          <AdminFilterInput
            id="role-permission-list-search"
            type="search"
            value={query}
            placeholder="Lọc theo mã hoặc mô tả..."
            aria-label="Lọc quyền đã gán"
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>
      </div>

      {filtered.length === 0 ? (
        <p className="px-4 py-10 text-center text-sm text-admin-text-muted sm:px-5">
          Không có quyền khớp “{query.trim()}”.
        </p>
      ) : (
        <div className="divide-y divide-admin-border-subtle">
          {groups.map((group) => (
            <section key={group.domain} aria-labelledby={`perm-group-${group.domain}`}>
              <div className="flex items-center justify-between gap-2 bg-admin-canvas/80 px-4 py-2 sm:px-5">
                <h3
                  id={`perm-group-${group.domain}`}
                  className="text-xs font-semibold tracking-wide text-admin-text-secondary uppercase"
                >
                  {group.label}
                </h3>
                <span className="tabular-nums text-xs text-admin-text-muted">
                  {group.items.length}
                </span>
              </div>
              <ul className="divide-y divide-admin-border-subtle">
                {group.items.map((perm) => (
                  <li
                    key={perm.code}
                    className="flex flex-col gap-3 px-4 py-3.5 transition-colors duration-200 hover:bg-admin-surface-muted/60 sm:flex-row sm:items-center sm:justify-between sm:px-5"
                  >
                    <div className="min-w-0">
                      <p className="break-all font-mono text-sm font-medium text-admin-text">
                        {perm.code}
                      </p>
                      {perm.description ? (
                        <p className="mt-1 text-sm text-admin-text-secondary">
                          {perm.description}
                        </p>
                      ) : null}
                    </div>
                    <AdminFilterButton
                      type="button"
                      variant="secondary"
                      className="w-full min-h-10 shrink-0 sm:w-auto"
                      disabled={revokingCode === perm.code}
                      onClick={() => onRevoke(perm.code)}
                    >
                      {revokingCode === perm.code ? "Đang thu hồi..." : "Thu hồi"}
                    </AdminFilterButton>
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      )}
    </AdminSurfaceCard>
  );
}

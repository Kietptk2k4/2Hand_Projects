import { useMemo, useState } from "react";
import { AdminFilterInput, AdminSurfaceCard } from "../../components/ui";
import { groupPermissionsByDomain } from "../utils/groupPermissionsByDomain.js";

export function UserPermissionChipList({ permissions }) {
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
        <p className="mt-3 text-sm font-medium text-admin-text">Chưa có quyền hiệu lực</p>
        <p className="mt-1 text-sm text-admin-text-muted">
          Người dùng này chưa nhận quyền nào từ vai trò được gán.
        </p>
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-3">
        <div className="flex items-baseline justify-between gap-2">
          <p className="text-sm font-semibold text-admin-text">Quyền hiệu lực</p>
          <p className="tabular-nums text-xs text-admin-text-muted">
            {filtered.length}
            {query.trim() ? ` / ${permissions.length}` : ""} mã
          </p>
        </div>
        <div className="mt-3">
          <AdminFilterInput
            id="user-permission-chip-search"
            type="search"
            value={query}
            placeholder="Lọc theo mã..."
            aria-label="Lọc quyền hiệu lực"
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>
      </div>

      {filtered.length === 0 ? (
        <p className="px-4 py-8 text-center text-sm text-admin-text-muted">
          Không có quyền khớp “{query.trim()}”.
        </p>
      ) : (
        <div className="max-h-[min(32rem,55dvh)] divide-y divide-admin-border-subtle overflow-y-auto">
          {groups.map((group) => (
            <section key={group.domain} aria-labelledby={`user-perm-group-${group.domain}`}>
              <div className="flex items-center justify-between gap-2 bg-admin-canvas/80 px-4 py-2">
                <h3
                  id={`user-perm-group-${group.domain}`}
                  className="text-xs font-semibold tracking-wide text-admin-text-secondary uppercase"
                >
                  {group.label}
                </h3>
                <span className="tabular-nums text-xs text-admin-text-muted">
                  {group.items.length}
                </span>
              </div>
              <div className="flex flex-wrap gap-1.5 px-4 py-3">
                {group.items.map((perm) => (
                  <span
                    key={perm.code}
                    title={perm.description || perm.code}
                    className="inline-flex max-w-full break-all rounded-md border border-admin-border bg-admin-surface-muted/70 px-2 py-1 font-mono text-[11px] font-medium tracking-wide text-admin-text transition-colors duration-200 hover:border-admin-accent-border hover:bg-admin-accent-soft/50"
                  >
                    {perm.code}
                  </span>
                ))}
              </div>
            </section>
          ))}
        </div>
      )}
    </AdminSurfaceCard>
  );
}

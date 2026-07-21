import { useMemo, useState } from "react";
import { AdminFilterInput, AdminSurfaceCard } from "../../components/ui";
import { RoleCodeBadge } from "./RoleCodeBadge.jsx";

export function RolePermissionPicker({
  roles = [],
  selectedRoleId,
  onSelect,
  disabled = false,
}) {
  const [query, setQuery] = useState("");

  const filteredRoles = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return roles;
    return roles.filter((role) => {
      const haystack = `${role.code || ""} ${role.name || ""}`.toLowerCase();
      return haystack.includes(normalized);
    });
  }, [roles, query]);

  return (
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-3">
        <div className="flex items-baseline justify-between gap-2">
          <p className="text-sm font-semibold text-admin-text">Vai trò</p>
          <p className="tabular-nums text-xs text-admin-text-muted">
            {filteredRoles.length}/{roles.length}
          </p>
        </div>
        <div className="mt-3">
          <AdminFilterInput
            id="role-permission-picker-search"
            type="search"
            value={query}
            disabled={disabled || roles.length === 0}
            placeholder="Tìm theo mã hoặc tên..."
            aria-label="Tìm vai trò"
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>
      </div>

      {roles.length === 0 ? (
        <p className="px-4 py-8 text-center text-sm text-admin-text-muted">
          Chưa có vai trò nào.
        </p>
      ) : filteredRoles.length === 0 ? (
        <p className="px-4 py-8 text-center text-sm text-admin-text-muted">
          Không có vai trò khớp “{query.trim()}”.
        </p>
      ) : (
        <ul className="max-h-[min(28rem,55dvh)] divide-y divide-admin-border-subtle overflow-y-auto" aria-label="Danh sách vai trò">
          {filteredRoles.map((role) => {
            const isSelected = selectedRoleId === role.id;
            return (
              <li key={role.id}>
                <button
                  type="button"
                  aria-pressed={isSelected}
                  disabled={disabled}
                  onClick={() => onSelect?.(role.id)}
                  className={[
                    "flex w-full items-start gap-3 px-4 py-3 text-left transition-colors duration-200",
                    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-admin-accent-soft",
                    "active:scale-[0.99]",
                    isSelected
                      ? "bg-admin-accent-soft/60 shadow-[inset_3px_0_0_0_var(--color-admin-accent)]"
                      : "hover:bg-admin-surface-muted",
                    disabled ? "cursor-not-allowed opacity-60" : "cursor-pointer",
                  ].join(" ")}
                >
                  <span className="min-w-0 flex-1">
                    <RoleCodeBadge code={role.code} />
                    <span className="mt-1.5 block truncate text-sm text-admin-text">
                      {role.name}
                    </span>
                  </span>
                  {isSelected ? (
                    <span
                      className="material-symbols-outlined mt-0.5 shrink-0 text-base text-admin-accent"
                      aria-hidden="true"
                    >
                      check
                    </span>
                  ) : null}
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </AdminSurfaceCard>
  );
}

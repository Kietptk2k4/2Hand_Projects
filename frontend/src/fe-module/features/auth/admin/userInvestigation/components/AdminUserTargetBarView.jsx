import { AdminSurfaceCard } from "../../components/ui";
import { UserStatusBadge } from "./EnforcementBadges.jsx";

function getInitials(user, fallbackId) {
  const source = user?.display_name?.trim() || user?.email?.trim() || fallbackId || "";
  if (!source) return "?";
  const parts = source.split(/[\s@._-]+/).filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }
  return source.slice(0, 2).toUpperCase();
}

export function AdminUserTargetBarView({
  listboxId,
  containerRef,
  query,
  userId,
  selectedUser,
  searchStatus,
  searchError,
  showDropdown,
  results,
  minSearchLength,
  onInputChange,
  onInputFocus,
  onSelectUser,
  onClear,
  formatUserSummary,
}) {
  const hasTarget = Boolean(userId);

  return (
    <AdminSurfaceCard padding="none" className="mb-4 overflow-hidden">
      {hasTarget ? (
        <div className="flex flex-col gap-3 border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-3 sm:flex-row sm:items-center sm:justify-between lg:px-5">
          <div className="flex min-w-0 items-start gap-3">
            <span
              className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-admin-accent-soft text-sm font-semibold text-admin-accent-strong"
              aria-hidden="true"
            >
              {getInitials(selectedUser, userId)}
            </span>
            <div className="min-w-0">
              <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
                Đang điều tra
              </p>
              <p className="mt-0.5 truncate text-sm font-semibold text-admin-text">
                {selectedUser?.email || formatUserSummary?.(selectedUser) || userId}
              </p>
              {selectedUser?.display_name ? (
                <p className="truncate text-sm text-admin-text-secondary">
                  {selectedUser.display_name}
                </p>
              ) : null}
              <p className="mt-1 break-all font-mono text-[11px] text-admin-text-muted">
                {selectedUser?.user_id || userId}
              </p>
            </div>
          </div>
          <div className="flex shrink-0 flex-wrap items-center gap-2">
            {selectedUser?.status ? <UserStatusBadge status={selectedUser.status} /> : null}
            <button
              type="button"
              onClick={onClear}
              className="inline-flex min-h-9 items-center rounded-lg border border-admin-border bg-admin-surface px-3 py-1.5 text-sm font-medium text-admin-text-secondary transition-colors duration-200 hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft active:scale-[0.98]"
            >
              Bỏ chọn
            </button>
          </div>
        </div>
      ) : null}

      <div ref={containerRef} className="px-4 py-3 lg:px-5">
        <label
          htmlFor="investigation-user-search"
          className="mb-1.5 block text-xs font-medium text-admin-text-secondary"
        >
          {hasTarget ? "Đổi người dùng" : "Người dùng điều tra"}
        </label>
        <div className="relative">
          <input
            id="investigation-user-search"
            type="search"
            value={query}
            onChange={onInputChange}
            onFocus={onInputFocus}
            placeholder="Tìm theo email hoặc UUID…"
            autoComplete="off"
            role="combobox"
            aria-expanded={showDropdown}
            aria-controls={listboxId}
            aria-autocomplete="list"
            className="w-full min-h-11 rounded-lg border border-admin-border bg-admin-surface px-3 py-2.5 text-sm text-admin-text outline-none transition-colors placeholder:text-admin-text-muted focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft"
          />

          {showDropdown ? (
            <ul
              id={listboxId}
              aria-label="Kết quả tìm kiếm người dùng"
              className="absolute z-20 mt-1 max-h-60 w-full overflow-auto rounded-lg border border-admin-border bg-admin-surface py-1 shadow-[var(--shadow-admin-surface)]"
            >
              {searchStatus === "loading" ? (
                <li className="min-h-11 px-3 py-2.5 text-sm text-admin-text-muted">Đang tìm…</li>
              ) : null}
              {searchStatus === "error" ? (
                <li className="min-h-11 px-3 py-2.5 text-sm text-admin-danger">{searchError}</li>
              ) : null}
              {searchStatus === "ready" && results.length === 0 ? (
                <li className="min-h-11 px-3 py-2.5 text-sm text-admin-text-muted">
                  Không tìm thấy người dùng phù hợp.
                </li>
              ) : null}
              {results.map((user) => (
                <li key={user.user_id}>
                  <button
                    type="button"
                    onClick={() => onSelectUser(user)}
                    className="flex min-h-11 w-full flex-col justify-center px-3 py-2 text-left transition-colors duration-200 hover:bg-admin-surface-muted focus-visible:outline-none focus-visible:bg-admin-accent-soft/40 active:scale-[0.99]"
                  >
                    <span className="block text-sm font-medium text-admin-text">
                      {formatUserSummary(user)}
                    </span>
                    <span className="mt-0.5 block break-all text-xs text-admin-text-muted">
                      {user.user_id}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          ) : null}
        </div>

        {!hasTarget ? (
          <p className="mt-2 text-xs text-admin-text-muted">
            Nhập ít nhất {minSearchLength} ký tự email, hoặc dán UUID. Cũng có thể chọn từ danh sách bên dưới.
          </p>
        ) : null}
      </div>
    </AdminSurfaceCard>
  );
}

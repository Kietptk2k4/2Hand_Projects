import { AdminSurfaceCard } from "../../components/ui";

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
  return (
    <AdminSurfaceCard padding="md" className="mb-6">
      <div ref={containerRef}>
        <label
          htmlFor="investigation-user-search"
          className="mb-1.5 block text-xs font-medium text-admin-text-secondary"
        >
          Người dùng điều tra
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
            className="w-full min-h-11 rounded-lg border border-admin-border bg-admin-surface px-3 py-2.5 pr-16 text-base text-admin-text outline-none transition-colors placeholder:text-admin-text-muted focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft"
          />
          {userId ? (
            <button
              type="button"
              onClick={onClear}
              className="absolute right-2 top-1/2 flex min-h-11 -translate-y-1/2 items-center rounded-lg px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted"
              aria-label="Xóa người dùng đã chọn"
            >
              Xóa
            </button>
          ) : null}

          {showDropdown ? (
            <ul
              id={listboxId}
              role="listbox"
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
                <li key={user.user_id} role="option">
                  <button
                    type="button"
                    onClick={() => onSelectUser(user)}
                    className="flex min-h-11 w-full flex-col justify-center px-3 py-2 text-left transition-colors hover:bg-admin-surface-muted"
                  >
                    <span className="block text-sm font-medium text-admin-text">
                      {formatUserSummary(user)}
                    </span>
                    <span className="mt-0.5 block break-all text-xs text-admin-text-muted">
                      {user.user_id} · {user.status}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          ) : null}
        </div>

        {selectedUser ? (
          <p className="mt-2 break-all text-xs text-admin-text-muted">
            User ID: {selectedUser.user_id}
          </p>
        ) : userId ? (
          <p className="mt-2 break-all text-xs text-admin-text-muted">User ID: {userId}</p>
        ) : (
          <p className="mt-2 text-xs text-admin-text-muted">
            Nhập ít nhất {minSearchLength} ký tự email hoặc dán UUID để tìm người dùng thật từ
            auth-service.
          </p>
        )}
      </div>
    </AdminSurfaceCard>
  );
}

import { AdminFilterInput } from "../../components/ui";

function getInitials(admin, fallbackId) {
  const source = admin?.displayName?.trim() || admin?.email?.trim() || fallbackId || "";
  if (!source) return "?";
  const parts = source.split(/[\s@._-]+/).filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }
  return source.slice(0, 2).toUpperCase();
}

export function AuditAdminPickerView({
  listboxId,
  containerRef,
  query,
  adminId,
  selectedAdmin,
  searchStatus,
  searchError,
  showDropdown,
  results,
  minSearchLength,
  onInputChange,
  onInputFocus,
  onSelectAdmin,
  onClear,
  formatAdminSummary,
}) {
  const hasSelection = Boolean(adminId);

  return (
    <div ref={containerRef} className="relative min-w-0">
      {hasSelection ? (
        <div className="mb-2 flex items-start justify-between gap-2 rounded-lg border border-admin-accent-border bg-admin-accent-soft/60 px-3 py-2">
          <div className="flex min-w-0 items-start gap-2.5">
            <span
              className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-admin-surface text-xs font-semibold text-admin-accent-strong"
              aria-hidden="true"
            >
              {getInitials(selectedAdmin, adminId)}
            </span>
            <div className="min-w-0">
              <p className="truncate text-sm font-medium text-admin-text">
                {selectedAdmin?.email || formatAdminSummary?.(selectedAdmin) || adminId}
              </p>
              {selectedAdmin?.displayName ? (
                <p className="truncate text-xs text-admin-text-secondary">
                  {selectedAdmin.displayName}
                </p>
              ) : null}
              <p className="mt-0.5 break-all font-mono text-[11px] text-admin-text-muted">
                {adminId}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClear}
            className="inline-flex min-h-8 shrink-0 items-center rounded-lg border border-admin-border bg-admin-surface px-2.5 text-xs font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          >
            Bỏ chọn
          </button>
        </div>
      ) : null}

      <label htmlFor="audit-admin-search" className="mb-1.5 block text-xs font-medium text-admin-text-secondary">
        {hasSelection ? "Đổi admin" : "Admin thực hiện"}
      </label>
      <AdminFilterInput
        id="audit-admin-search"
        type="search"
        value={query}
        onChange={onInputChange}
        onFocus={onInputFocus}
        placeholder="Email, tên hoặc UUID admin..."
        autoComplete="off"
        role="combobox"
        aria-expanded={showDropdown}
        aria-controls={listboxId}
      />

      {showDropdown ? (
        <div
          id={listboxId}
          role="listbox"
          className="absolute z-20 mt-1 max-h-64 w-full overflow-y-auto rounded-lg border border-admin-border bg-admin-surface py-1 shadow-[var(--shadow-admin-surface)]"
        >
          {searchStatus === "loading" ? (
            <p className="px-3 py-2 text-sm text-admin-text-muted">Đang tìm...</p>
          ) : null}
          {searchStatus === "error" ? (
            <p className="px-3 py-2 text-sm text-admin-danger">{searchError}</p>
          ) : null}
          {searchStatus === "ready" && !results.length ? (
            <p className="px-3 py-2 text-sm text-admin-text-muted">
              {query.trim().length < minSearchLength
                ? `Nhập ít nhất ${minSearchLength} ký tự hoặc UUID.`
                : "Không tìm thấy admin phù hợp."}
            </p>
          ) : null}
          {results.map((admin) => (
            <button
              key={admin.id}
              type="button"
              role="option"
              className="flex w-full items-start gap-2 px-3 py-2 text-left text-sm transition-colors hover:bg-admin-surface-muted focus-visible:bg-admin-surface-muted focus-visible:outline-none"
              onClick={() => onSelectAdmin?.(admin)}
            >
              <span className="min-w-0 flex-1">
                <span className="block truncate font-medium text-admin-text">{admin.email}</span>
                {admin.displayName ? (
                  <span className="block truncate text-xs text-admin-text-secondary">
                    {admin.displayName}
                  </span>
                ) : null}
              </span>
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}

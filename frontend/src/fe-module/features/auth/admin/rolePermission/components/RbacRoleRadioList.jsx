export function RbacRoleRadioList({
  name,
  roles,
  selectedRoleId,
  onSelect,
  fieldError,
  emptyMessage,
  label = "Vai trò",
  required = true,
  disabled = false,
}) {
  return (
    <div
      className={[
        "flex flex-col gap-2 transition-opacity",
        disabled ? "pointer-events-none opacity-60" : "",
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <p className="text-sm font-medium text-admin-text">
        {label}
        {required ? <span className="text-admin-danger"> *</span> : null}
      </p>

      {emptyMessage ? (
        <p className="text-sm text-admin-text-muted">{emptyMessage}</p>
      ) : (
        <div className="space-y-2" role="radiogroup" aria-label={label} aria-disabled={disabled}>
          {roles.map((role) => {
            const isSelected = selectedRoleId === role.id;
            return (
              <label
                key={role.id}
                className={[
                  "flex w-full cursor-pointer items-start gap-3 rounded-lg border px-3 py-2.5 transition-colors",
                  isSelected
                    ? "border-admin-accent bg-admin-accent-soft/50 shadow-[inset_3px_0_0_0_var(--color-admin-accent)]"
                    : "border-admin-border hover:bg-admin-surface-muted",
                  disabled ? "cursor-not-allowed" : "",
                ].join(" ")}
              >
                <input
                  type="radio"
                  name={name}
                  value={role.id}
                  checked={isSelected}
                  disabled={disabled}
                  onChange={() => onSelect(role.id)}
                  className="mt-1"
                />
                <span className="min-w-0">
                  <span className="block font-mono text-sm font-medium text-admin-text">
                    {role.code}
                  </span>
                  <span className="block text-xs text-admin-text-secondary">{role.name}</span>
                </span>
              </label>
            );
          })}
        </div>
      )}

      {fieldError ? <p className="text-sm text-admin-danger">{fieldError}</p> : null}
    </div>
  );
}

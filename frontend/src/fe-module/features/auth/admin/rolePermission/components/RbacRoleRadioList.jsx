export function RbacRoleRadioList({
  name,
  roles,
  selectedRoleId,
  onSelect,
  fieldError,
  emptyMessage,
  label = "Vai trò",
  required = true,
}) {
  return (
    <div className="flex flex-col gap-2">
      <p className="text-sm font-medium text-admin-text">
        {label}
        {required ? <span className="text-admin-danger"> *</span> : null}
      </p>

      {emptyMessage ? (
        <p className="text-sm text-admin-text-muted">{emptyMessage}</p>
      ) : (
        <div className="space-y-2">
          {roles.map((role) => (
            <label
              key={role.id}
              className={[
                "flex w-full cursor-pointer items-start gap-3 rounded-lg border px-3 py-2.5 transition-colors",
                selectedRoleId === role.id
                  ? "border-admin-accent-border bg-admin-accent-soft/40"
                  : "border-admin-border hover:bg-admin-surface-muted",
              ].join(" ")}
            >
              <input
                type="radio"
                name={name}
                value={role.id}
                checked={selectedRoleId === role.id}
                onChange={() => onSelect(role.id)}
                className="mt-1"
              />
              <span className="min-w-0">
                <span className="block text-sm font-medium text-admin-text">{role.code}</span>
                <span className="block text-xs text-admin-text-secondary">{role.name}</span>
              </span>
            </label>
          ))}
        </div>
      )}

      {fieldError ? <p className="text-sm text-admin-danger">{fieldError}</p> : null}
    </div>
  );
}

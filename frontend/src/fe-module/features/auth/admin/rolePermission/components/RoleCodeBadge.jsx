const ROLE_BADGE_TONES = {
  ADMIN: "border-admin-danger/30 bg-admin-danger-soft text-admin-danger",
  MODERATOR: "border-admin-warning/30 bg-admin-warning-soft text-admin-warning",
  USER: "border-admin-border bg-admin-surface-muted text-admin-text-secondary",
};

export function RoleCodeBadge({ code, className = "" }) {
  const tone = ROLE_BADGE_TONES[code] || "border-admin-border bg-admin-surface-muted text-admin-text";

  return (
    <span
      className={[
        "inline-flex max-w-full items-center rounded-md border px-2 py-0.5 font-mono text-xs font-medium tracking-wide",
        tone,
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {code}
    </span>
  );
}

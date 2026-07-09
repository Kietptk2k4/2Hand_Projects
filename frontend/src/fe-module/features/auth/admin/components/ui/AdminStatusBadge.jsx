const VARIANTS = {
  neutral: "bg-admin-surface-muted text-admin-text-secondary",
  active: "bg-admin-accent-soft text-admin-accent-strong",
  success: "bg-admin-success-soft text-admin-success",
  warning: "bg-admin-warning-soft text-admin-warning",
  danger: "bg-admin-danger-soft text-admin-danger",
};

export function AdminStatusBadge({ children, variant = "neutral", className = "" }) {
  return (
    <span
      className={[
        "inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium",
        VARIANTS[variant] ?? VARIANTS.neutral,
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
    </span>
  );
}

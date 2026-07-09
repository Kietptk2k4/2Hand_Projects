const VARIANTS = {
  error: "border-admin-danger/30 bg-admin-danger-soft text-admin-danger",
  info: "border-admin-accent-border bg-admin-accent-soft text-admin-accent-strong",
};

export function RbacInlineAlert({ variant = "error", message, className = "" }) {
  if (!message) return null;

  return (
    <div
      className={[
        "rounded-lg border px-4 py-3 text-sm",
        VARIANTS[variant] ?? VARIANTS.error,
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      role={variant === "error" ? "alert" : "status"}
    >
      {message}
    </div>
  );
}

const PADDING = {
  none: "",
  sm: "p-4",
  md: "p-4 lg:p-5",
  lg: "p-4 lg:p-6",
};

export function AdminSurfaceCard({
  children,
  className = "",
  padding = "md",
  as: Component = "div",
}) {
  return (
    <Component
      className={[
        "max-w-full min-w-0 rounded-xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]",
        PADDING[padding] ?? PADDING.md,
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
    </Component>
  );
}

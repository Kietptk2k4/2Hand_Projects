export function CartBadgePill({ count, active = false, className = "" }) {
  if (!count || count <= 0) return null;

  const label = count > 99 ? "99+" : String(count);

  return (
    <span
      className={[
        "inline-flex min-h-5 min-w-5 items-center justify-center rounded-full px-1.5 text-label-sm font-semibold tabular-nums",
        active ? "bg-on-primary text-primary" : "bg-primary text-on-primary",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      aria-label={`${count} sản phẩm trong giỏ`}
    >
      {label}
    </span>
  );
}
export function NotificationBadgePill({ count, className = "" }) {
  if (!count || count <= 0) return null;

  const label = count > 99 ? "99+" : String(count);

  return (
    <span
      className={[
        "inline-flex min-h-5 min-w-5 items-center justify-center rounded-full bg-error px-1.5 text-[10px] font-semibold text-white tabular-nums",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      aria-label={`${count} thong bao chua doc`}
    >
      {label}
    </span>
  );
}

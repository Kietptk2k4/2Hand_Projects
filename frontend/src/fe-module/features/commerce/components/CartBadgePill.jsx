import { useEffect, useState } from "react";

export function CartBadgePill({ count, active = false, className = "", pulseToken = 0 }) {
  const [isPulsing, setIsPulsing] = useState(false);

  useEffect(() => {
    if (!pulseToken) return undefined;
    setIsPulsing(true);
    const timer = window.setTimeout(() => setIsPulsing(false), 320);
    return () => window.clearTimeout(timer);
  }, [pulseToken]);

  useEffect(() => {
    if (!count || count <= 0) return undefined;
    setIsPulsing(true);
    const timer = window.setTimeout(() => setIsPulsing(false), 320);
    return () => window.clearTimeout(timer);
  }, [count]);

  if (!count || count <= 0) return null;

  const label = count > 99 ? "99+" : String(count);

  return (
    <span
      className={[
        "inline-flex min-h-5 min-w-5 items-center justify-center rounded-full px-1.5 text-label-sm font-semibold tabular-nums transition-transform",
        active ? "bg-on-primary text-primary" : "bg-primary text-on-primary",
        isPulsing ? "scale-125" : "scale-100",
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

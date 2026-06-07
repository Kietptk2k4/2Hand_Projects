import { formatLikeCount } from "../utils/formatLikeCount";

export function LikeCountButton({
  count,
  onPress,
  size = "default",
  showZero = false,
  className = "",
}) {
  const num = Number(count) || 0;

  const capsuleBase =
    "inline-flex shrink-0 items-center justify-center rounded-full font-semibold leading-none";

  if (num <= 0) {
    if (!showZero) return null;
    const zeroSizeClass =
      size === "compact" ? "h-7 min-w-[2.75rem] px-3 text-xs" : "h-8 min-w-[3rem] px-3.5 text-sm";
    return (
      <span
        className={`${capsuleBase} bg-surface-container-high text-on-surface-variant/45 ${zeroSizeClass} ${className}`}
      >
        0
      </span>
    );
  }

  const sizeClass =
    size === "compact" ? "h-7 min-w-[2.75rem] px-3 text-xs" : "h-8 min-w-[3rem] px-3.5 text-sm";

  return (
    <button
      type="button"
      onClick={(event) => {
        event.stopPropagation();
        onPress?.(num);
      }}
      className={[
        capsuleBase,
        "bg-primary/12 text-primary ring-1 ring-inset ring-primary/25",
        "transition-all hover:bg-primary/18 hover:ring-primary/40 active:scale-[0.98]",
        sizeClass,
        className,
      ].join(" ")}
      aria-label={`Xem ${num} người đã thích`}
    >
      {formatLikeCount(num)}
    </button>
  );
}
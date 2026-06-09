const VARIANT_STYLES = {
  sale: "bg-[#E53935] text-white border-white/90",
  lowStock: "bg-amber-500 text-white border-white/90",
  soldOut: "bg-neutral-800/95 text-white border-white/25",
  condition: "bg-primary text-on-primary border-white/90",
};

export function ProductStickerBadge({
  children,
  variant = "sale",
  className = "",
  rotate = "-rotate-6",
}) {
  const variantClass = VARIANT_STYLES[variant] ?? VARIANT_STYLES.sale;

  return (
    <span
      className={[
        "pointer-events-none inline-flex items-center gap-0.5",
        "rounded-sm border-2 px-2 py-0.5",
        "text-[10px] font-bold uppercase leading-tight tracking-wide",
        "shadow-[2px_3px_0_rgba(0,0,0,0.22)]",
        rotate,
        variantClass,
        className,
      ].join(" ")}
    >
      {children}
    </span>
  );
}

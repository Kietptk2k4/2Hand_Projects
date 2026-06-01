export function StarRating({ rating = 0, size = "md", showValue = false }) {
  const rounded = Math.round(rating * 10) / 10;
  const sizeClass = size === "lg" ? "text-[22px]" : "text-[18px]";

  return (
    <div className="flex items-center gap-1">
      <div className="flex text-primary" aria-hidden="true">
        {[1, 2, 3, 4, 5].map((star) => (
          <span
            key={star}
            className={`material-symbols-outlined ${sizeClass} ${
              star <= Math.round(rating) ? "" : "text-outline-variant"
            }`}
            style={star <= Math.round(rating) ? { fontVariationSettings: "'FILL' 1" } : undefined}
          >
            star
          </span>
        ))}
      </div>
      {showValue ? (
        <span className="text-sm font-semibold text-on-surface">{rounded.toFixed(1)}</span>
      ) : null}
    </div>
  );
}

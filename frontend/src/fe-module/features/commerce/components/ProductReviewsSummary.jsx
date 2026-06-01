import { StarRating } from "./StarRating";

export function ProductReviewsSummary({ ratingSummary }) {
  const avg = ratingSummary?.ratingAvg ?? 0;
  const count = ratingSummary?.ratingCount ?? 0;

  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <p className="text-headline-lg font-bold text-on-surface">{avg > 0 ? avg.toFixed(1) : "—"}</p>
      <StarRating rating={avg} size="lg" />
      <p className="mt-2 text-sm text-on-surface-variant">
        {count} đánh giá
      </p>
    </div>
  );
}

import { StarRating } from "./StarRating";

export function SellerShopReviewsStats({ ratingAvg, ratingCount }) {
  const displayAvg = Number(ratingAvg || 0).toFixed(1);

  return (
    <div className="flex flex-wrap items-center gap-6 rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm">
      <div className="flex flex-col">
        <span className="text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
          Điểm trung bình
        </span>
        <div className="flex items-end gap-2">
          <span className="text-headline-xl font-bold text-primary">{displayAvg}</span>
          <span className="mb-1 text-body-sm text-on-surface-variant">/ 5</span>
        </div>
      </div>

      <div className="hidden h-10 w-px bg-outline-variant sm:block" aria-hidden="true" />

      <div className="flex flex-col">
        <span className="text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
          Tổng đánh giá
        </span>
        <span className="mt-1 text-headline-md font-semibold text-on-surface">{ratingCount}</span>
      </div>

      <div className="hidden sm:block">
        <StarRating rating={ratingAvg} />
      </div>
    </div>
  );
}

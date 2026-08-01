import { StarRating } from "./StarRating";

export function SellerShopReviewsStats({ ratingAvg, ratingCount }) {
  const displayAvg = Number(ratingAvg || 0).toFixed(1);

  return (
    <div className="flex flex-wrap items-center gap-6 rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs transition-all hover:shadow-md">
      <div className="flex items-center gap-4">
        <div className="flex h-14 w-14 flex-col items-center justify-center rounded-2xl bg-amber-50 text-amber-600 border border-amber-200">
          <span className="text-xl font-black">{displayAvg}</span>
          <span className="text-[10px] font-bold text-amber-700">/ 5.0</span>
        </div>
        <div>
          <p className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">
            Đánh giá cửa hàng
          </p>
          <div className="mt-1 flex items-center gap-2">
            <StarRating rating={ratingAvg} />
            <span className="text-xs font-bold text-on-surface">({ratingCount} đánh giá)</span>
          </div>
        </div>
      </div>
    </div>
  );
}

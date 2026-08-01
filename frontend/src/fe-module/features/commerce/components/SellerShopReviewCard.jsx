import { useState } from "react";
import { REVIEW_STATUS_LABELS } from "../constants/sellerShopReviewsConstants";
import { formatShortOrderItemId } from "../utils/formatShortOrderItemId";
import { formatReviewDate } from "../utils/formatReviewDate";
import { ProductReviewMediaStrip } from "./ProductReviewMediaStrip";
import { ProductReviewSellerReply } from "./ProductReviewSellerReply";
import { StarRating } from "./StarRating";

const FALLBACK_PRODUCT_IMAGES = [
  "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500&auto=format&fit=crop&q=80",
];

function getRandomProductFallback(name) {
  if (!name) return FALLBACK_PRODUCT_IMAGES[0];
  let hash = 0;
  for (let i = 0; i < name.length; i += 1) {
    hash = (hash << 5) - hash + name.charCodeAt(i);
    hash |= 0;
  }
  return FALLBACK_PRODUCT_IMAGES[Math.abs(hash) % FALLBACK_PRODUCT_IMAGES.length];
}

export function SellerShopReviewCard({ review, onReply, disabled }) {
  const [imgError, setImgError] = useState(false);

  if (!review) return null;

  const isHidden = review.status === "HIDDEN";
  const hasReply = Boolean(review.sellerReply);
  const canReply = review.status === "VISIBLE" && !hasReply;

  const statusDotClass = isHidden ? "bg-slate-400" : "bg-emerald-500";
  const displayImageSrc = !imgError && review.productThumbnail
    ? review.productThumbnail
    : getRandomProductFallback(review.productNameSnapshot);

  return (
    <article className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs transition-all hover:shadow-md">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        {/* Product Info Left Column */}
        <div className="flex w-full min-w-[200px] gap-3.5 lg:w-1/4">
          <div className="h-16 w-16 shrink-0 overflow-hidden rounded-xl border border-outline-variant/60 bg-surface-container-low">
            <img
              src={displayImageSrc}
              alt={review.productNameSnapshot || "Sản phẩm"}
              onError={() => setImgError(true)}
              className="h-full w-full object-cover transition-transform duration-300 hover:scale-105"
              loading="lazy"
            />
          </div>
          <div className="min-w-0">
            <h3 className="line-clamp-2 text-xs font-bold text-on-surface sm:text-sm">
              {review.productNameSnapshot}
            </h3>
            <span className="mt-1 inline-block rounded-md bg-surface-container-high px-2 py-0.5 font-mono text-[11px] text-slate-500">
              {formatShortOrderItemId(review.orderItemId)}
            </span>
          </div>
        </div>

        {/* Review Content Center Column */}
        <div className="flex w-full flex-col gap-2.5 lg:w-2/4">
          <div className="flex items-center justify-between gap-2">
            <StarRating rating={review.rating} />
            <time className="text-xs text-on-surface-variant font-semibold" dateTime={review.createdAt}>
              {formatReviewDate(review.createdAt)}
            </time>
          </div>

          {review.comment ? (
            <p className="text-xs font-semibold leading-relaxed text-on-surface sm:text-sm">
              {review.comment}
            </p>
          ) : (
            <p className="text-xs italic text-on-surface-variant">Không có bình luận.</p>
          )}

          <ProductReviewMediaStrip media={review.media} />
          <ProductReviewSellerReply sellerReply={review.sellerReply} />
        </div>

        {/* Actions Right Column */}
        <div className="flex w-full flex-col items-start justify-between gap-4 border-t border-outline-variant/60 pt-4 lg:w-1/4 lg:items-end lg:border-t-0 lg:pt-0">
          <div className="flex items-center gap-1.5 rounded-lg bg-surface-container-high px-2.5 py-1">
            <span className={`h-2 w-2 rounded-full ${statusDotClass}`} aria-hidden="true" />
            <span className="text-[11px] font-bold uppercase text-on-surface-variant">
              {REVIEW_STATUS_LABELS[review.status] || review.status}
            </span>
          </div>

          {canReply ? (
            <button
              type="button"
              disabled={disabled}
              onClick={() => onReply?.(review)}
              className="inline-flex w-full items-center justify-center gap-1.5 rounded-xl bg-primary px-5 py-2.5 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 disabled:opacity-50 lg:w-auto cursor-pointer"
            >
              <span className="material-symbols-outlined text-base" aria-hidden="true">
                reply
              </span>
              Phản hồi
            </button>
          ) : hasReply ? (
            <span className="inline-block rounded-xl bg-blue-50 px-3 py-1.5 text-xs font-bold text-blue-700 border border-blue-200">
              Đã phản hồi
            </span>
          ) : (
            <span className="text-xs text-slate-400 font-semibold">
              {isHidden ? "Không thể phản hồi (đã ẩn)" : "—"}
            </span>
          )}
        </div>
      </div>
    </article>
  );
}

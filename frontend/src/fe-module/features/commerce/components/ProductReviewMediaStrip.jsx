import { useCallback, useState } from "react";
import { ProductMediaLightbox } from "./ProductMediaLightbox";

const FALLBACK_REVIEW_IMAGES = [
  "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=80",
];

function getReviewFallback(index) {
  return FALLBACK_REVIEW_IMAGES[index % FALLBACK_REVIEW_IMAGES.length];
}

function ReviewMediaItem({ item, index, openAt }) {
  const [imgError, setImgError] = useState(false);
  const displaySrc = !imgError && item.url ? item.url : getReviewFallback(index);

  return (
    <button
      type="button"
      onClick={() => openAt(index)}
      className="relative h-20 w-20 shrink-0 cursor-pointer overflow-hidden rounded-xl border border-outline-variant/80 bg-surface-container transition-transform hover:scale-105 shadow-xs"
      aria-label={
        item.mediaType === "VIDEO" ? "Xem video đánh giá" : "Xem ảnh đánh giá"
      }
    >
      {item.mediaType === "VIDEO" ? (
        <>
          <video
            src={item.url}
            className="h-full w-full object-cover"
            muted
            playsInline
            preload="metadata"
            aria-hidden="true"
          />
          <span
            className="pointer-events-none absolute inset-0 flex items-center justify-center bg-black/30 text-white"
            aria-hidden="true"
          >
            <span className="material-symbols-outlined">play_circle</span>
          </span>
        </>
      ) : (
        <img
          src={displaySrc}
          alt="Ảnh đánh giá"
          onError={() => setImgError(true)}
          className="h-full w-full object-cover"
          loading="lazy"
        />
      )}
    </button>
  );
}

export function ProductReviewMediaStrip({ media = [] }) {
  const [lightboxIndex, setLightboxIndex] = useState(null);
  const items = media.filter((item) => item?.url || item?.mediaId);

  const openAt = useCallback((index) => {
    setLightboxIndex(index);
  }, []);

  const closeLightbox = useCallback(() => {
    setLightboxIndex(null);
  }, []);

  if (!items.length) return null;

  return (
    <>
      <div className="mt-3 flex gap-2 overflow-x-auto pb-1">
        {items.map((item, index) => (
          <ReviewMediaItem
            key={item.mediaId || index}
            item={item}
            index={index}
            openAt={openAt}
          />
        ))}
      </div>

      {lightboxIndex !== null ? (
        <ProductMediaLightbox
          items={items}
          initialIndex={lightboxIndex}
          variant="review"
          onClose={closeLightbox}
        />
      ) : null}
    </>
  );
}

import { useCallback, useState } from "react";
import { ProductMediaLightbox } from "./ProductMediaLightbox";

export function ProductReviewMediaStrip({ media = [] }) {
  const [lightboxIndex, setLightboxIndex] = useState(null);
  const items = media.filter((item) => item?.url);

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
          <button
            key={item.mediaId}
            type="button"
            onClick={() => openAt(index)}
            className="relative h-20 w-20 shrink-0 cursor-pointer overflow-hidden rounded-lg border border-outline-variant bg-surface-container transition-opacity hover:opacity-90"
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
              <img src={item.url} alt="" className="h-full w-full object-cover" />
            )}
          </button>
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

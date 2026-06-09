import { useMemo, useState } from "react";
import { getConditionLabel } from "../constants/productDetailConstants";
import { isAddToCartDisabled, isProductOnSale } from "../utils/productDetailDisplay";
import { ProductImageStickers } from "./ProductImageStickers";

const PREVIEW_VIDEO = "video";

function ImageOnlyGallery({ product, images, stickers }) {
  const [activeIndex, setActiveIndex] = useState(0);
  const activeItem = images[activeIndex] || images[0];

  return (
    <>
      <div className="relative aspect-[16/10] w-full overflow-hidden rounded-xl border border-outline-variant bg-surface-container-low shadow-sm md:aspect-[21/9]">
        {activeItem?.mediaUrl ? (
          <img
            src={activeItem.mediaUrl}
            alt={product.title}
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="flex h-full items-center justify-center">
            <span className="material-symbols-outlined text-5xl text-outline" aria-hidden="true">
              image
            </span>
          </div>
        )}
        {stickers}
      </div>

      {images.length > 1 ? (
        <div className="mt-4 flex gap-3 overflow-x-auto pb-2">
          {images.map((item, index) => (
            <button
              key={item.mediaId || index}
              type="button"
              onClick={() => setActiveIndex(index)}
              className={[
                "h-20 w-20 shrink-0 overflow-hidden rounded-lg border-2 transition-colors md:h-24 md:w-24",
                index === activeIndex
                  ? "border-primary"
                  : "border-outline-variant opacity-70 hover:opacity-100",
              ].join(" ")}
              aria-label={`Anh ${index + 1}`}
              aria-current={index === activeIndex ? "true" : undefined}
            >
              <img src={item.mediaUrl} alt="" className="h-full w-full object-cover" />
            </button>
          ))}
        </div>
      ) : null}
    </>
  );
}

function VideoWithImageSidebarGallery({ product, video, images, stickers }) {
  const [previewMode, setPreviewMode] = useState(PREVIEW_VIDEO);
  const showingVideo = previewMode === PREVIEW_VIDEO;
  const activeImage = typeof previewMode === "number" ? images[previewMode] : null;

  const thumbButtonClass = (isActive) =>
    [
      "relative shrink-0 overflow-hidden rounded-lg border-2 transition-colors",
      "h-16 w-16 md:h-20 md:w-20 lg:h-[4.5rem] lg:w-full",
      isActive ? "border-primary ring-2 ring-primary/30" : "border-outline-variant opacity-80 hover:opacity-100",
    ].join(" ");

  return (
    <div className="flex flex-col gap-4 lg:flex-row lg:items-stretch">
      <div className="relative min-h-[220px] flex-1 overflow-hidden rounded-xl border border-outline-variant bg-surface-container-low shadow-sm md:min-h-[280px] lg:min-h-[360px]">
        {showingVideo && video?.mediaUrl ? (
          <video
            key={video.mediaUrl}
            src={video.mediaUrl}
            className="h-full min-h-[220px] w-full object-cover md:min-h-[280px] lg:min-h-[360px]"
            controls
            playsInline
            preload="metadata"
          />
        ) : activeImage?.mediaUrl ? (
          <img
            src={activeImage.mediaUrl}
            alt={product.title}
            className="h-full min-h-[220px] w-full object-cover md:min-h-[280px] lg:min-h-[360px]"
          />
        ) : (
          <div className="flex h-full min-h-[220px] items-center justify-center md:min-h-[280px] lg:min-h-[360px]">
            <span className="material-symbols-outlined text-5xl text-outline" aria-hidden="true">
              image
            </span>
          </div>
        )}
        {stickers}
        {!showingVideo ? (
          <button
            type="button"
            onClick={() => setPreviewMode(PREVIEW_VIDEO)}
            className="absolute bottom-3 right-3 z-10 flex items-center gap-1 rounded-full bg-surface/95 px-3 py-1.5 text-label-sm font-medium text-on-surface shadow-md backdrop-blur-sm transition-colors hover:bg-surface"
          >
            <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
              videocam
            </span>
            Xem video
          </button>
        ) : null}
      </div>

      <div className="flex gap-2 overflow-x-auto pb-1 lg:w-24 lg:flex-col lg:overflow-y-auto lg:overflow-x-hidden lg:pb-0">
        <button
          type="button"
          onClick={() => setPreviewMode(PREVIEW_VIDEO)}
          className={thumbButtonClass(showingVideo)}
          aria-label="Xem video san pham"
          aria-current={showingVideo ? "true" : undefined}
        >
          <div className="flex h-full w-full flex-col items-center justify-center bg-neutral-900 text-on-primary">
            <span className="material-symbols-outlined text-[22px]" aria-hidden="true">
              play_circle
            </span>
            <span className="mt-0.5 text-[9px] font-semibold uppercase tracking-wide">Video</span>
          </div>
        </button>

        {images.map((item, index) => {
          const isActive = previewMode === index;
          return (
            <button
              key={item.mediaId || index}
              type="button"
              onClick={() => setPreviewMode(index)}
              className={thumbButtonClass(isActive)}
              aria-label={`Xem anh ${index + 1}`}
              aria-current={isActive ? "true" : undefined}
            >
              <img src={item.mediaUrl} alt="" className="h-full w-full object-cover" />
            </button>
          );
        })}
      </div>
    </div>
  );
}

export function ProductMediaGallery({ product }) {
  const { video, images } = useMemo(() => {
    const items = product?.media || [];
    return {
      video: items.find((item) => item.mediaType === "VIDEO" && item.mediaUrl) || null,
      images: items.filter((item) => item.mediaType === "IMAGE" && item.mediaUrl),
    };
  }, [product]);

  const isOnSale = isProductOnSale(product);
  const isOutOfStock = isAddToCartDisabled(product);
  const conditionLabel = getConditionLabel(product?.condition);
  const lowStock = product?.inventorySummary?.lowStock ?? false;

  if (!product) return null;

  const stickers = (
    <ProductImageStickers
      isOnSale={isOnSale}
      isOutOfStock={isOutOfStock}
      lowStock={lowStock}
      conditionLabel={conditionLabel || null}
    />
  );

  return (
    <section className="mb-8">
      {video ? (
        <VideoWithImageSidebarGallery
          key={product.productId}
          product={product}
          video={video}
          images={images}
          stickers={stickers}
        />
      ) : (
        <ImageOnlyGallery
          key={product.productId}
          product={product}
          images={images}
          stickers={stickers}
        />
      )}
    </section>
  );
}

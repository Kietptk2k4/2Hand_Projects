import { useCallback, useMemo, useState } from "react";
import { getConditionLabel } from "../constants/productDetailConstants";
import { isAddToCartDisabled, isProductOnSale } from "../utils/productDetailDisplay";
import { ProductImageStickers } from "./ProductImageStickers";
import { ProductMediaLightbox } from "./ProductMediaLightbox";

const PREVIEW_VIDEO = "video";
const MAIN_VIEWER_CLASS =
  "group relative aspect-square w-full max-w-[min(100%,450px)] cursor-zoom-in overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm";

function thumbButtonClass(isActive) {
  return [
    "relative shrink-0 overflow-hidden rounded-lg border-2 bg-surface-container-lowest transition-colors",
    "h-16 w-16 lg:h-[4.5rem] lg:w-full",
    isActive
      ? "border-primary ring-2 ring-primary/30"
      : "border-outline-variant opacity-80 hover:opacity-100",
  ].join(" ");
}

function buildLightboxItems(video, images) {
  const items = [];
  if (video?.mediaUrl) {
    items.push({ ...video, mediaType: "VIDEO" });
  }
  images.forEach((image) => {
    if (image?.mediaUrl) {
      items.push({ ...image, mediaType: "IMAGE" });
    }
  });
  return items;
}

function previewModeToIndex(previewMode, hasVideo) {
  if (previewMode === PREVIEW_VIDEO) return 0;
  return hasVideo ? previewMode + 1 : previewMode;
}

function indexToPreviewMode(index, hasVideo) {
  if (hasVideo && index === 0) return PREVIEW_VIDEO;
  return hasVideo ? index - 1 : index;
}

function MainViewer({
  product,
  showingVideo,
  video,
  activeImage,
  stickers,
  onOpenLightbox,
  canOpenLightbox,
}) {
  const handleOpen = () => {
    if (canOpenLightbox) onOpenLightbox?.();
  };

  return (
    <div
      className={MAIN_VIEWER_CLASS}
      onClick={handleOpen}
      onKeyDown={(event) => {
        if ((event.key === "Enter" || event.key === " ") && canOpenLightbox) {
          event.preventDefault();
          handleOpen();
        }
      }}
      role={canOpenLightbox ? "button" : undefined}
      tabIndex={canOpenLightbox ? 0 : undefined}
      aria-label={canOpenLightbox ? "Phóng to ảnh hoặc video" : undefined}
    >
      {showingVideo && video?.mediaUrl ? (
        <video
          key={video.mediaUrl}
          src={video.mediaUrl}
          className="h-full w-full object-contain"
          controls
          playsInline
          preload="metadata"
          onClick={(event) => event.stopPropagation()}
        />
      ) : activeImage?.mediaUrl ? (
        <img
          src={activeImage.mediaUrl}
          alt={product.title}
          className="h-full w-full object-contain"
        />
      ) : (
        <div className="flex h-full items-center justify-center">
          <span className="material-symbols-outlined text-5xl text-outline" aria-hidden="true">
            image
          </span>
        </div>
      )}
      {stickers}
      {canOpenLightbox && !showingVideo ? (
        <div
          className="pointer-events-none absolute bottom-3 right-3 flex h-9 w-9 items-center justify-center rounded-full bg-surface-container-lowest/90 text-on-surface shadow-md opacity-0 transition-opacity group-hover:opacity-100 group-focus-within:opacity-100"
          aria-hidden="true"
        >
          <span className="material-symbols-outlined text-[20px]">zoom_in</span>
        </div>
      ) : null}
      {canOpenLightbox && showingVideo ? (
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation();
            handleOpen();
          }}
          className="absolute bottom-3 right-3 z-10 flex items-center gap-1 rounded-full bg-surface/95 px-3 py-1.5 text-label-sm font-medium text-on-surface shadow-md backdrop-blur-sm transition-colors hover:bg-surface"
        >
          <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
            fullscreen
          </span>
          Phóng to
        </button>
      ) : null}
    </div>
  );
}

function ThumbnailStrip({
  video,
  images,
  previewMode,
  onSelectVideo,
  onSelectImage,
  className = "",
}) {
  const hasVideo = Boolean(video?.mediaUrl);
  if (!hasVideo && images.length <= 1) return null;

  return (
    <div className={className}>
      {hasVideo ? (
        <button
          type="button"
          onClick={onSelectVideo}
          className={thumbButtonClass(previewMode === PREVIEW_VIDEO)}
          aria-label="Xem video sản phẩm"
          aria-current={previewMode === PREVIEW_VIDEO ? "true" : undefined}
        >
          <div className="flex h-full w-full flex-col items-center justify-center bg-neutral-900 text-on-primary">
            <span className="material-symbols-outlined text-[22px]" aria-hidden="true">
              play_circle
            </span>
            <span className="mt-0.5 text-[9px] font-semibold uppercase tracking-wide">Video</span>
          </div>
        </button>
      ) : null}

      {images.map((item, index) => {
        const isActive = previewMode === index;
        return (
          <button
            key={item.mediaId || index}
            type="button"
            onClick={() => onSelectImage(index)}
            className={thumbButtonClass(isActive)}
            aria-label={`Ảnh ${index + 1}`}
            aria-current={isActive ? "true" : undefined}
          >
            <img src={item.mediaUrl} alt="" className="h-full w-full object-cover" />
          </button>
        );
      })}
    </div>
  );
}

function ShopeeStyleGallery({ product, video, images, stickers }) {
  const hasVideo = Boolean(video?.mediaUrl);
  const defaultMode = hasVideo ? PREVIEW_VIDEO : 0;
  const [previewMode, setPreviewMode] = useState(defaultMode);
  const [lightboxIndex, setLightboxIndex] = useState(null);

  const lightboxItems = useMemo(() => buildLightboxItems(video, images), [video, images]);
  const canOpenLightbox = lightboxItems.length > 0;

  const showingVideo = previewMode === PREVIEW_VIDEO;
  const activeImage = typeof previewMode === "number" ? images[previewMode] : null;
  const showThumbs = hasVideo || images.length > 1;

  const openLightbox = useCallback(() => {
    if (!canOpenLightbox) return;
    setLightboxIndex(previewModeToIndex(previewMode, hasVideo));
  }, [canOpenLightbox, hasVideo, previewMode]);

  const closeLightbox = useCallback(() => {
    setLightboxIndex(null);
  }, []);

  const handleLightboxIndexChange = useCallback(
    (index) => {
      setPreviewMode(indexToPreviewMode(index, hasVideo));
    },
    [hasVideo]
  );

  return (
    <>
      <div className="w-full">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-start">
          {showThumbs ? (
            <ThumbnailStrip
              video={video}
              images={images}
              previewMode={previewMode}
              onSelectVideo={() => setPreviewMode(PREVIEW_VIDEO)}
              onSelectImage={setPreviewMode}
              className="hidden max-h-[min(100vw,450px)] shrink-0 flex-col gap-2 overflow-y-auto lg:flex lg:w-[4.5rem]"
            />
          ) : null}

          <MainViewer
            product={product}
            showingVideo={showingVideo}
            video={video}
            activeImage={activeImage ?? images[0]}
            stickers={stickers}
            canOpenLightbox={canOpenLightbox}
            onOpenLightbox={openLightbox}
          />
        </div>

        {showThumbs ? (
          <ThumbnailStrip
            video={video}
            images={images}
            previewMode={previewMode}
            onSelectVideo={() => setPreviewMode(PREVIEW_VIDEO)}
            onSelectImage={setPreviewMode}
            className="mt-3 flex gap-2 overflow-x-auto pb-1 lg:hidden"
          />
        ) : null}
      </div>

      {lightboxIndex !== null ? (
        <ProductMediaLightbox
          items={lightboxItems}
          initialIndex={lightboxIndex}
          title={product.title}
          onClose={closeLightbox}
          onIndexChange={handleLightboxIndexChange}
        />
      ) : null}
    </>
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
    <ShopeeStyleGallery
      key={product.productId}
      product={product}
      video={video}
      images={images}
      stickers={stickers}
    />
  );
}

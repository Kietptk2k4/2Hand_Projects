import { useCallback, useEffect, useState } from "react";

function isVideoItem(item) {
  return item?.mediaType === "VIDEO";
}

export function ProductMediaLightbox({
  items = [],
  initialIndex = 0,
  title = "",
  onClose,
  onIndexChange,
}) {
  const filteredItems = (items || []).filter((item) => item?.mediaUrl);
  const [activeIndex, setActiveIndex] = useState(() =>
    Math.min(Math.max(initialIndex, 0), Math.max(filteredItems.length - 1, 0))
  );

  const current = filteredItems[activeIndex];
  const isVideo = current ? isVideoItem(current) : false;
  const hasMultiple = filteredItems.length > 1;

  const goTo = useCallback(
    (nextIndex) => {
      if (!filteredItems.length) return;
      const clamped = (nextIndex + filteredItems.length) % filteredItems.length;
      setActiveIndex(clamped);
      onIndexChange?.(clamped);
    },
    [filteredItems.length, onIndexChange]
  );

  const handleClose = useCallback(() => {
    onClose?.();
  }, [onClose]);

  useEffect(() => {
    setActiveIndex(
      Math.min(Math.max(initialIndex, 0), Math.max(filteredItems.length - 1, 0))
    );
  }, [initialIndex, filteredItems.length]);

  useEffect(() => {
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previousOverflow;
    };
  }, []);

  useEffect(() => {
    const onKeyDown = (event) => {
      if (event.key === "Escape") {
        handleClose();
        return;
      }
      if (!hasMultiple) return;
      if (event.key === "ArrowLeft") {
        event.preventDefault();
        goTo(activeIndex - 1);
      }
      if (event.key === "ArrowRight") {
        event.preventDefault();
        goTo(activeIndex + 1);
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [activeIndex, goTo, handleClose, hasMultiple]);

  if (!current) return null;

  return (
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center bg-on-background/85 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label={isVideo ? "Xem video sản phẩm" : "Xem ảnh sản phẩm"}
      onClick={handleClose}
    >
      <button
        type="button"
        onClick={handleClose}
        className="absolute right-4 top-4 z-10 rounded-full bg-surface-container-lowest/90 p-2 text-on-surface shadow-md transition-colors hover:bg-surface-variant"
        aria-label="Đóng"
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          close
        </span>
      </button>

      {hasMultiple ? (
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation();
            goTo(activeIndex - 1);
          }}
          className="absolute left-3 top-1/2 z-10 hidden -translate-y-1/2 rounded-full bg-surface-container-lowest/90 p-2 text-on-surface shadow-md transition-colors hover:bg-surface-variant sm:flex"
          aria-label="Ảnh trước"
        >
          <span className="material-symbols-outlined" aria-hidden="true">
            chevron_left
          </span>
        </button>
      ) : null}

      {hasMultiple ? (
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation();
            goTo(activeIndex + 1);
          }}
          className="absolute right-3 top-1/2 z-10 hidden -translate-y-1/2 rounded-full bg-surface-container-lowest/90 p-2 text-on-surface shadow-md transition-colors hover:bg-surface-variant sm:flex"
          aria-label="Ảnh sau"
        >
          <span className="material-symbols-outlined" aria-hidden="true">
            chevron_right
          </span>
        </button>
      ) : null}

      <div
        className="flex max-h-[90vh] max-w-[min(100vw-2rem,960px)] flex-col items-center"
        onClick={(event) => event.stopPropagation()}
      >
        {isVideo ? (
          <video
            key={current.mediaUrl}
            src={current.mediaUrl}
            className="max-h-[85vh] max-w-full rounded-lg bg-on-background"
            controls
            autoPlay
            playsInline
          />
        ) : (
          <img
            src={current.mediaUrl}
            alt={title || "Ảnh sản phẩm"}
            className="max-h-[85vh] max-w-full rounded-lg object-contain"
          />
        )}

        {hasMultiple ? (
          <p className="mt-4 text-sm font-medium text-on-primary">
            {activeIndex + 1} / {filteredItems.length}
          </p>
        ) : null}
      </div>
    </div>
  );
}

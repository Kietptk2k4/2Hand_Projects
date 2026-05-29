import { useEffect } from "react";

export function MediaGalleryLightbox({ media = [], initialIndex = 0, onClose }) {
  const items = (media || []).filter((item) => item?.url);
  const safeIndex = Math.min(Math.max(initialIndex, 0), Math.max(items.length - 1, 0));
  const current = items[safeIndex];

  useEffect(() => {
    const onKeyDown = (event) => {
      if (event.key === "Escape") onClose?.();
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  if (!current) return null;

  return (
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center bg-on-background/80 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label="Xem ảnh"
      onClick={onClose}
    >
      <button
        type="button"
        onClick={onClose}
        className="absolute right-4 top-4 z-10 rounded-full bg-surface-container-lowest/90 p-2 text-on-surface hover:bg-surface-variant"
        aria-label="Đóng gallery"
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          close
        </span>
      </button>
      <img
        src={current.url}
        alt=""
        className="max-h-[90vh] max-w-full rounded-lg object-contain"
        onClick={(event) => event.stopPropagation()}
      />
      {items.length > 1 ? (
        <p className="absolute bottom-6 text-sm text-on-primary">
          {safeIndex + 1} / {items.length}
        </p>
      ) : null}
    </div>
  );
}

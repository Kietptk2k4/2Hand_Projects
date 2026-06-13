import { useEffect } from "react";

export function ProfileImageLightbox({ imageUrl, label = "Xem ảnh", onClose }) {
  useEffect(() => {
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    const onKeyDown = (event) => {
      if (event.key === "Escape") {
        onClose?.();
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", onKeyDown);
    };
  }, [onClose]);

  if (!imageUrl) return null;

  return (
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center bg-on-background/85 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label={label}
      onClick={onClose}
    >
      <button
        type="button"
        onClick={onClose}
        className="absolute right-4 top-4 z-10 rounded-full bg-surface-container-lowest/90 p-2 text-on-surface shadow-md transition-colors hover:bg-surface-variant"
        aria-label="Đóng"
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          close
        </span>
      </button>

      <img
        src={imageUrl}
        alt=""
        className="max-h-[90vh] max-w-full rounded-lg object-contain shadow-lg"
        onClick={(event) => event.stopPropagation()}
      />
    </div>
  );
}

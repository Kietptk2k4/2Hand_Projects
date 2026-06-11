import { useEffect } from "react";
import { createPortal } from "react-dom";

export function ProductTagUnavailableModal({ isOpen, onClose }) {
  useEffect(() => {
    if (!isOpen) return undefined;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) return undefined;
    const onKeyDown = (event) => {
      if (event.key !== "Escape") return;
      event.stopPropagation();
      onClose?.();
    };
    window.addEventListener("keydown", onKeyDown, true);
    return () => window.removeEventListener("keydown", onKeyDown, true);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return createPortal(
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center bg-on-background/50 p-4 backdrop-blur-sm"
      role="presentation"
      onClick={onClose}
    >
      <div
        className="relative w-full max-w-sm rounded-xl bg-surface-container-lowest p-6 shadow-lg"
        role="dialog"
        aria-modal="true"
        aria-labelledby="product-tag-unavailable-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="mb-4 flex items-center gap-3">
          <span
            className="material-symbols-outlined text-3xl text-outline"
            aria-hidden="true"
          >
            inventory_2
          </span>
          <h2
            id="product-tag-unavailable-title"
            className="text-lg font-semibold text-on-surface"
          >
            Sản phẩm không còn khả dụng
          </h2>
        </div>
        <p className="text-sm text-on-surface-variant">
          Sản phẩm này hiện không thể xem trên 2Hands.
        </p>
        <button
          type="button"
          onClick={onClose}
          className="mt-6 w-full rounded-lg bg-primary px-4 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
        >
          Đóng
        </button>
      </div>
    </div>,
    document.body
  );
}

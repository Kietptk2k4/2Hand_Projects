import { useEffect, useRef, useState } from "react";

export function PostOwnerMenu({ onEdit, icon = "more_horiz", align = "right", className = "" }) {
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;

    const onPointerDown = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener("pointerdown", onPointerDown);
    return () => document.removeEventListener("pointerdown", onPointerDown);
  }, [open]);

  const stopPropagation = (event) => {
    event.stopPropagation();
  };

  const menuAlignClass = align === "left" ? "left-0" : "right-0";

  return (
    <div ref={rootRef} className={`relative ${className}`}>
      <button
        type="button"
        className="p-1 text-on-surface-variant hover:text-on-surface"
        aria-label="Tùy chọn bài viết"
        aria-expanded={open}
        onClick={(event) => {
          stopPropagation(event);
          setOpen((prev) => !prev);
        }}
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          {icon}
        </span>
      </button>

      {open ? (
        <div
          className={`absolute top-full z-30 mt-1 min-w-[200px] rounded-lg border border-outline-variant bg-surface-container-lowest py-1 shadow-lg ${menuAlignClass}`}
          role="menu"
        >
          <button
            type="button"
            role="menuitem"
            className="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-on-surface hover:bg-surface-container-low"
            onClick={(event) => {
              stopPropagation(event);
              setOpen(false);
              onEdit?.();
            }}
          >
            <span className="material-symbols-outlined text-[18px] text-on-surface-variant" aria-hidden="true">
              edit
            </span>
            Chỉnh sửa bài viết
          </button>
        </div>
      ) : null}
    </div>
  );
}

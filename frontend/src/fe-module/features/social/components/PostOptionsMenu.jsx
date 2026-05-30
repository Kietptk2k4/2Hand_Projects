import { useEffect, useRef, useState } from "react";

const DELETE_CONFIRM_MESSAGE =
  "Bạn có chắc muốn xóa bài viết này? Hành động không thể hoàn tác.";

export function PostOptionsMenu({
  postId,
  isOwner = false,
  savedByMe = false,
  onEdit,
  onDelete,
  onToggleSave,
  isSaving = false,
  isDeleting = false,
  icon = "more_horiz",
  align = "right",
  className = "",
}) {
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
  const busy = isSaving || isDeleting;
  const saveLabel = savedByMe ? "Bỏ lưu" : "Lưu bài";
  const saveIcon = savedByMe ? "bookmark_remove" : "bookmark";

  return (
    <div ref={rootRef} className={`relative ${className}`} data-post-id={postId}>
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
          onClick={stopPropagation}
        >
          {isOwner ? (
            <button
              type="button"
              role="menuitem"
              disabled={busy}
              className="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-on-surface hover:bg-surface-container-low disabled:cursor-not-allowed disabled:opacity-50"
              onClick={(event) => {
                stopPropagation(event);
                setOpen(false);
                onEdit?.();
              }}
            >
              <span
                className="material-symbols-outlined text-[18px] text-on-surface-variant"
                aria-hidden="true"
              >
                edit
              </span>
              Chỉnh sửa bài viết
            </button>
          ) : null}

          {isOwner ? (
            <button
              type="button"
              role="menuitem"
              disabled={busy}
              className="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-error hover:bg-error-container/30 disabled:cursor-not-allowed disabled:opacity-50"
              onClick={(event) => {
                stopPropagation(event);
                setOpen(false);
                if (window.confirm(DELETE_CONFIRM_MESSAGE)) {
                  onDelete?.();
                }
              }}
            >
              <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                delete
              </span>
              {isDeleting ? "Đang xóa…" : "Xóa bài viết"}
            </button>
          ) : null}

          <button
            type="button"
            role="menuitem"
            disabled={busy}
            className="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-on-surface hover:bg-surface-container-low disabled:cursor-not-allowed disabled:opacity-50"
            onClick={(event) => {
              stopPropagation(event);
              setOpen(false);
              onToggleSave?.();
            }}
          >
            <span
              className="material-symbols-outlined text-[18px] text-on-surface-variant"
              aria-hidden="true"
            >
              {saveIcon}
            </span>
            {isSaving ? "Đang xử lý…" : saveLabel}
          </button>
        </div>
      ) : null}
    </div>
  );
}

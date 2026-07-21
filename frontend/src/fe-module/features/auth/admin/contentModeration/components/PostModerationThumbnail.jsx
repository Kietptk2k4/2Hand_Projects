const placeholderClass =
  "flex h-12 w-12 shrink-0 items-center justify-center rounded-lg border border-admin-border bg-admin-surface-muted text-admin-text-muted";

export function PostModerationThumbnail({ url, alt = "Ảnh bài viết", className = "" }) {
  if (!url) {
    return (
      <div className={[placeholderClass, className].filter(Boolean).join(" ")} aria-hidden="true">
        <span className="material-symbols-outlined text-[20px]">image</span>
      </div>
    );
  }

  return (
    <img
      src={url}
      alt={alt}
      loading="lazy"
      className={[
        "h-12 w-12 shrink-0 rounded-lg border border-admin-border object-cover bg-admin-surface-muted",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    />
  );
}

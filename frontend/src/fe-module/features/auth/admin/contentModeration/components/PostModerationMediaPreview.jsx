import { PostModerationThumbnail } from "./PostModerationThumbnail.jsx";

export function PostModerationMediaPreview({ media = [], thumbnailUrl = "" }) {
  const items = media?.length ? media : thumbnailUrl ? [{ url: thumbnailUrl, type: "IMAGE" }] : [];

  if (!items.length) {
    return (
      <div className="rounded-lg border border-dashed border-admin-border px-4 py-8 text-center text-sm text-admin-text-secondary">
        Bài viết không có media.
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      {items.map((item, index) => {
        const isVideo = String(item.type || "").toUpperCase() === "VIDEO";
        return (
          <div
            key={`${item.url}-${index}`}
            className="relative overflow-hidden rounded-lg border border-admin-border bg-admin-surface-muted"
          >
            {isVideo ? (
              <div className="flex aspect-square items-center justify-center">
                <span className="material-symbols-outlined text-[28px] text-admin-text-muted" aria-hidden="true">
                  play_circle
                </span>
              </div>
            ) : (
              <PostModerationThumbnail
                url={item.url}
                className="!h-auto !w-full aspect-square rounded-none border-0"
              />
            )}
            <span className="absolute bottom-1 right-1 rounded bg-admin-text/70 px-1.5 py-0.5 text-[10px] font-medium uppercase tracking-wide text-white">
              {isVideo ? "Video" : "Ảnh"}
            </span>
          </div>
        );
      })}
    </div>
  );
}

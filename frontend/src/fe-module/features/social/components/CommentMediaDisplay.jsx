import { PostMediaThumbnail } from "./PostMediaItem";

export function CommentMediaDisplay({ media = [], className = "", onMediaClick }) {
  const items = (media || []).filter((item) => item?.url);
  if (items.length === 0) return null;

  const interactive = Boolean(onMediaClick);

  return (
    <div className={`mt-2 flex flex-wrap gap-2 ${className}`}>
      {items.map((item, index) => (
        <button
          key={item.url || index}
          type="button"
          className={[
            "h-20 w-20 shrink-0 overflow-hidden rounded-md border border-outline-variant/60 bg-surface-container-high",
            interactive ? "cursor-pointer transition-opacity hover:opacity-90" : "",
          ]
            .filter(Boolean)
            .join(" ")}
          onClick={(event) => {
            event.stopPropagation();
            onMediaClick?.(index);
          }}
          aria-label={interactive ? `Xem media ${index + 1}` : undefined}
        >
          <PostMediaThumbnail item={item} />
        </button>
      ))}
    </div>
  );
}

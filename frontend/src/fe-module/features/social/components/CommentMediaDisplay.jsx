import { PostMediaThumbnail } from "./PostMediaItem";

export function CommentMediaDisplay({ media = [], className = "" }) {
  const items = (media || []).filter((item) => item?.url);
  if (items.length === 0) return null;

  return (
    <div className={`mt-2 flex flex-wrap gap-2 ${className}`}>
      {items.map((item, index) => (
        <div
          key={item.url || index}
          className="h-20 w-20 shrink-0 overflow-hidden rounded-md border border-outline-variant/60 bg-surface-container-high"
        >
          <PostMediaThumbnail item={item} />
        </div>
      ))}
    </div>
  );
}
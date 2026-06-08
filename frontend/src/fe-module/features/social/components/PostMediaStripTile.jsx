import { getMediaStripTileStyle } from "../utils/postMediaAspectRatio";
import { getPostMediaUrl } from "../utils/postMediaType";
import { PostMediaItem } from "./PostMediaItem";

export function PostMediaStripTile({
  item,
  surface = "feed",
  playbackId,
  className = "",
  onActivate,
  interactive = false,
}) {
  if (!item) return null;

  const src = getPostMediaUrl(item);
  if (!src) return null;

  const tileStyle = getMediaStripTileStyle(item, surface);
  const containerClass = [
    "relative overflow-hidden bg-on-background/5",
    interactive || onActivate ? "cursor-pointer" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  const videoActivate = surface === "feed" ? onActivate : undefined;

  const mediaNode = (
    <PostMediaItem
      item={item}
      variant="inline"
      className="h-full w-full object-cover"
      playbackId={playbackId}
      onActivate={videoActivate}
    />
  );

  if (!onActivate) {
    return (
      <div className={containerClass} style={tileStyle}>
        {mediaNode}
      </div>
    );
  }

  return (
    <div
      className={containerClass}
      style={tileStyle}
      onClick={(event) => {
        event.stopPropagation();
        onActivate(event);
      }}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          onActivate(event);
        }
      }}
      role="button"
      tabIndex={0}
    >
      {mediaNode}
    </div>
  );
}

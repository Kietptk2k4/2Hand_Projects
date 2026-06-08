import { getMediaStageStyle } from "../utils/postMediaAspectRatio";
import { getPostMediaUrl } from "../utils/postMediaType";
import { PostMediaItem } from "./PostMediaItem";

export function PostMediaStage({
  item,
  postId,
  surface = "feed",
  playbackId,
  className = "",
  onActivate,
  interactive = false,
}) {
  if (!item) return null;

  const src = getPostMediaUrl(item);
  if (!src) return null;

  const stageStyle = getMediaStageStyle(item);
  const containerClass = [
    "relative w-full overflow-hidden bg-on-background/5",
    interactive || onActivate ? "cursor-pointer" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  const stageProps = {
    className: containerClass,
    style: stageStyle,
  };

  const videoActivate = surface === "feed" ? onActivate : undefined;

  const mediaNode = (
    <PostMediaItem
      item={item}
      variant="inline"
      className="h-full w-full object-contain"
      playbackId={playbackId}
      onActivate={videoActivate}
    />
  );

  if (!onActivate) {
    return <div {...stageProps}>{mediaNode}</div>;
  }

  return (
    <div
      {...stageProps}
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

import { buildPlaybackId } from "../utils/videoPlaybackId";
import { getPostMediaUrl } from "../utils/postMediaType";
import { PostMediaItem } from "./PostMediaItem";

export function PostMediaGrid({
  media = [],
  postId,
  surface = "feed",
  onMediaClick,
}) {
  const items = (media || []).filter((item) => item?.url || item?.mediaUrl).map((item) => ({
    ...item,
    url: getPostMediaUrl(item),
  }));
  if (items.length === 0) return null;

  const handleActivate = (event, index) => {
    event.stopPropagation();
    onMediaClick?.(index);
  };

  const interactiveProps = (index) =>
    onMediaClick
      ? {
          className: "cursor-pointer",
          onClick: (event) => handleActivate(event, index),
          onKeyDown: (event) => {
            if (event.key === "Enter" || event.key === " ") {
              event.preventDefault();
              handleActivate(event, index);
            }
          },
          role: "button",
          tabIndex: 0,
        }
      : {};

  if (items.length === 1) {
    return (
      <div
        className={[
          "relative h-64 w-full overflow-hidden bg-surface-container-high",
          onMediaClick ? "cursor-pointer" : "",
        ]
          .filter(Boolean)
          .join(" ")}
        onClick={(event) => handleActivate(event, 0)}
        onKeyDown={(event) => {
          if (onMediaClick && (event.key === "Enter" || event.key === " ")) {
            event.preventDefault();
            handleActivate(event, 0);
          }
        }}
        role={onMediaClick ? "button" : undefined}
        tabIndex={onMediaClick ? 0 : undefined}
      >
        <PostMediaItem
          item={items[0]}
          variant="inline"
          className="h-full w-full object-cover"
          playbackId={buildPlaybackId(postId, 0, surface)}
        />
      </div>
    );
  }

  if (items.length === 2) {
    return (
      <div className="grid h-64 grid-cols-2 gap-0.5 bg-surface-container-high">
        {items.map((item, index) => (
          <div key={item.url || index} {...interactiveProps(index)}>
            <PostMediaItem
              item={item}
              variant="grid"
              className="h-full w-full object-cover"
              playbackId={buildPlaybackId(postId, index, surface)}
            />
          </div>
        ))}
      </div>
    );
  }

  const visible = items.slice(0, 4);
  const overflow = items.length - visible.length;

  return (
    <div className="grid h-64 grid-cols-2 grid-rows-2 gap-0.5 bg-surface-container-high">
      {visible.map((item, index) => {
        const isLast = index === visible.length - 1 && overflow > 0;
        return (
          <div
            key={item.url || index}
            className={["relative h-full w-full", onMediaClick ? "cursor-pointer" : ""]
              .filter(Boolean)
              .join(" ")}
            {...interactiveProps(index)}
          >
            <PostMediaItem
              item={item}
              variant="grid"
              className="h-full w-full object-cover"
              playbackId={buildPlaybackId(postId, index, surface)}
            />
            {isLast ? (
              <div className="pointer-events-none absolute inset-0 flex items-center justify-center bg-on-surface/50 text-xl font-semibold text-on-primary">
                +{overflow}
              </div>
            ) : null}
          </div>
        );
      })}
    </div>
  );
}

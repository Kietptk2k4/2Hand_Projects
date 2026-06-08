import { useCallback, useMemo } from "react";
import { useHorizontalScrollDrag } from "../hooks/useHorizontalScrollDrag";
import { buildPlaybackId } from "../utils/videoPlaybackId";
import { getPostMediaUrl } from "../utils/postMediaType";
import { PostMediaStage } from "./PostMediaStage";
import { PostMediaStripTile } from "./PostMediaStripTile";

const SCROLL_STRIP_CLASS =
  "flex w-full gap-1 overflow-x-auto scroll-smooth touch-pan-x overscroll-x-contain [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden";

export function PostMediaCarousel({
  media = [],
  postId,
  surface = "feed",
  onMediaClick,
  className = "",
}) {
  const items = useMemo(
    () =>
      (media || [])
        .filter((item) => item?.url || item?.mediaUrl)
        .map((item) => ({ ...item, url: getPostMediaUrl(item) })),
    [media],
  );

  const {
    scrollRef,
    onPointerDown,
    onPointerMove,
    onPointerUp,
    onPointerCancel,
    wasDragged,
    resetDrag,
  } = useHorizontalScrollDrag();

  const handleMediaActivate = useCallback(
    (index) => (event) => {
      if (wasDragged()) {
        event.preventDefault();
        event.stopPropagation();
        resetDrag();
        return;
      }
      event.stopPropagation();
      onMediaClick?.(index);
    },
    [onMediaClick, resetDrag, wasDragged],
  );

  if (items.length === 0) return null;

  if (items.length === 1) {
    return (
      <PostMediaStage
        item={items[0]}
        postId={postId}
        surface={surface}
        playbackId={buildPlaybackId(postId, 0, surface)}
        className={["rounded-xl", className].filter(Boolean).join(" ")}
        onActivate={onMediaClick ? handleMediaActivate(0) : undefined}
        interactive={Boolean(onMediaClick)}
      />
    );
  }

  return (
    <div className={["w-full", className].filter(Boolean).join(" ")}>
      <div
        ref={scrollRef}
        className={SCROLL_STRIP_CLASS}
        onPointerDown={(event) => {
          if (event.target.closest("video")) return;
          onPointerDown(event);
        }}
        onPointerMove={onPointerMove}
        onPointerUp={onPointerUp}
        onPointerCancel={onPointerCancel}
        role="region"
        aria-label="Media gallery"
        aria-roledescription="carousel"
      >
        {items.map((item, index) => (
          <PostMediaStripTile
            key={item.url || index}
            item={item}
            surface={surface}
            playbackId={buildPlaybackId(postId, index, surface)}
            className="rounded-xl"
            onActivate={onMediaClick ? handleMediaActivate(index) : undefined}
            interactive={Boolean(onMediaClick)}
          />
        ))}
        <div className="w-4 shrink-0" aria-hidden="true" />
      </div>
    </div>
  );
}

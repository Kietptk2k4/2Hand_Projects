import { useCallback, useEffect, useMemo, useState } from "react";
import { useHorizontalScrollDrag } from "../hooks/useHorizontalScrollDrag";
import { buildPlaybackId } from "../utils/videoPlaybackId";
import { getPostMediaUrl } from "../utils/postMediaType";
import { PostMediaStage } from "./PostMediaStage";
import { PostMediaStripTile } from "./PostMediaStripTile";

const SCROLL_STRIP_CLASS =
  "flex w-full gap-2 overflow-x-auto scroll-smooth touch-pan-x overscroll-x-contain [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden rounded-2xl select-none";

export function PostMediaCarousel({
  media = [],
  postId,
  surface = "feed",
  onMediaClick,
  className = "",
}) {
  const [currentIndex, setCurrentIndex] = useState(0);

  const items = useMemo(
    () =>
      (media || [])
        .filter((item) => item?.url || item?.mediaUrl)
        .map((item) => ({ ...item, url: getPostMediaUrl(item) })),
    [media],
  );

  useEffect(() => {
    setCurrentIndex(0);
  }, [postId, media]);

  const {
    scrollRef,
    onPointerDown,
    onPointerMove,
    onPointerUp,
    onPointerCancel,
    wasDragged,
    resetDrag,
  } = useHorizontalScrollDrag();

  const handleMediaActivateFeed = useCallback(
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

  const handleMediaActivateDetail = useCallback(
    (index) => (event) => {
      event?.stopPropagation();
      onMediaClick?.(index);
    },
    [onMediaClick],
  );

  const handlePrev = useCallback((event) => {
    event?.stopPropagation();
    setCurrentIndex((prev) => Math.max(0, prev - 1));
  }, []);

  const handleNext = useCallback(
    (event) => {
      event?.stopPropagation();
      setCurrentIndex((prev) => Math.min(items.length - 1, prev + 1));
    },
    [items.length],
  );

  if (items.length === 0) return null;

  // Single media item -> Render PostMediaStage
  if (items.length === 1) {
    return (
      <div className={["w-full h-full flex items-center justify-center overflow-hidden rounded-2xl", className].filter(Boolean).join(" ")}>
        <PostMediaStage
          item={items[0]}
          postId={postId}
          surface={surface}
          playbackId={buildPlaybackId(postId, 0, surface)}
          className="w-full h-full object-contain"
          onActivate={onMediaClick ? handleMediaActivateDetail(0) : undefined}
          interactive={Boolean(onMediaClick)}
        />
      </div>
    );
  }

  // Multi-media in FEED surface (giao diện ngoài bài viết) -> Scroll / Drag ngang với nhiều hình trên 1 ô
  if (surface === "feed") {
    return (
      <div className={["w-full overflow-hidden rounded-2xl border border-outline-variant/30 bg-black/5", className].filter(Boolean).join(" ")}>
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
              className="rounded-xl shrink-0 overflow-hidden"
              onActivate={onMediaClick ? handleMediaActivateFeed(index) : undefined}
              interactive={Boolean(onMediaClick)}
            />
          ))}
          <div className="w-2 shrink-0" aria-hidden="true" />
        </div>
      </div>
    );
  }

  // Multi-media in DETAIL surface (modal chi tiết bài viết) -> 1 ảnh tại 1 thời điểm fit khung + nút Nút trái/phải
  const activeItem = items[currentIndex] || items[0];

  return (
    <div
      className={[
        "relative group w-full h-full flex items-center justify-center overflow-hidden bg-black",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <PostMediaStage
        item={activeItem}
        postId={postId}
        surface={surface}
        playbackId={buildPlaybackId(postId, currentIndex, surface)}
        className="w-full h-full object-contain"
        onActivate={onMediaClick ? handleMediaActivateDetail(currentIndex) : undefined}
        interactive={Boolean(onMediaClick)}
      />

      {/* Previous Button */}
      {currentIndex > 0 ? (
        <button
          type="button"
          onClick={handlePrev}
          className="absolute left-3 top-1/2 -translate-y-1/2 z-20 flex h-10 w-10 items-center justify-center rounded-full bg-black/60 text-white shadow-lg backdrop-blur-xs transition-all hover:bg-black/90 hover:scale-105 active:scale-95"
          aria-label="Hình trước"
        >
          <span className="material-symbols-outlined text-2xl" aria-hidden="true">
            chevron_left
          </span>
        </button>
      ) : null}

      {/* Next Button */}
      {currentIndex < items.length - 1 ? (
        <button
          type="button"
          onClick={handleNext}
          className="absolute right-3 top-1/2 -translate-y-1/2 z-20 flex h-10 w-10 items-center justify-center rounded-full bg-black/60 text-white shadow-lg backdrop-blur-xs transition-all hover:bg-black/90 hover:scale-105 active:scale-95"
          aria-label="Hình kế tiếp"
        >
          <span className="material-symbols-outlined text-2xl" aria-hidden="true">
            chevron_right
          </span>
        </button>
      ) : null}

      {/* Dots / Counter Badge */}
      <div className="absolute bottom-3 left-1/2 -translate-x-1/2 z-20 flex items-center gap-1.5 rounded-full bg-black/60 px-3 py-1 text-xs font-semibold text-white shadow-md backdrop-blur-xs">
        <span>
          {currentIndex + 1} / {items.length}
        </span>
      </div>
    </div>
  );
}

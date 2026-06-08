import { useEffect, useRef } from "react";
import { useVideoPlayback } from "../context/VideoPlaybackContext";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";

export function PostMediaThumbnail({ item, className = "", fallbackSrc, alt = "" }) {
  const src = item?.url || item?.mediaUrl ? getPostMediaUrl(item) : fallbackSrc;
  if (!src) return null;

  const isVideo = item ? isPostVideoMedia(item) : false;

  return (
    <div className={`relative h-full w-full overflow-hidden ${className}`}>
      {isVideo ? (
        <>
          <video
            src={src}
            className="h-full w-full object-cover"
            muted
            playsInline
            preload="metadata"
            tabIndex={-1}
            aria-hidden="true"
          />
          <div className="pointer-events-none absolute inset-0 flex items-center justify-center bg-on-background/25">
            <span
              className="material-symbols-outlined text-4xl text-on-primary drop-shadow-md md:text-5xl"
              style={{ fontVariationSettings: "'FILL' 1" }}
              aria-hidden="true"
            >
              play_circle
            </span>
          </div>
        </>
      ) : (
        <img src={src} alt={alt} className="h-full w-full object-cover" loading="lazy" />
      )}
    </div>
  );
}

function PostMediaVideoPlayer({
  src,
  className,
  showControls,
  playbackId,
  onActivate,
}) {
  const videoRef = useRef(null);
  const { registerPlayer, claimPlayback, releasePlayback, isActivePlayer, activePlaybackId } =
    useVideoPlayback();

  useEffect(() => {
    if (!playbackId || !videoRef.current) return undefined;
    return registerPlayer(playbackId, videoRef.current);
  }, [playbackId, registerPlayer]);

  useEffect(() => {
    const element = videoRef.current;
    if (!playbackId || !element) return;
    if (activePlaybackId && activePlaybackId !== playbackId && !element.paused) {
      element.pause();
    }
  }, [activePlaybackId, playbackId]);

  const handlePlay = () => {
    if (playbackId) {
      claimPlayback(playbackId);
    }
  };

  const handlePause = () => {
    if (playbackId && isActivePlayer(playbackId)) {
      releasePlayback(playbackId);
    }
  };

  const handleEnded = () => {
    if (playbackId) {
      releasePlayback(playbackId);
    }
  };

  return (
    <video
      ref={videoRef}
      src={src}
      className={className}
      controls={showControls}
      playsInline
      preload="metadata"
      onPlay={handlePlay}
      onPause={handlePause}
      onEnded={handleEnded}
      onClick={(event) => {
        event.stopPropagation();
        onActivate?.(event);
      }}
      onKeyDown={(event) => event.stopPropagation()}
    />
  );
}

export function PostMediaItem({
  item,
  className = "",
  variant = "inline",
  controls,
  loading = "lazy",
  playbackId = null,
  onActivate,
}) {
  const src = getPostMediaUrl(item);
  if (!src) return null;

  const isVideo = isPostVideoMedia(item);
  const showControls = controls ?? (variant === "inline" && isVideo);

  if (variant === "thumbnail" || (variant === "grid" && isVideo)) {
    return <PostMediaThumbnail item={item} className={className} />;
  }

  if (isVideo) {
    return (
      <PostMediaVideoPlayer
        src={src}
        className={className}
        showControls={showControls}
        playbackId={playbackId}
        onActivate={onActivate}
      />
    );
  }

  return <img src={src} alt="" className={className} loading={loading} />;
}
import { useEffect, useRef } from "react";
import { useVideoPlayback } from "../context/VideoPlaybackContext";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";
import { buildPlaybackId, VIDEO_PLAYBACK_SURFACES } from "../utils/videoPlaybackId";

export function MediaGalleryLightbox({
  media = [],
  postId,
  ownerId,
  initialIndex = 0,
  onClose,
}) {
  const playbackOwnerId = ownerId || postId;
  const videoRef = useRef(null);
  const { registerPlayer, claimPlayback, releasePlayback, pauseAll } = useVideoPlayback();

  const items = (media || [])
    .filter((item) => item?.url || item?.mediaUrl)
    .map((item) => ({ ...item, url: getPostMediaUrl(item) }));
  const safeIndex = Math.min(Math.max(initialIndex, 0), Math.max(items.length - 1, 0));
  const current = items[safeIndex];
  const isVideo = current ? isPostVideoMedia(current) : false;
  const playbackId = buildPlaybackId(playbackOwnerId, safeIndex, VIDEO_PLAYBACK_SURFACES.GALLERY);

  useEffect(() => {
    const onKeyDown = (event) => {
      if (event.key === "Escape") onClose?.();
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  useEffect(() => {
    if (!isVideo || !playbackId || !videoRef.current) return undefined;
    return registerPlayer(playbackId, videoRef.current);
  }, [isVideo, playbackId, registerPlayer]);

  useEffect(() => {
    if (!isVideo || !playbackId || !videoRef.current) return undefined;

    claimPlayback(playbackId);
    videoRef.current.play().catch(() => {});

    return () => {
      releasePlayback(playbackId);
    };
  }, [claimPlayback, isVideo, playbackId, releasePlayback, safeIndex]);

  const handleClose = () => {
    pauseAll();
    onClose?.();
  };

  if (!current) return null;

  return (
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center bg-on-background/80 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label={isVideo ? "Xem video" : "Xem ảnh"}
      onClick={handleClose}
    >
      <button
        type="button"
        onClick={handleClose}
        className="absolute right-4 top-4 z-10 rounded-full bg-surface-container-lowest/90 p-2 text-on-surface hover:bg-surface-variant"
        aria-label="Đóng gallery"
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          close
        </span>
      </button>
      {isVideo ? (
        <video
          ref={videoRef}
          src={current.url}
          className="max-h-[90vh] max-w-full rounded-lg bg-on-background"
          controls
          playsInline
          onClick={(event) => event.stopPropagation()}
        />
      ) : (
        <img
          src={current.url}
          alt=""
          className="max-h-[90vh] max-w-full rounded-lg object-contain"
          onClick={(event) => event.stopPropagation()}
        />
      )}
      {items.length > 1 ? (
        <p className="absolute bottom-6 text-sm text-on-primary">
          {safeIndex + 1} / {items.length}
        </p>
      ) : null}
    </div>
  );
}

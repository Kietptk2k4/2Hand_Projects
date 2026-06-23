export const VIDEO_PLAYBACK_SURFACES = {
  FEED: "feed",
  DETAIL: "detail",
  GALLERY: "gallery",
};

export function buildPlaybackId(postId, mediaIndex = 0, surface = VIDEO_PLAYBACK_SURFACES.FEED) {
  if (!postId) return null;
  return `${surface}:${postId}:${mediaIndex}`;
}

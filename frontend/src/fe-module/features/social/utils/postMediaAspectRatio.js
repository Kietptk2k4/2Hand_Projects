/** Instagram-like feed bounds: portrait 4:5 .. landscape 1.91:1 (width / height). */
const MIN_WIDTH_HEIGHT_RATIO = 4 / 5;
const MAX_WIDTH_HEIGHT_RATIO = 1.91;
const DEFAULT_RATIO = 1;

export function normalizeMediaDimensions(item) {
  const width = Number(item?.width) || null;
  const height = Number(item?.height) || null;
  if (!width || !height || width <= 0 || height <= 0) {
    return { width: null, height: null };
  }
  return { width, height };
}

export function getClampedAspectRatio(width, height, fallback = DEFAULT_RATIO) {
  if (!width || !height || width <= 0 || height <= 0) {
    return fallback;
  }
  const ratio = width / height;
  return Math.min(MAX_WIDTH_HEIGHT_RATIO, Math.max(MIN_WIDTH_HEIGHT_RATIO, ratio));
}

export function getMediaStageStyle(item) {
  const { width, height } = normalizeMediaDimensions(item);
  const ratio = getClampedAspectRatio(width, height);
  return { aspectRatio: String(ratio) };
}

const STRIP_HEIGHT_FEED_PX = 280;
const STRIP_HEIGHT_DETAIL_PX = 360;

export function getMediaStripHeightPx(surface = "feed") {
  return surface === "detail" ? STRIP_HEIGHT_DETAIL_PX : STRIP_HEIGHT_FEED_PX;
}

/** Threads-style tile: fixed height, width from clamped aspect ratio. */
export function getMediaStripTileStyle(item, surface = "feed") {
  const { width, height } = normalizeMediaDimensions(item);
  const ratio = getClampedAspectRatio(width, height);
  return {
    height: `${getMediaStripHeightPx(surface)}px`,
    aspectRatio: String(ratio),
    flexShrink: 0,
  };
}

export function mapPostMediaPayload(item) {
  const { width, height } = normalizeMediaDimensions(item);
  const payload = {
    url: item.url || item.mediaUrl,
    type: item.type,
  };
  if (width && height) {
    payload.width = width;
    payload.height = height;
  }
  return payload;
}

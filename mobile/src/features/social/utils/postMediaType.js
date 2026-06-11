import { normalizePostMediaUrl } from "./postMediaUrl";

export function isPostVideoMedia(item) {
  if (!item) return false;

  const type = String(item.type || item.mediaType || "").toUpperCase();
  if (type === "VIDEO") return true;

  const url = String(item.url || item.mediaUrl || "").toLowerCase();
  return /\.(mp4|webm|mov)(\?|#|$)/i.test(url);
}

export function getPostMediaUrl(item) {
  const raw = item?.url || item?.mediaUrl || "";
  return normalizePostMediaUrl(raw);
}

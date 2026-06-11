export function normalizeHashtagParam(raw) {
  if (raw === undefined || raw === null) return "";
  try {
    return decodeURIComponent(String(raw)).replace(/^#+/, "").trim();
  } catch {
    return String(raw).replace(/^#+/, "").trim();
  }
}

export function isValidHashtagParam(hashtag) {
  const normalized = normalizeHashtagParam(hashtag);
  if (!normalized) return false;
  if (normalized.length > 100) return false;
  return /^[a-zA-Z0-9_]+$/.test(normalized);
}

export function formatHashtagLabel(hashtag) {
  const normalized = normalizeHashtagParam(hashtag);
  return normalized ? `#${normalized}` : "#";
}

export function normalizeSearchKeyword(raw) {
  if (raw == null || typeof raw !== "string") return "";
  return raw.trim().replace(/\s+/g, " ");
}

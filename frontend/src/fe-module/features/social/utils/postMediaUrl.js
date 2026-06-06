const LEGACY_LOCAL_SOCIAL_SEGMENT = "/2hands-social-post/social/";

export function normalizePostMediaUrl(url) {
  if (!url || typeof url !== "string") return url;
  if (url.includes(LEGACY_LOCAL_SOCIAL_SEGMENT)) {
    return url.replace(LEGACY_LOCAL_SOCIAL_SEGMENT, "/2hands-social-post/");
  }
  return url;
}
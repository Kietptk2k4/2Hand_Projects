import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";

const LEGACY_LOCAL_SOCIAL_SEGMENT = "/2hands-social-post/social/";

export function normalizePostMediaUrl(url) {
  if (!url || typeof url !== "string") return url;

  let next = url;
  if (next.includes(LEGACY_LOCAL_SOCIAL_SEGMENT)) {
    next = next.replace(LEGACY_LOCAL_SOCIAL_SEGMENT, "/2hands-social-post/");
  }

  return resolveDevMediaUrl(next);
}

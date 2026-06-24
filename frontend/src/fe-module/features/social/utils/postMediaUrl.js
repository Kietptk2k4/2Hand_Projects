import { resolveDevMediaUrl } from "../../../shared/utils/getClientUploadOrigin";

const LEGACY_LOCAL_SOCIAL_SEGMENT = "/2hands-social-post/social/";

export function normalizePostMediaUrl(url) {
  if (!url || typeof url !== "string") return url;
  let normalized = url;
  if (normalized.includes(LEGACY_LOCAL_SOCIAL_SEGMENT)) {
    normalized = normalized.replace(LEGACY_LOCAL_SOCIAL_SEGMENT, "/2hands-social-post/");
  }
  return resolveDevMediaUrl(normalized);
}
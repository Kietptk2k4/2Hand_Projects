import { APP_ROUTES } from "../../../shared/constants/routes";

export function buildSocialSearchPath(keyword) {
  const trimmed = keyword?.trim();
  if (!trimmed) return APP_ROUTES.socialSearchPosts;
  return `${APP_ROUTES.socialSearchPosts}?q=${encodeURIComponent(trimmed)}`;
}

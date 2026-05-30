import { APP_ROUTES } from "../../../shared/constants/routes";

export function buildSocialProfilePath(userId) {
  if (!userId) return APP_ROUTES.socialFeed;
  return APP_ROUTES.socialProfile.replace(":userId", encodeURIComponent(userId));
}

import { router } from "expo-router";
import { clearSessionTokens } from "../../../services/auth/tokenStorage";
import { ROUTES } from "../../../shared/constants/routes";
import { setSessionExpiredMessage } from "../../auth/utils/authNavigationState";

export async function handleSocialQueryError(error) {
  if (error?.code === 401 || error?.code === "SOCIAL-401") {
    setSessionExpiredMessage(error?.message);
    await clearSessionTokens();
    router.replace(ROUTES.sessionExpired);
    return true;
  }
  return false;
}

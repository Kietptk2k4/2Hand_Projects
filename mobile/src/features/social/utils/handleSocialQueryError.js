import { router } from "expo-router";
import { clearSessionTokens } from "../../../services/auth/tokenStorage";
import { ROUTES } from "../../../shared/constants/routes";

export async function handleSocialQueryError(error) {
  if (error?.code === 401 || error?.code === "SOCIAL-401") {
    await clearSessionTokens();
    router.replace(ROUTES.login);
    return true;
  }
  return false;
}

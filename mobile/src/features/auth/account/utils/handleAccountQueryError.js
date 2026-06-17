import { router } from "expo-router";
import { clearSessionTokens } from "../../../../services/auth/tokenStorage";
import { ROUTES } from "../../../../shared/constants/routes";
import { setSessionExpiredMessage } from "../../utils/authNavigationState";

export async function handleAccountQueryError(error) {
  if (error?.code === 401) {
    setSessionExpiredMessage(error?.message);
    await clearSessionTokens();
    router.replace(ROUTES.sessionExpired);
    return true;
  }
  return false;
}
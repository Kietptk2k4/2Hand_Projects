import { router } from "expo-router";
import { clearSessionTokens } from "../../../../services/auth/tokenStorage";
import { ROUTES } from "../../../../shared/constants/routes";

export async function handleAccountQueryError(error) {
  if (error?.code === 401) {
    await clearSessionTokens();
    router.replace(ROUTES.login);
    return true;
  }
  return false;
}
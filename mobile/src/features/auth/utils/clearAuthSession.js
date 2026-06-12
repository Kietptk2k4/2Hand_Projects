import { router } from "expo-router";
import { clearSessionTokens } from "../../../services/auth/tokenStorage";
import { queryClient } from "../../../services/query/queryClient";
import { ROUTES } from "../../../shared/constants/routes";

export async function clearAuthSession({ redirectToLogin = true } = {}) {
  await clearSessionTokens();
  queryClient.clear();

  if (redirectToLogin) {
    router.replace(ROUTES.login);
  }
}
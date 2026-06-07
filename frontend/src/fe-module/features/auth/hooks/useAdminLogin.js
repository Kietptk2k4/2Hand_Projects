import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginWithAdminEmail } from "../api/adminAuthApi";
import { ADMIN_LOGIN_ERROR_BY_CODE } from "../constants/adminAuthUiStrings";
import { GENERIC_ERROR_RETRY } from "../constants/authUiStrings";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useAuthSession } from "./useAuthSession.jsx";
import { ADMIN_SESSION_KIND, buildAdminUserFromLoginData } from "../utils/adminSession";

function resolveAdminLoginError(error) {
  const code = error?.code || 500;
  if (code === 400) {
    return error?.message || "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.";
  }
  return ADMIN_LOGIN_ERROR_BY_CODE[code] || GENERIC_ERROR_RETRY;
}

export function useAdminLogin({ redirectUrl = APP_ROUTES.admin } = {}) {
  const navigate = useNavigate();
  const { setSession } = useAuthSession();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const performAdminLogin = useCallback(
    async (credentials) => {
      setIsSubmitting(true);
      try {
        const loginData = await loginWithAdminEmail(credentials);
        const adminUser = buildAdminUserFromLoginData(loginData);

        setSession({
          accessToken: loginData.access_token,
          refreshToken: loginData.refresh_token,
          user: adminUser,
          sessionKind: ADMIN_SESSION_KIND,
        });

        navigate(redirectUrl, { replace: true });
        return { success: true };
      } catch (error) {
        return { success: false, message: resolveAdminLoginError(error), error };
      } finally {
        setIsSubmitting(false);
      }
    },
    [navigate, redirectUrl, setSession],
  );

  return { performAdminLogin, isSubmitting };
}

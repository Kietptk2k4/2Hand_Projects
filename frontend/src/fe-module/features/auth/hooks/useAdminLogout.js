import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { logoutAdminWithRefreshToken } from "../api/adminAuthApi";
import {
  ADMIN_LOGOUT_FALLBACK_MESSAGE,
  ADMIN_LOGOUT_SUCCESS_MESSAGE,
} from "../constants/adminAuthUiStrings";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useAuthSession } from "./useAuthSession.jsx";

export function useAdminLogout() {
  const navigate = useNavigate();
  const { accessToken, refreshToken, clearSession, hideSessionExpired } = useAuthSession();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const performAdminLogout = useCallback(async () => {
    setIsLoggingOut(true);
    const token = refreshToken || localStorage.getItem("twohands_refresh_token");
    const bearer = accessToken || localStorage.getItem("twohands_access_token");
    let logoutMessage = ADMIN_LOGOUT_SUCCESS_MESSAGE;

    try {
      if (token) {
        await logoutAdminWithRefreshToken(token, bearer);
      }
    } catch (error) {
      if (error?.code === 500 || error?.code === "NETWORK" || !error?.code) {
        logoutMessage = ADMIN_LOGOUT_FALLBACK_MESSAGE;
      }
    } finally {
      hideSessionExpired();
      clearSession();
      setIsLoggingOut(false);
      navigate(APP_ROUTES.adminLogin, {
        replace: true,
        state: { logoutMessage },
      });
    }
  }, [accessToken, refreshToken, clearSession, hideSessionExpired, navigate]);

  return { performAdminLogout, isLoggingOut };
}

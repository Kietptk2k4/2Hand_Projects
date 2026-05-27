import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { logoutWithRefreshToken } from "../api/authApi";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useAuthSession } from "./useAuthSession.jsx";

export const LOGOUT_FALLBACK_MESSAGE =
  "Da dang xuat tren thiet bi nay. Neu can, vui long thu lai.";

export function useLogout() {
  const navigate = useNavigate();
  const { refreshToken, clearSession, hideSessionExpired } = useAuthSession();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const performLogout = useCallback(async () => {
    setIsLoggingOut(true);
    const token = refreshToken || localStorage.getItem("twohands_refresh_token");
    let logoutMessage = null;

    try {
      if (token) {
        await logoutWithRefreshToken(token);
      }
    } catch (error) {
      // Always clear local session after logout attempt (400, 500, network).
      if (error?.code === 500 || error?.code === "NETWORK" || !error?.code) {
        logoutMessage = LOGOUT_FALLBACK_MESSAGE;
      }
    } finally {
      hideSessionExpired();
      clearSession();
      setIsLoggingOut(false);
      navigate(APP_ROUTES.login, {
        replace: true,
        state: logoutMessage ? { logoutMessage } : undefined,
      });
    }
  }, [refreshToken, clearSession, hideSessionExpired, navigate]);

  return { performLogout, isLoggingOut };
}

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { configureAuthRefreshService } from "../../../services/http/authRefreshService";

const AuthSessionContext = createContext(null);
const DEFAULT_SESSION_EXPIRED_MESSAGE = "Phien dang nhap da het han, vui long dang nhap lai.";

export function AuthSessionProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null);
  const [refreshToken, setRefreshToken] = useState(null);
  const [user, setUser] = useState(null);
  const [sessionExpiredState, setSessionExpiredState] = useState({
    isOpen: false,
    message: DEFAULT_SESSION_EXPIRED_MESSAGE,
  });

  const setSession = ({ accessToken: nextAccessToken, refreshToken: nextRefreshToken, user: nextUser }) => {
    setAccessToken(nextAccessToken || null);
    setRefreshToken(nextRefreshToken || null);
    setUser(nextUser || null);

    if (nextAccessToken) {
      localStorage.setItem("twohands_access_token", nextAccessToken);
    }
    if (nextRefreshToken) {
      localStorage.setItem("twohands_refresh_token", nextRefreshToken);
    }
  };

  const clearSession = useCallback(() => {
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
    localStorage.removeItem("twohands_access_token");
    localStorage.removeItem("twohands_refresh_token");
  }, []);

  const showSessionExpired = useCallback((message = DEFAULT_SESSION_EXPIRED_MESSAGE) => {
    setSessionExpiredState({
      isOpen: true,
      message: message || DEFAULT_SESSION_EXPIRED_MESSAGE,
    });
  }, []);

  const hideSessionExpired = useCallback(() => {
    setSessionExpiredState((prev) => ({ ...prev, isOpen: false }));
  }, []);

  const value = useMemo(
    () => ({
      accessToken,
      refreshToken,
      user,
      setSession,
      setAccessToken,
      isAuthenticated: Boolean(accessToken || localStorage.getItem("twohands_access_token")),
      clearSession,
      sessionExpiredState,
      showSessionExpired,
      hideSessionExpired,
    }),
    [accessToken, refreshToken, user, sessionExpiredState]
  );

  useEffect(() => {
    configureAuthRefreshService({
      getAccessToken: () => localStorage.getItem("twohands_access_token"),
      getRefreshToken: () => localStorage.getItem("twohands_refresh_token"),
      setAccessToken: (nextAccessToken) => {
        if (nextAccessToken) {
          setAccessToken(nextAccessToken);
          localStorage.setItem("twohands_access_token", nextAccessToken);
        } else {
          setAccessToken(null);
          localStorage.removeItem("twohands_access_token");
        }
      },
      clearSession: () => {
        clearSession();
      },
      onSessionExpired: (message) => {
        showSessionExpired(message);
      },
    });
  }, [clearSession, showSessionExpired]);

  return <AuthSessionContext.Provider value={value}>{children}</AuthSessionContext.Provider>;
}

export function useAuthSession() {
  const context = useContext(AuthSessionContext);
  if (!context) {
    throw new Error("useAuthSession must be used inside AuthSessionProvider");
  }
  return context;
}


import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { configureAuthRefreshService } from "../../../services/http/authRefreshService";
import {
  persistStoredUser,
  readStoredUser,
  resolveUserIdFromAccessToken,
} from "../utils/resolveCurrentUserId";
import { SESSION_EXPIRED_DEFAULT_MESSAGE } from "../constants/authUiStrings";
import { persistSessionKind, readSessionKind } from "../utils/adminSession";

const AuthSessionContext = createContext(null);
const DEFAULT_SESSION_EXPIRED_MESSAGE = SESSION_EXPIRED_DEFAULT_MESSAGE;

function readInitialAccessToken() {
  return localStorage.getItem("twohands_access_token");
}

function readInitialUser() {
  const stored = readStoredUser();
  if (stored?.id) return stored;

  const token = readInitialAccessToken();
  const idFromToken = resolveUserIdFromAccessToken(token);
  if (idFromToken) {
    return { id: idFromToken };
  }

  return null;
}

export function AuthSessionProvider({ children }) {
  const [accessToken, setAccessToken] = useState(readInitialAccessToken);
  const [refreshToken, setRefreshToken] = useState(() =>
    localStorage.getItem("twohands_refresh_token")
  );
  const [user, setUser] = useState(readInitialUser);
  const [sessionKind, setSessionKind] = useState(readSessionKind);
  const [sessionExpiredState, setSessionExpiredState] = useState({
    isOpen: false,
    message: DEFAULT_SESSION_EXPIRED_MESSAGE,
  });

  const setSession = ({
    accessToken: nextAccessToken,
    refreshToken: nextRefreshToken,
    user: nextUser,
    sessionKind: nextSessionKind,
  }) => {
    setAccessToken(nextAccessToken || null);
    setRefreshToken(nextRefreshToken || null);
    setUser(nextUser || null);

    if (nextSessionKind) {
      setSessionKind(nextSessionKind);
      persistSessionKind(nextSessionKind);
    }

    persistStoredUser(nextUser);

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
    setSessionKind(null);
    localStorage.removeItem("twohands_access_token");
    localStorage.removeItem("twohands_refresh_token");
    persistStoredUser(null);
    persistSessionKind(null);
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
      sessionKind,
      setSession,
      setAccessToken,
      isAuthenticated: Boolean(accessToken || localStorage.getItem("twohands_access_token")),
      isAdminSession: sessionKind === "admin",
      clearSession,
      sessionExpiredState,
      showSessionExpired,
      hideSessionExpired,
    }),
    [accessToken, refreshToken, user, sessionKind, sessionExpiredState]
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


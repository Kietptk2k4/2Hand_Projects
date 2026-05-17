import { createContext, useContext, useMemo, useState } from "react";

const AuthSessionContext = createContext(null);

export function AuthSessionProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null);
  const [refreshToken, setRefreshToken] = useState(null);
  const [user, setUser] = useState(null);

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

  const clearSession = () => {
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
    localStorage.removeItem("twohands_access_token");
    localStorage.removeItem("twohands_refresh_token");
  };

  const value = useMemo(
    () => ({
      accessToken,
      refreshToken,
      user,
      setSession,
      setAccessToken,
      isAuthenticated: Boolean(accessToken || localStorage.getItem("twohands_access_token")),
      clearSession,
    }),
    [accessToken, refreshToken, user]
  );

  return <AuthSessionContext.Provider value={value}>{children}</AuthSessionContext.Provider>;
}

export function useAuthSession() {
  const context = useContext(AuthSessionContext);
  if (!context) {
    throw new Error("useAuthSession must be used inside AuthSessionProvider");
  }
  return context;
}


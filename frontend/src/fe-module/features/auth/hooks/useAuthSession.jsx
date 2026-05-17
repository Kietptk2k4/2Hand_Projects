import { createContext, useContext, useMemo, useState } from "react";

const AuthSessionContext = createContext(null);

export function AuthSessionProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null);

  const clearSession = () => {
    setAccessToken(null);
    localStorage.removeItem("twohands_access_token");
    localStorage.removeItem("twohands_refresh_token");
  };

  const value = useMemo(
    () => ({
      accessToken,
      setAccessToken,
      isAuthenticated: Boolean(accessToken),
      clearSession,
    }),
    [accessToken]
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


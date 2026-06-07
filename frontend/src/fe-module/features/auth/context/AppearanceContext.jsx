import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { getMyProfile } from "../api/authApi";
import { useAuthSession } from "../hooks/useAuthSession.jsx";
import {
  applyAppearanceToDocument,
  normalizeAppearanceMode,
  persistAppearanceMode,
  readStoredAppearanceMode,
} from "../utils/appearanceTheme";

const AppearanceContext = createContext(null);

export function AppearanceProvider({ children }) {
  const { isAuthenticated, user } = useAuthSession();
  const [appearanceMode, setAppearanceModeState] = useState(() =>
    normalizeAppearanceMode(readStoredAppearanceMode())
  );
  const syncedUserIdRef = useRef(null);

  const setAppearanceMode = useCallback((mode, { persist = true } = {}) => {
    const normalized = normalizeAppearanceMode(mode);
    setAppearanceModeState(normalized);
    applyAppearanceToDocument(normalized);
    if (persist) {
      persistAppearanceMode(normalized);
    }
  }, []);

  useEffect(() => {
    applyAppearanceToDocument(appearanceMode);
  }, [appearanceMode]);

  useEffect(() => {
    if (appearanceMode !== "SYSTEM") return undefined;

    const media = window.matchMedia("(prefers-color-scheme: dark)");
    const onChange = () => applyAppearanceToDocument("SYSTEM");

    media.addEventListener("change", onChange);
    return () => media.removeEventListener("change", onChange);
  }, [appearanceMode]);

  useEffect(() => {
    if (!isAuthenticated) {
      syncedUserIdRef.current = null;
      return;
    }

    const userId = user?.id;
    if (!userId || syncedUserIdRef.current === userId) {
      return;
    }

    let cancelled = false;

    (async () => {
      try {
        const data = await getMyProfile();
        if (cancelled) return;
        const serverMode = data?.settings?.appearance_mode;
        if (serverMode) {
          setAppearanceMode(serverMode);
        }
        syncedUserIdRef.current = userId;
      } catch {
        if (!cancelled) {
          syncedUserIdRef.current = userId;
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [isAuthenticated, setAppearanceMode, user?.id]);

  const value = useMemo(
    () => ({
      appearanceMode,
      setAppearanceMode,
    }),
    [appearanceMode, setAppearanceMode]
  );

  return <AppearanceContext.Provider value={value}>{children}</AppearanceContext.Provider>;
}

export function useAppearance() {
  const context = useContext(AppearanceContext);
  if (!context) {
    throw new Error("useAppearance must be used inside AppearanceProvider");
  }
  return context;
}
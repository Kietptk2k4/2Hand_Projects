import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { useColorScheme } from "react-native";
import { StatusBar } from "expo-status-bar";
import { getMyProfile } from "../api/authApi";
import { getAccessToken } from "../../../services/auth/tokenStorage";
import { resolveUserIdFromAccessToken } from "../../social/utils/decodeAccessToken";
import {
  normalizeAppearanceMode,
  persistAppearanceMode,
  readStoredAppearanceMode,
  resolveIsDark,
} from "../utils/appearanceTheme";

const AppearanceContext = createContext(null);

export function AppearanceProvider({ children }) {
  const systemScheme = useColorScheme();
  const [appearanceMode, setAppearanceModeState] = useState("SYSTEM");
  const [hydrated, setHydrated] = useState(false);
  const syncedUserIdRef = useRef(null);

  useEffect(() => {
    let active = true;

    (async () => {
      const stored = await readStoredAppearanceMode();
      if (active && stored) {
        setAppearanceModeState(normalizeAppearanceMode(stored));
      }
      if (active) {
        setHydrated(true);
      }
    })();

    return () => {
      active = false;
    };
  }, []);

  const setAppearanceMode = useCallback((mode, { persist = true } = {}) => {
    const normalized = normalizeAppearanceMode(mode);
    setAppearanceModeState(normalized);
    if (persist) {
      persistAppearanceMode(normalized);
    }
  }, []);

  useEffect(() => {
    if (!hydrated) return undefined;

    let cancelled = false;

    (async () => {
      const token = await getAccessToken();
      if (!token || cancelled) return;

      const userId = resolveUserIdFromAccessToken(token);
      if (!userId || syncedUserIdRef.current === userId) return;

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
  }, [hydrated, setAppearanceMode]);

  const isDark = resolveIsDark(appearanceMode, systemScheme);
  const statusBarStyle = isDark ? "light" : "dark";

  const value = useMemo(
    () => ({
      appearanceMode,
      setAppearanceMode,
      isDark,
    }),
    [appearanceMode, setAppearanceMode, isDark]
  );

  return (
    <AppearanceContext.Provider value={value}>
      <StatusBar style={statusBarStyle} />
      {children}
    </AppearanceContext.Provider>
  );
}

export function useAppearance() {
  const context = useContext(AppearanceContext);
  if (!context) {
    throw new Error("useAppearance must be used inside AppearanceProvider");
  }
  return context;
}

import { useAppearance } from "../../features/auth/context/AppearanceContext";
import { darkPalette, lightPalette } from "./palettes";

export function getThemeColors(isDark) {
  return isDark ? darkPalette : lightPalette;
}

export function useThemeColors() {
  const { isDark } = useAppearance();
  return getThemeColors(isDark);
}
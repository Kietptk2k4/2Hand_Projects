import { lightPalette } from "./palettes";

/** @deprecated Prefer useThemeColors() for theme-aware screens. */
export const colors = lightPalette;

export { darkPalette, lightPalette } from "./palettes";
export { getThemeColors, useThemeColors } from "./useThemeColors";
export { useThemedStyles } from "./useThemedStyles";

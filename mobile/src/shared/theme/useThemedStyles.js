import { useMemo } from "react";
import { StyleSheet } from "react-native";
import { useThemeColors } from "./useThemeColors";

export function useThemedStyles(styleFactory) {
  const colors = useThemeColors();
  return useMemo(() => StyleSheet.create(styleFactory(colors)), [colors, styleFactory]);
}
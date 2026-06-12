import { useMemo } from "react";
import { StyleSheet, Text, View } from "react-native";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";

export function AccountCard({ title, children }) {
  const colors = useThemeColors();

  const styles = useMemo(
    () =>
      StyleSheet.create({
        card: {
          backgroundColor: colors.surfaceContainerLowest,
          borderRadius: 16,
          borderWidth: 1,
          borderColor: colors.outlineVariant,
          padding: 16,
        },
        title: {
          fontSize: 18,
          fontWeight: "600",
          color: colors.onSurface,
          marginBottom: 12,
        },
      }),
    [colors]
  );

  return (
    <View style={styles.card}>
      {title ? <Text style={styles.title}>{title}</Text> : null}
      {children}
    </View>
  );
}

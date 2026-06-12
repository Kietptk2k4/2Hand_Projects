import { useMemo } from "react";
import { StyleSheet, View } from "react-native";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";

function SkeletonCard({ styles }) {
  return (
    <View style={styles.card}>
      <View style={[styles.line, styles.titleLine]} />
      <View style={[styles.line, styles.fullLine]} />
      <View style={[styles.line, styles.fullLine]} />
      <View style={[styles.line, styles.mediumLine]} />
    </View>
  );
}

export function AccountInfoSkeleton() {
  const colors = useThemeColors();

  const styles = useMemo(
    () =>
      StyleSheet.create({
        wrap: {
          gap: 16,
        },
        card: {
          backgroundColor: colors.surfaceContainerLowest,
          borderRadius: 16,
          borderWidth: 1,
          borderColor: colors.outlineVariant,
          padding: 16,
          gap: 12,
        },
        line: {
          height: 14,
          borderRadius: 7,
          backgroundColor: colors.surfaceContainerHigh,
        },
        titleLine: {
          width: "40%",
          height: 18,
          marginBottom: 4,
        },
        fullLine: {
          width: "100%",
        },
        mediumLine: {
          width: "70%",
        },
      }),
    [colors]
  );

  return (
    <View style={styles.wrap}>
      <SkeletonCard styles={styles} />
      <SkeletonCard styles={styles} />
      <SkeletonCard styles={styles} />
    </View>
  );
}

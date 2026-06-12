import { View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    container: { padding: 16, gap: 16 },
    summary: {
      height: 120,
      borderRadius: 16,
      backgroundColor: colors.surfaceContainerLow,
    },
    filters: {
      height: 100,
      borderRadius: 16,
      backgroundColor: colors.surfaceContainerLow,
    },
    card: {
      height: 140,
      borderRadius: 16,
      backgroundColor: colors.surfaceContainerLow,
    },
  };
}

export function ProductReviewsSkeleton() {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.container}>
      <View style={styles.summary} />
      <View style={styles.filters} />
      <View style={styles.card} />
      <View style={styles.card} />
      <View style={styles.card} />
    </View>
  );
}

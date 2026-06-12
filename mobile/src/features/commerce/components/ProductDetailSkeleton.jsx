import { View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    container: { gap: 16, padding: 16 },
    gallery: {
      height: 320,
      borderRadius: 16,
      backgroundColor: colors.surfaceContainerLow,
    },
    thumbs: { flexDirection: "row", gap: 8 },
    thumb: {
      width: 64,
      height: 64,
      borderRadius: 10,
      backgroundColor: colors.surfaceContainerLow,
    },
    title: {
      height: 28,
      width: "85%",
      borderRadius: 6,
      backgroundColor: colors.surfaceContainerLow,
    },
    meta: {
      height: 16,
      width: "55%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    price: {
      height: 32,
      width: "40%",
      borderRadius: 6,
      backgroundColor: colors.surfaceContainerLow,
    },
    card: {
      height: 120,
      borderRadius: 16,
      backgroundColor: colors.surfaceContainerLow,
    },
    section: {
      height: 100,
      borderRadius: 16,
      backgroundColor: colors.surfaceContainerLow,
    },
  };
}

export function ProductDetailSkeleton() {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.container}>
      <View style={styles.gallery} />
      <View style={styles.thumbs}>
        <View style={styles.thumb} />
        <View style={styles.thumb} />
        <View style={styles.thumb} />
      </View>
      <View style={styles.title} />
      <View style={styles.meta} />
      <View style={styles.price} />
      <View style={styles.card} />
      <View style={styles.section} />
      <View style={styles.section} />
    </View>
  );
}

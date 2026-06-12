import { View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    wrap: { gap: 16 },
    title: {
      height: 28,
      width: 200,
      borderRadius: 8,
      backgroundColor: colors.surfaceContainerHigh,
      opacity: 0.7,
    },
    hero: {
      height: 120,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
      opacity: 0.7,
    },
    blockLg: {
      height: 200,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
      opacity: 0.7,
    },
    blockMd: {
      height: 140,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
      opacity: 0.7,
    },
  };
}

export function ShipmentTrackingSkeleton() {
  const styles = useThemedStyles(createStyles);
  return (
    <View style={styles.wrap}>
      <View style={styles.title} />
      <View style={styles.hero} />
      <View style={styles.blockLg} />
      <View style={styles.blockMd} />
      <View style={styles.blockMd} />
    </View>
  );
}
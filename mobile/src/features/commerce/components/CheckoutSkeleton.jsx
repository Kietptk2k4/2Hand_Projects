import { View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    list: { gap: 12 },
    block: {
      height: 120,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
      opacity: 0.7,
    },
    summary: {
      height: 200,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
      opacity: 0.7,
    },
  };
}

export function CheckoutSkeleton() {
  const styles = useThemedStyles(createStyles);
  return (
    <View style={styles.list}>
      <View style={styles.block} />
      <View style={styles.block} />
      <View style={styles.summary} />
    </View>
  );
}
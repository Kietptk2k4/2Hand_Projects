import { View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    list: { gap: 12 },
    card: {
      height: 160,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
      opacity: 0.7,
    },
  };
}

export function OrderListSkeleton() {
  const styles = useThemedStyles(createStyles);
  return (
    <View style={styles.list}>
      <View style={styles.card} />
      <View style={styles.card} />
      <View style={styles.card} />
      <View style={styles.card} />
    </View>
  );
}
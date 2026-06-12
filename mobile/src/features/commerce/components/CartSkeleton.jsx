import { View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    container: {
      gap: 16,
    },
    row: {
      flexDirection: "row",
      gap: 12,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
    },
    image: {
      width: 96,
      height: 96,
      borderRadius: 12,
      backgroundColor: colors.surfaceContainerLow,
    },
    body: {
      flex: 1,
      gap: 10,
    },
    lineLg: {
      height: 16,
      width: "70%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    lineMd: {
      height: 12,
      width: "40%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    footer: {
      marginTop: "auto",
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "center",
    },
    stepper: {
      width: 112,
      height: 32,
      borderRadius: 999,
      backgroundColor: colors.surfaceContainerLow,
    },
    price: {
      width: 80,
      height: 20,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    summary: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 12,
    },
    summaryTitle: {
      height: 20,
      width: "50%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    summaryLine: {
      height: 14,
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    summaryButton: {
      marginTop: 8,
      height: 48,
      borderRadius: 12,
      backgroundColor: colors.surfaceContainerLow,
    },
  };
}

function CartItemSkeleton({ styles }) {
  return (
    <View style={styles.row}>
      <View style={styles.image} />
      <View style={styles.body}>
        <View style={styles.lineLg} />
        <View style={styles.lineMd} />
        <View style={styles.footer}>
          <View style={styles.stepper} />
          <View style={styles.price} />
        </View>
      </View>
    </View>
  );
}

export function CartSkeleton() {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.container}>
      {[1, 2, 3].map((key) => (
        <CartItemSkeleton key={key} styles={styles} />
      ))}
      <View style={styles.summary}>
        <View style={styles.summaryTitle} />
        <View style={styles.summaryLine} />
        <View style={styles.summaryLine} />
        <View style={styles.summaryButton} />
      </View>
    </View>
  );
}

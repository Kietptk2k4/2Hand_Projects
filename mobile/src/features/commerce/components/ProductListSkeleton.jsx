import { View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    grid: {
      flexDirection: "row",
      flexWrap: "wrap",
      gap: 12,
    },
    card: {
      width: "48%",
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      overflow: "hidden",
    },
    image: {
      height: 160,
      backgroundColor: colors.surfaceContainerLow,
    },
    body: {
      padding: 12,
      gap: 8,
    },
    lineLg: {
      height: 16,
      width: "75%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    lineMd: {
      height: 12,
      width: "50%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    lineSm: {
      height: 12,
      width: "66%",
      borderRadius: 4,
      backgroundColor: colors.surfaceContainerLow,
    },
    button: {
      marginTop: 8,
      height: 32,
      borderRadius: 8,
      backgroundColor: colors.surfaceContainerLow,
    },
  };
}

export function ProductListSkeleton({ count = 6 }) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.grid}>
      {Array.from({ length: count }, (_, index) => (
        <View key={index} style={styles.card}>
          <View style={styles.image} />
          <View style={styles.body}>
            <View style={styles.lineLg} />
            <View style={styles.lineMd} />
            <View style={styles.lineSm} />
            <View style={styles.button} />
          </View>
        </View>
      ))}
    </View>
  );
}

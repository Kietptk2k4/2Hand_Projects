import { Text, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    container: {
      flex: 1,
      alignItems: "center",
      justifyContent: "center",
      padding: 24,
      backgroundColor: colors.surface,
    },
    title: {
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
      marginBottom: 8,
      textAlign: "center",
    },
    subtitle: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
      lineHeight: 20,
    },
  };
}

export function CommercePlaceholderScreen({
  title,
  message = "Màn hình sẽ được triển khai ở phase tiếp theo.",
}) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.subtitle}>{message}</Text>
    </View>
  );
}

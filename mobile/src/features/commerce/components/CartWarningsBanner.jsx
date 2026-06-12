import { Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    banner: {
      marginBottom: 16,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.primary}4D`,
      backgroundColor: colors.surfaceContainerLow,
      padding: 16,
      gap: 8,
    },
    row: {
      flexDirection: "row",
      alignItems: "flex-start",
      gap: 8,
    },
    text: {
      flex: 1,
      fontSize: 14,
      color: colors.onSurface,
      lineHeight: 20,
    },
  };
}

export function CartWarningsBanner({ warnings = [] }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!warnings.length) return null;

  return (
    <View style={styles.banner} accessibilityRole="alert">
      {warnings.map((message) => (
        <View key={message} style={styles.row}>
          <Ionicons name="information-circle-outline" size={18} color={colors.primary} />
          <Text style={styles.text}>{message}</Text>
        </View>
      ))}
    </View>
  );
}

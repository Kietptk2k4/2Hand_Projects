import { Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    banner: {
      flexDirection: "row",
      alignItems: "flex-start",
      gap: 12,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.primary,
      backgroundColor: colors.surfaceContainerLow,
      padding: 14,
      marginBottom: 16,
    },
    title: {
      fontSize: 15,
      fontWeight: "600",
      color: colors.onSurface,
    },
    message: {
      marginTop: 4,
      fontSize: 14,
      lineHeight: 20,
      color: colors.onSurfaceVariant,
    },
    body: {
      flex: 1,
    },
  };
}

export function ShopVacationBanner({ message }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!message) return null;

  return (
    <View style={styles.banner} accessibilityRole="text">
      <Ionicons name="airplane-outline" size={22} color={colors.primary} />
      <View style={styles.body}>
        <Text style={styles.title}>Cửa hàng đang trong kỳ nghỉ</Text>
        <Text style={styles.message}>{message}</Text>
      </View>
    </View>
  );
}

import { Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { getCartValidateReasonLabel } from "../constants/cartConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    banner: {
      marginBottom: 16,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.error}40`,
      backgroundColor: colors.errorContainer,
      padding: 16,
      gap: 8,
    },
    title: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onSurface,
    },
    row: {
      flexDirection: "row",
      alignItems: "flex-start",
      gap: 8,
    },
    text: {
      flex: 1,
      fontSize: 14,
      color: colors.onSurfaceVariant,
      lineHeight: 20,
    },
    productName: {
      fontWeight: "600",
      color: colors.onSurface,
    },
  };
}

export function CartInvalidItemsBanner({ items = [] }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const invalid = items.filter((item) => item.validateMessage || item.unavailableReason);

  if (!invalid.length) return null;

  return (
    <View style={styles.banner} accessibilityRole="alert">
      <Text style={styles.title}>Một số sản phẩm không thể thanh toán:</Text>
      {invalid.map((item) => (
        <View key={item.cartItemId} style={styles.row}>
          <Ionicons name="alert-circle" size={18} color={colors.error} />
          <Text style={styles.text}>
            <Text style={styles.productName}>{item.productName}</Text>
            {" — "}
            {item.validateMessage || getCartValidateReasonLabel(item.unavailableReason)}
          </Text>
        </View>
      ))}
    </View>
  );
}

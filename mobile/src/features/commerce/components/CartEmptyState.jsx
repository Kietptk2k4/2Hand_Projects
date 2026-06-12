import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 32,
      alignItems: "center",
      gap: 8,
    },
    title: {
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
      marginTop: 8,
    },
    subtitle: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
      lineHeight: 20,
    },
    button: {
      marginTop: 16,
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingHorizontal: 24,
      paddingVertical: 12,
    },
    buttonText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onPrimary,
    },
  };
}

export function CartEmptyState() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.card}>
      <Ionicons name="cart-outline" size={48} color={colors.outline} />
      <Text style={styles.title}>Giỏ hàng trống</Text>
      <Text style={styles.subtitle}>
        Bạn chưa có sản phẩm nào. Khám phá marketplace và thêm vào giỏ nhé.
      </Text>
      <Pressable style={styles.button} onPress={() => router.push(ROUTES.commerceHome)}>
        <Text style={styles.buttonText}>Tiếp tục mua sắm</Text>
      </Pressable>
    </View>
  );
}

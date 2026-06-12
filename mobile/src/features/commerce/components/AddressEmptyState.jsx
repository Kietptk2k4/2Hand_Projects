import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    container: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 32,
      alignItems: "center",
      gap: 12,
    },
    text: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center" },
    button: {
      marginTop: 8,
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingHorizontal: 16,
      paddingVertical: 12,
    },
    buttonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
  };
}

export function AddressEmptyState({ onAdd }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.container}>
      <Ionicons name="location-outline" size={40} color={colors.outline} />
      <Text style={styles.text}>Bạn chưa có địa chỉ giao hàng.</Text>
      <Pressable style={styles.button} onPress={onAdd}>
        <Text style={styles.buttonText}>Thêm địa chỉ đầu tiên</Text>
      </Pressable>
    </View>
  );
}
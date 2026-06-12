import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { Pressable, StyleSheet } from "react-native";
import { ROUTES } from "../../../../shared/constants/routes";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";

export function AccountSettingsHeaderButton() {
  const colors = useThemeColors();

  return (
    <Pressable
      onPress={() => router.push(ROUTES.account)}
      accessibilityRole="button"
      accessibilityLabel="Cài đặt tài khoản"
      hitSlop={8}
      style={styles.button}
    >
      <Ionicons name="settings-outline" size={22} color={colors.onSurface} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  button: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginRight: 4,
  },
});

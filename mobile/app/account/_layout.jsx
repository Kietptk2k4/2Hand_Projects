import { Stack } from "expo-router";
import { useThemeColors } from "../../src/shared/theme/useThemeColors";

export default function AccountLayout() {
  const colors = useThemeColors();

  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: colors.surface },
        headerTintColor: colors.onSurface,
        headerTitleStyle: { fontWeight: "600" },
        contentStyle: { backgroundColor: colors.surface },
      }}
    >
      <Stack.Screen name="index" options={{ title: "Tài khoản" }} />
      <Stack.Screen name="info" options={{ title: "Thông tin tài khoản" }} />
      <Stack.Screen name="edit" options={{ title: "Chỉnh sửa hồ sơ" }} />
      <Stack.Screen name="avatar" options={{ title: "Ảnh đại diện" }} />
      <Stack.Screen name="privacy" options={{ title: "Quyền riêng tư" }} />
      <Stack.Screen name="settings" options={{ title: "Cài đặt" }} />
      <Stack.Screen name="password" options={{ title: "Đổi mật khẩu" }} />
      <Stack.Screen name="security" options={{ title: "Bảo mật tài khoản" }} />
      <Stack.Screen name="delete" options={{ title: "Xóa tài khoản" }} />
    </Stack>
  );
}

import { Stack } from "expo-router";
import { useThemeColors } from "../../src/shared/theme/useThemeColors";

export default function AuthLayout() {
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
      <Stack.Screen name="login" options={{ title: "Dang nhap" }} />
      <Stack.Screen name="register" options={{ title: "Dang ky" }} />
      <Stack.Screen name="forgot-password" options={{ title: "Quen mat khau" }} />
      <Stack.Screen name="verify-email" options={{ title: "Xac thuc email" }} />
      <Stack.Screen name="session-expired" options={{ title: "Phien het han", headerBackVisible: false }} />
      <Stack.Screen name="change-password" options={{ title: "Doi mat khau", headerShown: false }} />
    </Stack>
  );
}

import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { colors } from "../src/shared/theme/colors";

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <StatusBar style="dark" />
      <Stack
        screenOptions={{
          headerStyle: { backgroundColor: colors.surface },
          headerTintColor: colors.onSurface,
          contentStyle: { backgroundColor: colors.surface },
        }}
      >
        <Stack.Screen name="index" options={{ title: "2Hands" }} />
        <Stack.Screen name="(auth)/login" options={{ title: "Dang nhap" }} />
      </Stack>
    </SafeAreaProvider>
  );
}

import { useEffect } from "react";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { QueryClientProvider } from "@tanstack/react-query";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { router } from "expo-router";
import { configureAuthRefreshService } from "../src/services/http/authRefreshService";
import { queryClient } from "../src/services/query/queryClient";
import { ROUTES } from "../src/shared/constants/routes";
import { colors } from "../src/shared/theme/colors";

export default function RootLayout() {
  useEffect(() => {
    configureAuthRefreshService({
      onSessionExpired: () => {
        router.replace(ROUTES.login);
      },
    });
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <SafeAreaProvider>
        <StatusBar style="dark" />
        <Stack
          screenOptions={{
            headerStyle: { backgroundColor: colors.surface },
            headerTintColor: colors.onSurface,
            contentStyle: { backgroundColor: colors.surface },
          }}
        >
          <Stack.Screen name="index" options={{ headerShown: false }} />
          <Stack.Screen name="(auth)/login" options={{ title: "Đăng nhập" }} />
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen
            name="post/[postId]/index"
            options={{ title: "Bài viết" }}
          />
          <Stack.Screen name="user/[userId]/index" options={{ title: "Hồ sơ" }} />
          <Stack.Screen name="tags/[hashtag]" options={{ title: "Hashtag" }} />
          <Stack.Screen name="saved" options={{ title: "Đã lưu" }} />
          <Stack.Screen name="search" options={{ title: "Tìm kiếm" }} />
          <Stack.Screen name="suggestions" options={{ title: "Gợi ý" }} />
        </Stack>
      </SafeAreaProvider>
    </QueryClientProvider>
  );
}

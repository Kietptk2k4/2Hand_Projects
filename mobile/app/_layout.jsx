import { useEffect } from "react";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { QueryClientProvider } from "@tanstack/react-query";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { router } from "expo-router";
import { configureAuthRefreshService } from "../src/services/http/authRefreshService";
import { queryClient } from "../src/services/query/queryClient";
import { SocialToastProvider } from "../src/shared/components/SocialToastProvider";
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
      <SocialToastProvider>
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
          <Stack.Screen
            name="post/create/index"
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="post/[postId]/edit/index"
            options={{ headerShown: false }}
          />
          <Stack.Screen name="profile/[userId]/index" options={{ title: "Hồ sơ" }} />
          <Stack.Screen
            name="profile/[userId]/followers"
            options={{ title: "Người theo dõi" }}
          />
          <Stack.Screen
            name="profile/[userId]/following"
            options={{ title: "Đang theo dõi" }}
          />
          <Stack.Screen name="user/[userId]/index" options={{ headerShown: false }} />
          <Stack.Screen name="hashtag/[hashtag]/index" options={{ title: "Hashtag" }} />
          <Stack.Screen name="tags/[hashtag]" options={{ headerShown: false }} />
          <Stack.Screen name="saved/index" options={{ title: "Đã lưu" }} />
          <Stack.Screen name="search/index" options={{ title: "Tìm kiếm" }} />
          <Stack.Screen name="suggestions/index" options={{ title: "Gợi ý theo dõi" }} />
        </Stack>
      </SafeAreaProvider>
      </SocialToastProvider>
    </QueryClientProvider>
  );
}

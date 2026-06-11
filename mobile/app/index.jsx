import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { router } from "expo-router";
import { getAccessToken, getRefreshToken, clearSessionTokens } from "../src/services/auth/tokenStorage";
import { logoutWithRefreshToken } from "../src/features/auth/api/authApi";
import { colors } from "../src/shared/theme/colors";

export default function HomeScreen() {
  const [isLoading, setIsLoading] = useState(true);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const checkSession = useCallback(async () => {
    const token = await getAccessToken();
    if (!token) {
      router.replace("/(auth)/login");
      return;
    }
    setIsLoggedIn(true);
    setIsLoading(false);
  }, []);

  useEffect(() => {
    checkSession();
  }, [checkSession]);

  const handleLogout = async () => {
    if (isLoggingOut) return;
    setIsLoggingOut(true);
    try {
      const refreshToken = await getRefreshToken();
      if (refreshToken) {
        await logoutWithRefreshToken(refreshToken);
      }
    } catch {
      // Best-effort logout; clear local session regardless.
    } finally {
      await clearSessionTokens();
      setIsLoggingOut(false);
      router.replace("/(auth)/login");
    }
  };

  if (isLoading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (!isLoggedIn) {
    return null;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Xin chao!</Text>
      <Text style={styles.subtitle}>
        Ban da dang nhap thanh cong. Man hinh Commerce/Social se duoc them o buoc tiep theo.
      </Text>
      <Pressable
        style={[styles.button, isLoggingOut && styles.buttonDisabled]}
        onPress={handleLogout}
        disabled={isLoggingOut}
      >
        <Text style={styles.buttonText}>
          {isLoggingOut ? "Dang dang xuat..." : "Dang xuat"}
        </Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  centered: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.surface,
  },
  container: {
    flex: 1,
    padding: 24,
    backgroundColor: colors.surface,
    justifyContent: "center",
  },
  title: {
    fontSize: 24,
    fontWeight: "700",
    color: colors.onSurface,
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 15,
    lineHeight: 22,
    color: colors.onSurfaceVariant,
    marginBottom: 32,
  },
  button: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    minHeight: 48,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16,
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  buttonText: {
    color: colors.onPrimary,
    fontSize: 16,
    fontWeight: "600",
  },
});

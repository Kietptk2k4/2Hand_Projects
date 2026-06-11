import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { router } from "expo-router";
import { logoutWithRefreshToken } from "../../src/features/auth/api/authApi";
import {
  clearSessionTokens,
  getRefreshToken,
} from "../../src/services/auth/tokenStorage";
import { ROUTES } from "../../src/shared/constants/routes";
import { colors } from "../../src/shared/theme/colors";

export default function ProfileTabScreen() {
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    setIsReady(true);
  }, []);

  const handleLogout = useCallback(async () => {
    if (isLoggingOut) return;
    setIsLoggingOut(true);
    try {
      const refreshToken = await getRefreshToken();
      if (refreshToken) {
        await logoutWithRefreshToken(refreshToken);
      }
    } catch {
      // Best-effort logout
    } finally {
      await clearSessionTokens();
      setIsLoggingOut(false);
      router.replace(ROUTES.login);
    }
  }, [isLoggingOut]);

  if (!isReady) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Ho so</Text>
      <Text style={styles.subtitle}>
        Man hinh profile day du se duoc them o Phase 6.
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

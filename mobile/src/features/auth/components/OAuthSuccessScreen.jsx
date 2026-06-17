import { router, useLocalSearchParams } from "expo-router";
import { useEffect, useState } from "react";
import { ActivityIndicator, StyleSheet, Text, View } from "react-native";
import { fetchOAuthSession } from "../api/authApi";
import { AuthLinkButton } from "./AuthButtons";
import { GENERIC_ERROR_RETRY } from "../constants/authUiStrings";
import { ROUTES } from "../../../shared/constants/routes";
import { setSessionTokens } from "../../../services/auth/tokenStorage";
import { setVerifyEmailAddress } from "../utils/authNavigationState";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

export function OAuthSuccessScreen() {
  const colors = useThemeColors();
  const params = useLocalSearchParams();
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const status = Array.isArray(params.status) ? params.status[0] : params.status;

    if (status !== "success") {
      router.replace({
        pathname: ROUTES.oauthFailure,
        params: { status: "error", code: "AUTH-401-OAUTH-SESSION-INVALID" },
      });
      return;
    }

    let cancelled = false;

    (async () => {
      try {
        const sessionData = await fetchOAuthSession();
        if (cancelled) return;

        await setSessionTokens({
          accessToken: sessionData.access_token,
          refreshToken: sessionData.refresh_token,
        });

        const user = sessionData.user || {};
        const isPendingVerification = user.status === "PENDING_VERIFICATION";

        if (isPendingVerification) {
          setVerifyEmailAddress(user.email || "");
          router.replace(ROUTES.verifyEmail);
          return;
        }

        router.replace(ROUTES.feed);
      } catch (error) {
        if (cancelled) return;
        setErrorMessage(error?.message || GENERIC_ERROR_RETRY);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [params.status]);

  if (errorMessage) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.surface }]}>
        <Text style={[styles.error, { color: colors.error }]}>{errorMessage}</Text>
        <AuthLinkButton label="Ve trang dang nhap" onPress={() => router.replace(ROUTES.login)} />
      </View>
    );
  }

  return (
    <View style={[styles.centered, { backgroundColor: colors.surface }]}>
      <ActivityIndicator size="large" color={colors.primary} />
      <Text style={[styles.loading, { color: colors.onSurfaceVariant }]}>
        Dang hoan tat dang nhap...
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  centered: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
    gap: 16,
  },
  loading: { fontSize: 16 },
  error: { fontSize: 16, textAlign: "center" },
});

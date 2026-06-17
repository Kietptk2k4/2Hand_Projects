import { router, useLocalSearchParams } from "expo-router";
import { useMemo } from "react";
import { StyleSheet, Text, View } from "react-native";
import { mapOAuthFailureMessage } from "../constants/oauthConstants";
import { AuthLinkButton } from "./AuthButtons";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

export function OAuthFailureScreen() {
  const colors = useThemeColors();
  const params = useLocalSearchParams();

  const message = useMemo(() => {
    const code = Array.isArray(params.code) ? params.code[0] : params.code;
    return mapOAuthFailureMessage(code);
  }, [params.code]);

  return (
    <View style={[styles.centered, { backgroundColor: colors.surface }]}>
      <Text style={styles.icon}>!</Text>
      <Text style={[styles.title, { color: colors.onSurface }]}>Dang nhap that bai</Text>
      <Text style={[styles.message, { color: colors.onSurfaceVariant }]}>{message}</Text>
      <AuthLinkButton label="Thu dang nhap lai" onPress={() => router.replace(ROUTES.login)} />
    </View>
  );
}

const styles = StyleSheet.create({
  centered: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
    gap: 12,
  },
  icon: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: "#FFEDEA",
    color: "#b3261e",
    textAlign: "center",
    lineHeight: 56,
    fontSize: 28,
    fontWeight: "700",
    overflow: "hidden",
  },
  title: { fontSize: 22, fontWeight: "700" },
  message: { fontSize: 16, textAlign: "center", lineHeight: 22 },
});

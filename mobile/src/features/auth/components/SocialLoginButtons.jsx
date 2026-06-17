import * as Linking from "expo-linking";
import * as WebBrowser from "expo-web-browser";
import { useCallback, useState } from "react";
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from "react-native";
import { getOAuthRedirectUrl } from "../api/authApi";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

const SOCIAL_BUTTONS = [
  { key: "google", label: "Tiep tuc voi Google", icon: "G" },
  { key: "facebook", label: "Tiep tuc voi Facebook", icon: "f" },
];

WebBrowser.maybeCompleteAuthSession();

function resolveOAuthReturnUrl() {
  return (
    process.env.EXPO_PUBLIC_OAUTH_SUCCESS_REDIRECT || Linking.createURL("oauth/success")
  );
}

export function SocialLoginButtons({ disabled = false, onRedirectStart, onRedirectEnd }) {
  const colors = useThemeColors();
  const [isRedirecting, setIsRedirecting] = useState(false);

  const styles = StyleSheet.create({
    container: { gap: 12 },
    dividerRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      marginVertical: 8,
    },
    divider: { flex: 1, height: 1, backgroundColor: colors.outlineVariant },
    dividerText: {
      fontSize: 12,
      fontWeight: "600",
      color: colors.onSurfaceVariant,
      textTransform: "uppercase",
    },
    button: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "center",
      gap: 8,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      borderRadius: 8,
      minHeight: 48,
      paddingHorizontal: 16,
      backgroundColor: colors.surfaceContainerLowest,
      opacity: disabled || isRedirecting ? 0.7 : 1,
    },
    icon: {
      width: 24,
      height: 24,
      borderRadius: 12,
      alignItems: "center",
      justifyContent: "center",
    },
    iconText: { fontSize: 14, fontWeight: "700", color: colors.primary },
    label: { fontSize: 14, fontWeight: "500", color: colors.onSurface },
    overlay: {
      ...StyleSheet.absoluteFillObject,
      alignItems: "center",
      justifyContent: "center",
      backgroundColor: "rgba(255,255,255,0.85)",
      borderRadius: 16,
    },
    overlayText: {
      fontSize: 14,
      color: colors.onSurface,
      paddingHorizontal: 16,
      textAlign: "center",
    },
  });

  const onSocialPress = useCallback(
    async (provider) => {
      const authUrl = getOAuthRedirectUrl(provider);
      if (!authUrl || disabled || isRedirecting) return;

      setIsRedirecting(true);
      onRedirectStart?.();

      try {
        await WebBrowser.openAuthSessionAsync(authUrl, resolveOAuthReturnUrl());
      } finally {
        setIsRedirecting(false);
        onRedirectEnd?.();
      }
    },
    [disabled, isRedirecting, onRedirectEnd, onRedirectStart]
  );

  return (
    <View>
      <View style={styles.dividerRow}>
        <View style={styles.divider} />
        <Text style={styles.dividerText}>Hoac tiep tuc voi</Text>
        <View style={styles.divider} />
      </View>

      <View style={styles.container}>
        {SOCIAL_BUTTONS.map((item) => (
          <Pressable
            key={item.key}
            style={styles.button}
            onPress={() => onSocialPress(item.key)}
            disabled={disabled || isRedirecting}
          >
            <View style={styles.icon}>
              <Text style={styles.iconText}>{item.icon}</Text>
            </View>
            <Text style={styles.label}>{item.label}</Text>
          </Pressable>
        ))}
      </View>

      {isRedirecting ? (
        <View style={styles.overlay} pointerEvents="none">
          <ActivityIndicator color={colors.primary} />
          <Text style={styles.overlayText}>Dang chuyen huong den nha cung cap dang nhap...</Text>
        </View>
      ) : null}
    </View>
  );
}

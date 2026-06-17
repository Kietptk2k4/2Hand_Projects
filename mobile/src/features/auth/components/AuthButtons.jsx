import { ActivityIndicator, Pressable, StyleSheet, Text } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

export function AuthPrimaryButton({ label, loadingLabel, onPress, disabled, loading }) {
  const colors = useThemeColors();

  const styles = StyleSheet.create({
    button: {
      marginTop: 8,
      backgroundColor: colors.primary,
      borderRadius: 8,
      minHeight: 48,
      alignItems: "center",
      justifyContent: "center",
      opacity: disabled ? 0.8 : 1,
    },
    text: {
      color: colors.onPrimary,
      fontSize: 16,
      fontWeight: "600",
    },
  });

  return (
    <Pressable style={styles.button} onPress={onPress} disabled={disabled || loading}>
      {loading ? (
        <ActivityIndicator color={colors.onPrimary} />
      ) : (
        <Text style={styles.text}>{label}</Text>
      )}
    </Pressable>
  );
}

export function AuthLinkButton({ label, onPress, align = "center" }) {
  const colors = useThemeColors();

  return (
    <Pressable onPress={onPress} style={{ alignSelf: align === "left" ? "flex-start" : "center" }}>
      <Text style={{ fontSize: 14, fontWeight: "600", color: colors.primary }}>{label}</Text>
    </Pressable>
  );
}

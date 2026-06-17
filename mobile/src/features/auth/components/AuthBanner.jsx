import { StyleSheet, Text, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

export function AuthBanner({ message, variant = "error", title }) {
  const colors = useThemeColors();
  const isError = variant === "error";
  const isInfo = variant === "info";

  const styles = StyleSheet.create({
    banner: {
      borderRadius: 8,
      padding: 12,
      marginBottom: 16,
      backgroundColor: isError ? "#FFEDEA" : isInfo ? colors.primaryContainer : colors.surfaceContainer,
      borderWidth: 1,
      borderColor: isError ? colors.error : colors.primary,
    },
    title: {
      fontSize: 14,
      fontWeight: "600",
      color: isError ? colors.error : colors.onSurface,
      marginBottom: 4,
    },
    text: {
      fontSize: 14,
      lineHeight: 20,
      color: isError ? colors.error : colors.onSurfaceVariant,
    },
  });

  return (
    <View style={styles.banner}>
      {title ? <Text style={styles.title}>{title}</Text> : null}
      <Text style={styles.text}>{message}</Text>
    </View>
  );
}
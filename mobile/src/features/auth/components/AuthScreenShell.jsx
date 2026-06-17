import {
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

export function AuthScreenShell({ title, subtitle, children, footer }) {
  const colors = useThemeColors();

  const styles = StyleSheet.create({
    flex: { flex: 1, backgroundColor: colors.surface },
    scrollContent: {
      flexGrow: 1,
      justifyContent: "center",
      padding: 24,
    },
    card: {
      backgroundColor: colors.surfaceContainerLowest,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      padding: 24,
    },
    brand: {
      fontSize: 14,
      fontWeight: "700",
      color: colors.primary,
      marginBottom: 8,
    },
    heading: {
      fontSize: 24,
      fontWeight: "700",
      color: colors.onSurface,
      marginBottom: 8,
    },
    description: {
      fontSize: 14,
      lineHeight: 20,
      color: colors.onSurfaceVariant,
      marginBottom: 24,
    },
    footer: {
      marginTop: 24,
      alignItems: "center",
    },
  });

  return (
    <KeyboardAvoidingView
      style={styles.flex}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.card}>
          <Text style={styles.brand}>2Hands</Text>
          {title ? <Text style={styles.heading}>{title}</Text> : null}
          {subtitle ? <Text style={styles.description}>{subtitle}</Text> : null}
          {children}
        </View>
        {footer ? <View style={styles.footer}>{footer}</View> : null}
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

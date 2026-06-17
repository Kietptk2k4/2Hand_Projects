import { Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { HIDE_PASSWORD, SHOW_PASSWORD } from "../constants/authUiStrings";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

export function AuthTextField({
  label, value, onChangeText, onBlur, error, placeholder, secureTextEntry,
  keyboardType, autoCapitalize = "none", textContentType, editable = true,
  maxLength, showToggle, isPasswordVisible, onTogglePassword, textAlign = "left",
}) {
  const colors = useThemeColors();
  const styles = StyleSheet.create({
    field: { marginBottom: 16 },
    label: { fontSize: 14, fontWeight: "600", color: colors.onSurface, marginBottom: 8 },
    input: {
      borderWidth: 1, borderColor: colors.outlineVariant, borderRadius: 8, minHeight: 48,
      paddingHorizontal: 12, fontSize: 16, color: colors.onSurface,
      backgroundColor: colors.surfaceContainerLowest,
    },
    inputError: { borderColor: colors.error },
    passwordInput: { paddingRight: 64 },
    togglePassword: { position: "absolute", right: 12, top: 0, bottom: 0, justifyContent: "center" },
    togglePasswordText: { color: colors.primary, fontSize: 14, fontWeight: "600" },
    fieldError: { marginTop: 6, fontSize: 12, color: colors.error },
  });

  return (
    <View style={styles.field}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      <View style={showToggle ? { position: "relative" } : undefined}>
        <TextInput
          style={[styles.input, showToggle ? styles.passwordInput : null, error ? styles.inputError : null,
            textAlign === "center" ? { letterSpacing: 8, fontSize: 18 } : null]}
          value={value} onChangeText={onChangeText} onBlur={onBlur}
          autoCapitalize={autoCapitalize} autoCorrect={false} keyboardType={keyboardType}
          textContentType={textContentType} placeholder={placeholder}
          placeholderTextColor={colors.onSurfaceVariant} secureTextEntry={secureTextEntry}
          editable={editable} maxLength={maxLength} textAlign={textAlign}
        />
        {showToggle ? (
          <Pressable style={styles.togglePassword} onPress={onTogglePassword}>
            <Text style={styles.togglePasswordText}>{isPasswordVisible ? HIDE_PASSWORD : SHOW_PASSWORD}</Text>
          </Pressable>
        ) : null}
      </View>
      {error ? <Text style={styles.fieldError}>{error}</Text> : null}
    </View>
  );
}
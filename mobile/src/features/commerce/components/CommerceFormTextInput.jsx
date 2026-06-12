import { Text, TextInput, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    field: { gap: 4 },
    label: { fontSize: 14, fontWeight: "500", color: colors.onSurface },
    input: {
      minHeight: 48,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      borderRadius: 12,
      paddingHorizontal: 12,
      paddingVertical: 12,
      fontSize: 16,
      color: colors.onSurface,
      backgroundColor: colors.surfaceContainerLowest,
    },
    inputMultiline: { minHeight: 96, textAlignVertical: "top" },
    inputError: { borderColor: colors.error },
    errorText: { fontSize: 12, color: colors.error },
  };
}

export function CommerceFormTextInput({
  label,
  value,
  onChangeText,
  placeholder,
  error,
  multiline = false,
  keyboardType = "default",
  autoCapitalize = "sentences",
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.field}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.outline}
        multiline={multiline}
        keyboardType={keyboardType}
        autoCapitalize={autoCapitalize}
        style={[styles.input, multiline && styles.inputMultiline, error ? styles.inputError : null]}
      />
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
    </View>
  );
}
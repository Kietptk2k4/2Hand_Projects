import { StyleSheet, Text, TextInput, View } from "react-native";
import { colors } from "../../../../shared/theme/colors";

export function AccountTextInput({
  value,
  onChangeText,
  placeholder,
  error,
  multiline = false,
  numberOfLines = 1,
  maxLength,
  keyboardType = "default",
  autoCapitalize = "sentences",
}) {
  return (
    <View>
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.outline}
        multiline={multiline}
        numberOfLines={numberOfLines}
        maxLength={maxLength}
        keyboardType={keyboardType}
        autoCapitalize={autoCapitalize}
        style={[
          styles.input,
          multiline && styles.inputMultiline,
          error ? styles.inputError : null,
        ]}
      />
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  input: {
    minHeight: 48,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 12,
    fontSize: 16,
    color: colors.onSurface,
    backgroundColor: colors.surfaceContainerLowest,
  },
  inputMultiline: {
    minHeight: 112,
    textAlignVertical: "top",
  },
  inputError: {
    borderColor: colors.error,
  },
  errorText: {
    marginTop: 4,
    fontSize: 12,
    color: colors.error,
  },
});
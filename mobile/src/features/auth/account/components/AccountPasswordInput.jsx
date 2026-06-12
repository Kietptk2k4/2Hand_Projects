import { Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { colors } from "../../../../shared/theme/colors";

export function AccountPasswordInput({
  value,
  onChangeText,
  placeholder = "Nhập mật khẩu hiện tại",
  error,
  isVisible,
  onToggleVisibility,
}) {
  return (
    <View>
      <View style={styles.row}>
        <TextInput
          value={value}
          onChangeText={onChangeText}
          placeholder={placeholder}
          placeholderTextColor={colors.outline}
          secureTextEntry={!isVisible}
          textContentType="password"
          autoCapitalize="none"
          autoCorrect={false}
          style={[styles.input, error ? styles.inputError : null]}
        />
        <Pressable
          onPress={onToggleVisibility}
          style={styles.toggle}
          accessibilityRole="button"
          accessibilityLabel={isVisible ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
        >
          <Text style={styles.toggleText}>{isVisible ? "Ẩn" : "Hiện"}</Text>
        </Pressable>
      </View>
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    position: "relative",
  },
  input: {
    minHeight: 48,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingRight: 64,
    paddingVertical: 12,
    fontSize: 16,
    color: colors.onSurface,
    backgroundColor: colors.surfaceContainerLowest,
  },
  inputError: {
    borderColor: colors.error,
  },
  toggle: {
    position: "absolute",
    right: 12,
    top: 0,
    bottom: 0,
    justifyContent: "center",
  },
  toggleText: {
    color: colors.primary,
    fontSize: 14,
    fontWeight: "600",
  },
  errorText: {
    marginTop: 4,
    fontSize: 12,
    color: colors.error,
  },
});
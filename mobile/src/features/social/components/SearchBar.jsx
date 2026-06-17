import { Pressable, StyleSheet, TextInput, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { colors } from "../../../shared/theme/colors";

export function SearchBar({
  value,
  onChangeText,
  onClear,
  placeholder = "Tìm kiếm...",
  autoFocus = false,
  onSubmitEditing,
}) {
  return (
    <View style={styles.wrap}>
      <Ionicons name="search" size={20} color={colors.onSurfaceVariant} style={styles.icon} />
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.outline}
        style={styles.input}
        autoFocus={autoFocus}
        autoCapitalize="none"
        autoCorrect={false}
        returnKeyType="search"
        onSubmitEditing={onSubmitEditing}
      />
      {value ? (
        <Pressable onPress={onClear} style={styles.clearBtn} accessibilityLabel="Xóa tìm kiếm">
          <Ionicons name="close-circle" size={20} color={colors.outline} />
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    flexDirection: "row",
    alignItems: "center",
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 10,
    backgroundColor: colors.surfaceContainerLowest,
    paddingHorizontal: 12,
    minHeight: 44,
  },
  icon: {
    marginRight: 8,
  },
  input: {
    flex: 1,
    fontSize: 15,
    color: colors.onSurface,
    paddingVertical: 8,
  },
  clearBtn: {
    marginLeft: 8,
    padding: 4,
  },
});

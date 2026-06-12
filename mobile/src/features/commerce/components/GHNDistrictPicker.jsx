import { useState } from "react";
import { ActivityIndicator, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { GhnPickerModal } from "./GhnPickerModal";

function createStyles(colors) {
  return {
    field: { gap: 4 },
    label: { fontSize: 14, fontWeight: "500", color: colors.onSurface },
    trigger: {
      minHeight: 48,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      borderRadius: 12,
      paddingHorizontal: 12,
      paddingVertical: 12,
      backgroundColor: colors.surfaceContainerLowest,
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 8,
    },
    triggerDisabled: { opacity: 0.6 },
    triggerText: { flex: 1, fontSize: 16, color: colors.onSurface },
    placeholder: { color: colors.outline },
    errorText: { fontSize: 12, color: colors.error },
  };
}

export function GHNDistrictPicker({
  value,
  options,
  isLoading,
  disabled,
  error,
  provinceSelected,
  onChange,
}) {
  const [open, setOpen] = useState(false);
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const selected = options.find((item) => item.value === value);
  const placeholder = !provinceSelected
    ? "Chọn tỉnh/thành trước"
    : isLoading
      ? "Đang tải..."
      : "Chọn quận/huyện";
  const label = selected?.label || placeholder;

  return (
    <View style={styles.field}>
      <Text style={styles.label}>Quận/Huyện</Text>
      <Pressable
        style={[styles.trigger, disabled ? styles.triggerDisabled : null]}
        disabled={disabled || isLoading || !provinceSelected}
        onPress={() => setOpen(true)}
      >
        <Text style={[styles.triggerText, !selected ? styles.placeholder : null]}>{label}</Text>
        {isLoading ? (
          <ActivityIndicator size="small" color={colors.primary} />
        ) : (
          <Ionicons name="chevron-down" size={18} color={colors.onSurfaceVariant} />
        )}
      </Pressable>
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
      <GhnPickerModal
        visible={open}
        title="Chọn quận/huyện"
        options={options}
        selectedValue={value}
        isLoading={isLoading}
        onSelect={onChange}
        onClose={() => setOpen(false)}
      />
    </View>
  );
}
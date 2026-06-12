import { Ionicons } from "@expo/vector-icons";
import { useMemo } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";

export function AccountMenuRow({
  label,
  icon,
  danger = false,
  showChevron = true,
  onPress,
}) {
  const colors = useThemeColors();
  const labelColor = danger ? colors.error : colors.onSurface;
  const iconColor = danger ? colors.error : colors.onSurfaceVariant;

  const styles = useMemo(
    () =>
      StyleSheet.create({
        row: {
          flexDirection: "row",
          alignItems: "center",
          minHeight: 56,
          paddingHorizontal: 16,
          paddingVertical: 12,
          gap: 12,
        },
        rowPressed: {
          backgroundColor: colors.surfaceContainerLow,
        },
        iconWrap: {
          width: 40,
          height: 40,
          borderRadius: 20,
          alignItems: "center",
          justifyContent: "center",
          backgroundColor: colors.surfaceContainerLow,
        },
        iconWrapDanger: {
          backgroundColor: colors.errorContainer,
        },
        label: {
          flex: 1,
          fontSize: 16,
          fontWeight: "500",
        },
      }),
    [colors]
  );

  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [styles.row, pressed && styles.rowPressed]}
      accessibilityRole="button"
      accessibilityLabel={label}
    >
      <View style={[styles.iconWrap, danger && styles.iconWrapDanger]}>
        <Ionicons name={icon} size={22} color={iconColor} />
      </View>
      <Text style={[styles.label, { color: labelColor }]}>{label}</Text>
      {showChevron ? (
        <Ionicons name="chevron-forward" size={20} color={colors.outline} />
      ) : null}
    </Pressable>
  );
}

import { useMemo } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";
import { normalizeAppearanceMode } from "../../utils/appearanceTheme";
import { THEME_OPTIONS } from "../constants/appearanceOptions";

export function AppearanceModePicker({ value, onChange, disabled = false }) {
  const colors = useThemeColors();
  const normalizedValue = normalizeAppearanceMode(value);

  const styles = useMemo(
    () =>
      StyleSheet.create({
        list: {
          gap: 12,
        },
        card: {
          borderWidth: 2,
          borderColor: colors.outlineVariant,
          borderRadius: 12,
          padding: 16,
          backgroundColor: colors.surfaceContainerLowest,
        },
        cardSelected: {
          borderColor: colors.primary,
          backgroundColor: colors.surfaceContainerLow,
        },
        cardDisabled: {
          opacity: 0.7,
        },
        cardPressed: {
          opacity: 0.95,
        },
        preview: {
          height: 112,
          borderRadius: 12,
          borderWidth: 1,
          borderColor: colors.outlineVariant,
          alignItems: "center",
          justifyContent: "center",
          marginBottom: 12,
        },
        previewSystem: {
          borderColor: colors.outline,
        },
        previewIcon: {
          fontSize: 40,
        },
        labelRow: {
          flexDirection: "row",
          alignItems: "center",
          gap: 8,
        },
        radioOuter: {
          width: 16,
          height: 16,
          borderRadius: 8,
          borderWidth: 1,
          borderColor: colors.outlineVariant,
          alignItems: "center",
          justifyContent: "center",
        },
        radioOuterSelected: {
          borderColor: colors.primary,
        },
        radioInner: {
          width: 8,
          height: 8,
          borderRadius: 4,
          backgroundColor: colors.primary,
        },
        label: {
          fontSize: 14,
          fontWeight: "600",
          color: colors.onSurface,
        },
        hint: {
          marginTop: 8,
          fontSize: 12,
          lineHeight: 16,
          color: colors.onSurfaceVariant,
        },
      }),
    [colors]
  );

  return (
    <View style={styles.list}>
      {THEME_OPTIONS.map((option) => {
        const selected = normalizedValue === option.value;

        return (
          <Pressable
            key={option.value}
            onPress={() => onChange(option.value)}
            disabled={disabled}
            accessibilityRole="radio"
            accessibilityState={{ selected, disabled }}
            style={({ pressed }) => [
              styles.card,
              selected && styles.cardSelected,
              disabled && styles.cardDisabled,
              pressed && !disabled && styles.cardPressed,
            ]}
          >
            <View
              style={[
                styles.preview,
                { backgroundColor: option.previewColor },
                option.value === "SYSTEM" && styles.previewSystem,
              ]}
            >
              <Text style={styles.previewIcon}>{option.icon}</Text>
            </View>

            <View style={styles.labelRow}>
              <View style={[styles.radioOuter, selected && styles.radioOuterSelected]}>
                {selected ? <View style={styles.radioInner} /> : null}
              </View>
              <Text style={styles.label}>{option.label}</Text>
            </View>

            {option.hint ? <Text style={styles.hint}>{option.hint}</Text> : null}
          </Pressable>
        );
      })}
    </View>
  );
}

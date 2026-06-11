import { Pressable, StyleSheet, Text, View } from "react-native";
import { PROFILE_STATUS_FILTERS } from "../constants/profileConstants";
import { colors } from "../../../shared/theme/colors";

export function ProfilePostsFilter({ value, onChange, disabled = false }) {
  return (
    <View style={styles.row}>
      {PROFILE_STATUS_FILTERS.map((option) => {
        const isActive = value === option.value;
        return (
          <Pressable
            key={option.value}
            disabled={disabled}
            onPress={() => onChange?.(option.value)}
            style={[styles.chip, isActive && styles.chipActive, disabled && styles.disabled]}
          >
            <Text style={[styles.chipText, isActive && styles.chipTextActive]}>
              {option.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 8,
    paddingHorizontal: 16,
    paddingBottom: 12,
  },
  chip: {
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  chipActive: {
    borderColor: colors.primary,
    backgroundColor: colors.surfaceContainerLow,
  },
  chipText: {
    fontSize: 13,
    fontWeight: "500",
    color: colors.onSurfaceVariant,
  },
  chipTextActive: {
    color: colors.primary,
  },
  disabled: {
    opacity: 0.5,
  },
});

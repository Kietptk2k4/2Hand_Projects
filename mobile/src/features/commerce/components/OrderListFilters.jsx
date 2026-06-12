import { Pressable, ScrollView, Text, View } from "react-native";
import { ORDER_STATUS_FILTERS } from "../constants/orderListConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    row: { flexDirection: "row", gap: 8, paddingVertical: 4 },
    chip: {
      borderRadius: 20,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      paddingHorizontal: 14,
      paddingVertical: 8,
    },
    chipActive: {
      borderColor: colors.primary,
      backgroundColor: `${colors.primary}14`,
    },
    chipDisabled: { opacity: 0.5 },
    chipText: { fontSize: 13, fontWeight: "500", color: colors.onSurfaceVariant },
    chipTextActive: { color: colors.primary, fontWeight: "600" },
  };
}

export function OrderListFilters({ activeFilterId, onChange, disabled = false }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.row}
      accessibilityRole="tablist"
      accessibilityLabel="Lọc trạng thái đơn hàng"
    >
      {ORDER_STATUS_FILTERS.map((filter) => {
        const isActive = filter.id === activeFilterId;
        return (
          <Pressable
            key={filter.id}
            style={[
              styles.chip,
              isActive ? styles.chipActive : null,
              disabled ? styles.chipDisabled : null,
            ]}
            disabled={disabled}
            onPress={() => onChange?.(filter.status)}
            accessibilityRole="tab"
            accessibilityState={{ selected: isActive, disabled }}
            accessibilityLabel={filter.label}
          >
            <Text style={[styles.chipText, isActive ? styles.chipTextActive : null]}>
              {filter.label}
            </Text>
          </Pressable>
        );
      })}
    </ScrollView>
  );
}
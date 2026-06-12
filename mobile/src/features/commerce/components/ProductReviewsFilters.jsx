import { useState } from "react";
import { Modal, Pressable, Text, View } from "react-native";
import { RATING_TABS, SORT_OPTIONS } from "../constants/productReviewsConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 16,
    },
    label: { fontSize: 14, fontWeight: "600", color: colors.onSurface },
    sortRow: { flexDirection: "row", alignItems: "center", gap: 8 },
    sortMuted: { fontSize: 13, color: colors.onSurfaceVariant },
    sortTrigger: { paddingVertical: 4 },
    sortValue: { fontSize: 14, fontWeight: "600", color: colors.primary },
    chips: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
    chip: {
      borderRadius: 999,
      paddingHorizontal: 12,
      paddingVertical: 8,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surface,
    },
    chipActive: {
      borderColor: colors.primary,
      backgroundColor: colors.primary,
    },
    chipText: { fontSize: 12, fontWeight: "500", color: colors.onSurfaceVariant },
    chipTextActive: { color: colors.onPrimary },
    modalBackdrop: {
      flex: 1,
      backgroundColor: "rgba(0,0,0,0.4)",
      justifyContent: "flex-end",
    },
    sheet: {
      backgroundColor: colors.surfaceContainerLowest,
      borderTopLeftRadius: 16,
      borderTopRightRadius: 16,
      paddingBottom: 24,
    },
    sheetTitle: {
      fontSize: 16,
      fontWeight: "600",
      color: colors.onSurface,
      paddingHorizontal: 20,
      paddingVertical: 16,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
    },
    option: { paddingHorizontal: 20, paddingVertical: 14 },
    optionActive: { backgroundColor: colors.primaryContainer },
    optionText: { fontSize: 15, color: colors.onSurface },
    optionTextActive: { color: colors.primary, fontWeight: "600" },
    disabled: { opacity: 0.5 },
  };
}

export function ProductReviewsFilters({
  sort,
  ratingFilter,
  onSortChange,
  onRatingFilterChange,
  disabled = false,
}) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [visible, setVisible] = useState(false);
  const selected = SORT_OPTIONS.find((option) => option.value === sort) ?? SORT_OPTIONS[0];

  const handleSelect = (nextValue) => {
    setVisible(false);
    if (nextValue !== sort) onSortChange?.(nextValue);
  };

  return (
    <View style={styles.card}>
      <View>
        <Text style={styles.label}>Sắp xếp</Text>
        <View style={styles.sortRow}>
          <Text style={styles.sortMuted}>Theo:</Text>
          <Pressable
            style={[styles.sortTrigger, disabled && styles.disabled]}
            onPress={() => !disabled && setVisible(true)}
            disabled={disabled}
          >
            <Text style={styles.sortValue}>{selected.label}</Text>
          </Pressable>
        </View>
      </View>

      <View>
        <Text style={styles.label}>Lọc theo sao</Text>
        <View style={styles.chips}>
          {RATING_TABS.map((tab) => {
            const active = ratingFilter === tab.value;
            return (
              <Pressable
                key={tab.label}
                style={[styles.chip, active && styles.chipActive, disabled && styles.disabled]}
                disabled={disabled}
                onPress={() => onRatingFilterChange?.(tab.value)}
              >
                <Text style={[styles.chipText, active && styles.chipTextActive]}>{tab.label}</Text>
              </Pressable>
            );
          })}
        </View>
      </View>

      <Modal visible={visible} transparent animationType="slide" onRequestClose={() => setVisible(false)}>
        <Pressable style={styles.modalBackdrop} onPress={() => setVisible(false)}>
          <Pressable style={styles.sheet} onPress={(event) => event.stopPropagation()}>
            <Text style={styles.sheetTitle}>Sắp xếp đánh giá</Text>
            {SORT_OPTIONS.map((option) => {
              const isActive = option.value === sort;
              return (
                <Pressable
                  key={option.value}
                  style={[styles.option, isActive && styles.optionActive]}
                  onPress={() => handleSelect(option.value)}
                >
                  <Text style={[styles.optionText, isActive && styles.optionTextActive]}>{option.label}</Text>
                </Pressable>
              );
            })}
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

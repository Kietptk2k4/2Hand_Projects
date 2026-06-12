import { useState } from "react";
import { Modal, Pressable, Text, View } from "react-native";
import { SORT_OPTIONS } from "../constants/productListConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    row: {
      flexDirection: "row",
      alignItems: "center",
      gap: 8,
    },
    label: {
      fontSize: 13,
      color: colors.onSurfaceVariant,
    },
    trigger: {
      paddingVertical: 4,
      paddingHorizontal: 2,
    },
    triggerText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.primary,
    },
    triggerDisabled: {
      opacity: 0.6,
    },
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
    option: {
      paddingHorizontal: 20,
      paddingVertical: 14,
    },
    optionActive: {
      backgroundColor: colors.primaryContainer,
    },
    optionText: {
      fontSize: 15,
      color: colors.onSurface,
    },
    optionTextActive: {
      color: colors.primary,
      fontWeight: "600",
    },
  };
}

export function ProductListSortSelect({ value, onChange, disabled = false }) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [visible, setVisible] = useState(false);
  const selected = SORT_OPTIONS.find((option) => option.value === value) ?? SORT_OPTIONS[0];

  const handleSelect = (nextValue) => {
    setVisible(false);
    if (nextValue !== value) onChange(nextValue);
  };

  return (
    <>
      <View style={styles.row}>
        <Text style={styles.label}>Sắp xếp:</Text>
        <Pressable
          style={[styles.trigger, disabled && styles.triggerDisabled]}
          onPress={() => !disabled && setVisible(true)}
          disabled={disabled}
          accessibilityRole="button"
          accessibilityLabel="Sắp xếp sản phẩm"
        >
          <Text style={styles.triggerText}>{selected.label}</Text>
        </Pressable>
      </View>

      <Modal visible={visible} transparent animationType="slide" onRequestClose={() => setVisible(false)}>
        <Pressable style={styles.modalBackdrop} onPress={() => setVisible(false)}>
          <Pressable style={styles.sheet} onPress={(event) => event.stopPropagation()}>
            <Text style={styles.sheetTitle}>Sắp xếp sản phẩm</Text>
            {SORT_OPTIONS.map((option) => {
              const isActive = option.value === value;
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
    </>
  );
}

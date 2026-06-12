import { ActivityIndicator, FlatList, Modal, Pressable, Text, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    overlay: {
      flex: 1,
      backgroundColor: "rgba(0,0,0,0.45)",
      justifyContent: "flex-end",
    },
    sheet: {
      maxHeight: "70%",
      backgroundColor: colors.surfaceContainerLowest,
      borderTopLeftRadius: 20,
      borderTopRightRadius: 20,
      paddingBottom: 24,
    },
    header: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      paddingHorizontal: 16,
      paddingVertical: 14,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
    },
    title: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    closeText: { fontSize: 14, color: colors.primary, fontWeight: "600" },
    item: {
      paddingHorizontal: 16,
      paddingVertical: 14,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
    },
    itemSelected: { backgroundColor: colors.primaryContainer },
    itemText: { fontSize: 15, color: colors.onSurface },
    itemTextSelected: { color: colors.onPrimaryContainer, fontWeight: "600" },
    loading: { padding: 24, alignItems: "center" },
    empty: { padding: 24, alignItems: "center" },
    emptyText: { color: colors.onSurfaceVariant, fontSize: 14 },
  };
}

export function GhnPickerModal({
  visible,
  title,
  options,
  selectedValue,
  isLoading,
  onSelect,
  onClose,
}) {
  const styles = useThemedStyles(createStyles);
  const colors = useThemeColors();

  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <Pressable style={styles.overlay} onPress={onClose}>
        <Pressable style={styles.sheet} onPress={(event) => event.stopPropagation()}>
          <View style={styles.header}>
            <Text style={styles.title}>{title}</Text>
            <Pressable onPress={onClose}>
              <Text style={styles.closeText}>Đóng</Text>
            </Pressable>
          </View>

          {isLoading ? (
            <View style={styles.loading}>
              <ActivityIndicator color={colors.primary} />
            </View>
          ) : (
            <FlatList
              data={options}
              keyExtractor={(item) => item.value}
              ListEmptyComponent={
                <View style={styles.empty}>
                  <Text style={styles.emptyText}>Không có dữ liệu</Text>
                </View>
              }
              renderItem={({ item }) => {
                const selected = item.value === selectedValue;
                return (
                  <Pressable
                    style={[styles.item, selected ? styles.itemSelected : null]}
                    onPress={() => {
                      onSelect?.(item.value);
                      onClose?.();
                    }}
                  >
                    <Text style={[styles.itemText, selected ? styles.itemTextSelected : null]}>
                      {item.label}
                    </Text>
                  </Pressable>
                );
              }}
            />
          )}
        </Pressable>
      </Pressable>
    </Modal>
  );
}
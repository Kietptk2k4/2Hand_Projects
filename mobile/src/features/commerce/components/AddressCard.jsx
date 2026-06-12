import { ActivityIndicator, Pressable, Text, View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { formatAddressHeader, formatAddressLine } from "../utils/formatAddressLine";

function createStyles(colors) {
  return {
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 12,
    },
    cardDefault: { borderColor: colors.primary },
    headerRow: { flexDirection: "row", flexWrap: "wrap", alignItems: "center", gap: 8 },
    headerText: { fontSize: 16, fontWeight: "600", color: colors.onSurface, flexShrink: 1 },
    badge: {
      borderRadius: 999,
      backgroundColor: `${colors.primary}1A`,
      paddingHorizontal: 10,
      paddingVertical: 4,
    },
    badgeText: { fontSize: 12, fontWeight: "600", color: colors.primary },
    line: { fontSize: 14, color: colors.onSurfaceVariant, lineHeight: 20 },
    actions: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
    actionButton: {
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      paddingHorizontal: 12,
      paddingVertical: 8,
    },
    actionPrimary: { borderColor: colors.primary },
    actionDanger: { borderColor: `${colors.error}66` },
    actionText: { fontSize: 14, color: colors.onSurface },
    actionTextPrimary: { color: colors.primary, fontWeight: "600" },
    actionTextDanger: { color: colors.error },
    mutating: { flexDirection: "row", alignItems: "center", gap: 8 },
  };
}

export function AddressCard({
  address,
  disabled,
  isMutating,
  onEdit,
  onSetDefault,
  onDelete,
}) {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={[styles.card, address.isDefault ? styles.cardDefault : null]}>
      <View style={styles.headerRow}>
        <Text style={styles.headerText}>{formatAddressHeader(address)}</Text>
        {address.isDefault ? (
          <View style={styles.badge}>
            <Text style={styles.badgeText}>Mặc định</Text>
          </View>
        ) : null}
      </View>
      <Text style={styles.line}>{formatAddressLine(address)}</Text>

      {isMutating ? (
        <View style={styles.mutating}>
          <ActivityIndicator size="small" />
          <Text style={styles.line}>Đang xử lý...</Text>
        </View>
      ) : (
        <View style={styles.actions}>
          <Pressable style={styles.actionButton} disabled={disabled} onPress={() => onEdit?.(address)}>
            <Text style={styles.actionText}>Sửa</Text>
          </Pressable>
          {!address.isDefault ? (
            <Pressable
              style={[styles.actionButton, styles.actionPrimary]}
              disabled={disabled}
              onPress={() => onSetDefault?.(address)}
            >
              <Text style={styles.actionTextPrimary}>Đặt mặc định</Text>
            </Pressable>
          ) : null}
          <Pressable
            style={[styles.actionButton, styles.actionDanger]}
            disabled={disabled}
            onPress={() => onDelete?.(address)}
          >
            <Text style={styles.actionTextDanger}>Xóa</Text>
          </Pressable>
        </View>
      )}
    </View>
  );
}
import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { formatVndPrice } from "../utils/formatVndPrice";
import { getLineTotal, isCartItemInvalid } from "../utils/cartDisplay";

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
    title: {
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
      paddingBottom: 12,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
    },
    row: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "flex-start",
      gap: 12,
    },
    label: {
      flex: 1,
      fontSize: 14,
      color: colors.onSurface,
    },
    value: {
      fontSize: 14,
      color: colors.onSurface,
    },
    invalidRow: {
      opacity: 0.8,
    },
    invalidText: {
      textDecorationLine: "line-through",
      color: colors.onSurfaceVariant,
    },
    totalSection: {
      paddingTop: 12,
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      gap: 4,
    },
    totalLabel: {
      fontSize: 16,
      fontWeight: "600",
      color: colors.onSurface,
    },
    totalValue: {
      fontSize: 22,
      fontWeight: "600",
      color: colors.onSurface,
    },
    shippingNote: {
      fontSize: 12,
      color: colors.onSurfaceVariant,
      textAlign: "right",
    },
    checkoutButton: {
      marginTop: 8,
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingVertical: 14,
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "center",
      gap: 8,
    },
    checkoutButtonDisabled: {
      opacity: 0.5,
    },
    checkoutText: {
      fontSize: 14,
      fontWeight: "700",
      color: colors.onPrimary,
    },
    secureRow: {
      marginTop: 8,
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "center",
      gap: 4,
    },
    secureText: {
      fontSize: 12,
      color: colors.onSurfaceVariant,
    },
  };
}

export function CartSummaryCard({
  cart,
  selectedItems = [],
  onCheckout,
  isMutating = false,
  canCheckout,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const summary = cart?.summary;
  const checkoutEnabled = canCheckout ?? summary?.canCheckout;
  const items = cart?.items || [];
  const invalidItems = items.filter(isCartItemInvalid);
  const invalidDisplayTotal = invalidItems.reduce((sum, item) => sum + getLineTotal(item), 0);
  const selectedSubtotal = selectedItems.reduce((sum, item) => sum + getLineTotal(item), 0);
  const selectedCount = selectedItems.length;

  return (
    <View style={styles.card}>
      <Text style={styles.title}>Tóm tắt đơn hàng</Text>

      <View style={styles.row}>
        <Text style={styles.label}>
          Tạm tính
          {selectedCount > 0 ? ` (${selectedCount} sản phẩm đã chọn)` : ""}
        </Text>
        <Text style={styles.value}>{formatVndPrice(selectedSubtotal)}</Text>
      </View>

      {summary?.invalidItemCount > 0 ? (
        <View style={[styles.row, styles.invalidRow]}>
          <Text style={[styles.label, styles.invalidText]}>
            Sản phẩm không khả dụng ({summary.invalidItemCount})
          </Text>
          <Text style={[styles.value, styles.invalidText]}>
            {formatVndPrice(invalidDisplayTotal)}
          </Text>
        </View>
      ) : null}

      <View style={styles.totalSection}>
        <View style={styles.row}>
          <Text style={styles.totalLabel}>Tổng cộng</Text>
          <Text style={styles.totalValue}>{formatVndPrice(selectedSubtotal)}</Text>
        </View>
        <Text style={styles.shippingNote}>Phí vận chuyển tính khi thanh toán</Text>
      </View>

      <Pressable
        style={[
          styles.checkoutButton,
          !checkoutEnabled || isMutating ? styles.checkoutButtonDisabled : null,
        ]}
        disabled={!checkoutEnabled || isMutating}
        onPress={onCheckout}
      >
        <Text style={styles.checkoutText}>Tiến hành thanh toán</Text>
        <Ionicons name="arrow-forward" size={16} color={colors.onPrimary} />
      </Pressable>

      <View style={styles.secureRow}>
        <Ionicons name="lock-closed-outline" size={14} color={colors.onSurfaceVariant} />
        <Text style={styles.secureText}>Thanh toán an toàn</Text>
      </View>
    </View>
  );
}

import { ActivityIndicator, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { QUOTE_DISCLAIMER } from "../constants/checkoutConstants";
import { formatVndPrice } from "../utils/formatVndPrice";

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
      fontWeight: "700",
      color: colors.onSurface,
      paddingBottom: 12,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
    },
    row: { flexDirection: "row", justifyContent: "space-between", gap: 12 },
    label: { fontSize: 14, color: colors.onSurface },
    value: { fontSize: 14, color: colors.onSurface, fontWeight: "500" },
    totalRow: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "center",
      paddingTop: 12,
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
    },
    totalLabel: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    totalValue: { fontSize: 22, fontWeight: "700", color: colors.primary },
    disclaimer: { fontSize: 12, color: colors.onSurfaceVariant, lineHeight: 18 },
    hint: { fontSize: 14, color: colors.onSurfaceVariant },
    button: {
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingVertical: 14,
      alignItems: "center",
      flexDirection: "row",
      justifyContent: "center",
      gap: 8,
    },
    buttonDisabled: { opacity: 0.5 },
    buttonText: { fontSize: 16, fontWeight: "700", color: colors.onPrimary },
    secure: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "center",
      gap: 6,
    },
    secureText: { fontSize: 12, color: colors.onSurfaceVariant },
    skeleton: { height: 80, borderRadius: 12, backgroundColor: colors.surfaceContainerHigh, opacity: 0.7 },
  };
}

export function CheckoutSummaryCard({
  quote,
  isLoading,
  canSubmit,
  isSubmitting,
  onPlaceOrder,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.card}>
      <Text style={styles.title}>Đơn hàng của bạn</Text>

      {isLoading ? <View style={styles.skeleton} /> : null}

      {!isLoading && quote ? (
        <>
          <View style={styles.row}>
            <Text style={styles.label}>Tạm tính</Text>
            <Text style={styles.value}>{formatVndPrice(quote.totalAmount)}</Text>
          </View>
          <View style={styles.row}>
            <Text style={styles.label}>Phí vận chuyển</Text>
            <Text style={styles.value}>{formatVndPrice(quote.shippingFee)}</Text>
          </View>
          <View style={styles.totalRow}>
            <Text style={styles.totalLabel}>Tổng thanh toán</Text>
            <Text style={styles.totalValue}>{formatVndPrice(quote.finalAmount)}</Text>
          </View>
          <Text style={styles.disclaimer}>{QUOTE_DISCLAIMER}</Text>
        </>
      ) : null}

      {!isLoading && !quote ? (
        <Text style={styles.hint}>Chọn địa chỉ giao hàng để xem tổng tiền.</Text>
      ) : null}

      <Pressable
        style={[styles.button, !canSubmit || isSubmitting ? styles.buttonDisabled : null]}
        disabled={!canSubmit || isSubmitting}
        onPress={onPlaceOrder}
      >
        {isSubmitting ? (
          <ActivityIndicator color={colors.onPrimary} />
        ) : (
          <>
            <Text style={styles.buttonText}>Đặt hàng</Text>
            <Ionicons name="lock-closed" size={16} color={colors.onPrimary} />
          </>
        )}
      </Pressable>

      <View style={styles.secure}>
        <Ionicons name="shield-checkmark-outline" size={14} color={colors.onSurfaceVariant} />
        <Text style={styles.secureText}>Thanh toán an toàn</Text>
      </View>
    </View>
  );
}
import { Text, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import {
  ORDER_PAYMENT_STATUS_LABELS,
  PAYMENT_METHOD_LABELS,
} from "../constants/orderListConstants";
import { isOrderCancelledDueToPayment } from "../constants/paymentStatusLabels";
import { formatOrderDate } from "../utils/formatOrderDate";
import { formatVndPrice } from "../utils/formatVndPrice";

function getPaymentStatusBadgeStyle(colors, status) {
  switch (status) {
    case "PENDING":
      return { backgroundColor: "#FEF3C7", color: "#92400E" };
    case "PAID":
      return { backgroundColor: "#D1FAE5", color: "#065F46" };
    case "FAILED":
    case "CANCELLED":
      return { backgroundColor: colors.errorContainer, color: colors.onErrorContainer };
    default:
      return { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurfaceVariant };
  }
}

function createStyles(colors) {
  return {
    section: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
    },
    title: { fontSize: 16, fontWeight: "600", color: colors.onSurface, marginBottom: 16 },
    notice: {
      borderRadius: 10,
      borderWidth: 1,
      borderColor: `${colors.error}33`,
      backgroundColor: `${colors.errorContainer}26`,
      padding: 12,
      marginBottom: 14,
    },
    noticeText: { fontSize: 14, color: colors.onSurface, lineHeight: 20 },
    row: { flexDirection: "row", justifyContent: "space-between", marginBottom: 8 },
    rowLabel: { fontSize: 14, color: colors.onSurfaceVariant },
    rowValue: { fontSize: 14, color: colors.onSurface },
    totalRow: {
      flexDirection: "row",
      justifyContent: "space-between",
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      paddingTop: 12,
      marginTop: 4,
    },
    totalLabel: { fontSize: 14, fontWeight: "600", color: colors.onSurface },
    totalValue: { fontSize: 18, fontWeight: "700", color: colors.primary },
    badges: { flexDirection: "row", flexWrap: "wrap", gap: 8, marginTop: 14, alignItems: "center" },
    badge: { borderRadius: 12, paddingHorizontal: 10, paddingVertical: 3 },
    badgeText: { fontSize: 12, fontWeight: "500" },
    methodText: { fontSize: 14, color: colors.onSurfaceVariant },
    paidAt: { fontSize: 13, color: colors.onSurfaceVariant, marginTop: 12 },
    paidAtValue: { color: colors.onSurface },
  };
}

export function OrderDetailPaymentSummary({ order, shipments }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!order) return null;

  const showPaymentFailureNotice = isOrderCancelledDueToPayment(
    order.orderStatus,
    order.orderPaymentStatus,
  );

  const shippingFee =
    shipments?.reduce((sum, shipment) => sum + (shipment.shippingFee || 0), 0) ??
    Math.max(0, (order.finalAmount || 0) - (order.totalAmount || 0));

  const paymentStatusLabel =
    ORDER_PAYMENT_STATUS_LABELS[order.orderPaymentStatus] || order.orderPaymentStatus;
  const paymentBadge = getPaymentStatusBadgeStyle(colors, order.orderPaymentStatus);
  const paymentMethodLabel = PAYMENT_METHOD_LABELS[order.paymentMethod] || order.paymentMethod;

  return (
    <View style={styles.section}>
      <Text style={styles.title}>Tóm tắt thanh toán</Text>

      {showPaymentFailureNotice ? (
        <View style={styles.notice}>
          <Text style={styles.noticeText}>Đơn đã hủy do thanh toán không hoàn tất.</Text>
        </View>
      ) : null}

      <View style={styles.row}>
        <Text style={styles.rowLabel}>Tạm tính</Text>
        <Text style={styles.rowValue}>{formatVndPrice(order.totalAmount)}</Text>
      </View>
      <View style={styles.row}>
        <Text style={styles.rowLabel}>Phí vận chuyển</Text>
        <Text style={styles.rowValue}>{formatVndPrice(shippingFee)}</Text>
      </View>
      <View style={styles.totalRow}>
        <Text style={styles.totalLabel}>Tổng cộng</Text>
        <Text style={styles.totalValue}>{formatVndPrice(order.finalAmount)}</Text>
      </View>

      <View style={styles.badges}>
        <View style={[styles.badge, { backgroundColor: paymentBadge.backgroundColor }]}>
          <Text style={[styles.badgeText, { color: paymentBadge.color }]}>{paymentStatusLabel}</Text>
        </View>
        {paymentMethodLabel ? <Text style={styles.methodText}>{paymentMethodLabel}</Text> : null}
      </View>

      {order.payment?.paidAt ? (
        <Text style={styles.paidAt}>
          Thanh toán lúc:{" "}
          <Text style={styles.paidAtValue}>{formatOrderDate(order.payment.paidAt)}</Text>
        </Text>
      ) : null}
    </View>
  );
}
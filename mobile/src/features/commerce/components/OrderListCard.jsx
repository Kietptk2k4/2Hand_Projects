import { Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import {
  ORDER_PAYMENT_STATUS_LABELS,
  ORDER_STATUS_LABELS,
  PAYMENT_METHOD_LABELS,
} from "../constants/orderListConstants";
import { formatOrderDate, formatShortOrderId } from "../utils/formatOrderDate";
import { formatVndPrice } from "../utils/formatVndPrice";

function getOrderStatusBadgeStyle(colors, status) {
  switch (status) {
    case "AWAITING_PAYMENT":
      return { backgroundColor: "#FEF3C7", color: "#78350F" };
    case "PROCESSING":
      return { backgroundColor: `${colors.primary}1A`, color: colors.primary };
    case "COMPLETED":
      return { backgroundColor: "#D1FAE5", color: "#065F46" };
    case "CANCELLED":
      return { backgroundColor: colors.errorContainer, color: colors.onErrorContainer };
    default:
      return { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurfaceVariant };
  }
}

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
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      overflow: "hidden",
    },
    body: { padding: 16 },
    headerRow: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "center",
      gap: 8,
    },
    orderId: { fontSize: 14, fontWeight: "600", color: colors.onSurface, fontVariant: ["tabular-nums"] },
    date: { fontSize: 12, color: colors.onSurfaceVariant },
    badges: { flexDirection: "row", flexWrap: "wrap", gap: 6, marginTop: 10, alignItems: "center" },
    badge: { borderRadius: 12, paddingHorizontal: 10, paddingVertical: 3 },
    badgeText: { fontSize: 12, fontWeight: "500" },
    methodText: { fontSize: 12, color: colors.onSurfaceVariant },
    pendingReviewBadge: { backgroundColor: "#FEF3C7" },
    pendingReviewText: { color: "#78350F" },
    productRow: { flexDirection: "row", gap: 12, marginTop: 14, alignItems: "flex-start" },
    imageWrap: {
      width: 64,
      height: 64,
      borderRadius: 10,
      backgroundColor: colors.surfaceContainerHigh,
      overflow: "hidden",
      alignItems: "center",
      justifyContent: "center",
    },
    image: { width: 64, height: 64 },
    productInfo: { flex: 1, minWidth: 0 },
    productName: { fontSize: 14, fontWeight: "500", color: colors.onSurface },
    extraCount: { fontWeight: "400", color: colors.onSurfaceVariant },
    shipmentHint: { fontSize: 12, color: colors.onSurfaceVariant, marginTop: 6 },
    price: { fontSize: 16, fontWeight: "700", color: colors.primary },
    footer: {
      flexDirection: "row",
      justifyContent: "flex-end",
      gap: 8,
      borderTopWidth: 1,
      borderTopColor: `${colors.outlineVariant}99`,
      paddingHorizontal: 16,
      paddingVertical: 12,
    },
    primaryBtn: {
      borderRadius: 10,
      backgroundColor: colors.primary,
      paddingHorizontal: 16,
      paddingVertical: 10,
    },
    primaryBtnText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
    outlineBtn: {
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.primary,
      paddingHorizontal: 16,
      paddingVertical: 10,
    },
    outlineBtnText: { fontSize: 14, fontWeight: "600", color: colors.primary },
  };
}

function ShipmentHint({ shipmentSummary, styles }) {
  if (!shipmentSummary?.shipmentCount) return null;
  const statusLabel = shipmentSummary.statuses?.[0] || "Đang giao";
  return (
    <Text style={styles.shipmentHint}>
      {shipmentSummary.shipmentCount} lô hàng · {statusLabel}
    </Text>
  );
}

export function OrderListCard({ order }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!order) return null;

  const statusLabel = ORDER_STATUS_LABELS[order.orderStatus] || order.orderStatus;
  const orderBadge = getOrderStatusBadgeStyle(colors, order.orderStatus);
  const paymentStatusLabel =
    ORDER_PAYMENT_STATUS_LABELS[order.orderPaymentStatus] || order.orderPaymentStatus;
  const paymentBadge = getPaymentStatusBadgeStyle(colors, order.orderPaymentStatus);
  const paymentMethodLabel = PAYMENT_METHOD_LABELS[order.paymentMethod] || order.paymentMethod;
  const extraCount = order.itemCount > 1 ? order.itemCount - 1 : 0;
  const imageUri = resolveDevMediaUrl(order.previewImageUrl);
  const showPayNow =
    order.orderStatus === "AWAITING_PAYMENT" &&
    order.paymentMethod === "PAYOS" &&
    order.payment?.paymentId;

  const goToDetail = () => {
    if (order.orderId) router.push(ROUTES.commerceOrderDetail(order.orderId));
  };

  const goToPay = () => {
    if (order.payment?.paymentId) {
      router.push(ROUTES.commerceCheckoutPaymentResult(order.payment.paymentId));
    }
  };

  return (
    <View style={styles.card}>
      <Pressable style={styles.body} onPress={goToDetail} accessibilityRole="button">
        <View style={styles.headerRow}>
          <Text style={styles.orderId}>{formatShortOrderId(order.orderId)}</Text>
          <Text style={styles.date}>{formatOrderDate(order.createdAt)}</Text>
        </View>

        <View style={styles.badges}>
          <View style={[styles.badge, { backgroundColor: orderBadge.backgroundColor }]}>
            <Text style={[styles.badgeText, { color: orderBadge.color }]}>{statusLabel}</Text>
          </View>
          <View style={[styles.badge, { backgroundColor: paymentBadge.backgroundColor }]}>
            <Text style={[styles.badgeText, { color: paymentBadge.color }]}>{paymentStatusLabel}</Text>
          </View>
          {paymentMethodLabel ? <Text style={styles.methodText}>{paymentMethodLabel}</Text> : null}
          {order.pendingReview ? (
            <View style={[styles.badge, styles.pendingReviewBadge]}>
              <Text style={[styles.badgeText, styles.pendingReviewText]}>Chờ đánh giá</Text>
            </View>
          ) : null}
        </View>

        <View style={styles.productRow}>
          <View style={styles.imageWrap}>
            {imageUri ? (
              <Image source={{ uri: imageUri }} style={styles.image} resizeMode="cover" />
            ) : (
              <Ionicons name="cube-outline" size={28} color={colors.outline} />
            )}
          </View>
          <View style={styles.productInfo}>
            <Text style={styles.productName} numberOfLines={2}>
              {order.previewProductName || "Sản phẩm"}
              {extraCount > 0 ? (
                <Text style={styles.extraCount}> và {extraCount} sản phẩm khác</Text>
              ) : null}
            </Text>
            <ShipmentHint shipmentSummary={order.shipmentSummary} styles={styles} />
          </View>
          <Text style={styles.price}>{formatVndPrice(order.finalAmount)}</Text>
        </View>
      </Pressable>

      <View style={styles.footer}>
        {showPayNow ? (
          <Pressable style={styles.primaryBtn} onPress={goToPay}>
            <Text style={styles.primaryBtnText}>Thanh toán ngay</Text>
          </Pressable>
        ) : null}
        <Pressable style={styles.outlineBtn} onPress={goToDetail}>
          <Text style={styles.outlineBtnText}>Xem chi tiết</Text>
        </Pressable>
      </View>
    </View>
  );
}
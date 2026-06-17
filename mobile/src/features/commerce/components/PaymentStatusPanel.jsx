import { ActivityIndicator, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { PAYMENT_STATUS } from "../constants/paymentConstants";
import { isOrderCancelledDueToPayment } from "../constants/paymentStatusLabels";
import { formatVndPrice } from "../utils/formatVndPrice";
import {
  formatDateTime,
  formatOrderPaymentStatusLabel,
  formatOrderStatusLabel,
  formatPaymentStatusLabel,
} from "../utils/paymentDisplay";

function createStyles(colors) {
  return {
    container: { alignItems: "center", gap: 8 },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface, textAlign: "center" },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center", lineHeight: 20 },
    amount: { fontSize: 24, fontWeight: "700", color: colors.primary, marginTop: 8 },
    meta: { fontSize: 12, color: colors.onSurfaceVariant, textAlign: "center" },
    notice: {
      marginTop: 12,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      padding: 12,
    },
    noticeText: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center" },
    button: {
      marginTop: 16,
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingHorizontal: 24,
      paddingVertical: 12,
    },
    buttonDisabled: { opacity: 0.6 },
    buttonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
    links: { marginTop: 16, alignItems: "center", gap: 8 },
    link: { fontSize: 14, fontWeight: "600", color: colors.primary },
    linkMuted: { fontSize: 14, color: colors.onSurfaceVariant },
    errorCard: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.error}4D`,
      backgroundColor: colors.errorContainer,
      padding: 16,
      width: "100%",
    },
    errorText: { fontSize: 14, color: colors.onErrorContainer, textAlign: "center" },
  };
}

function PaymentResultLinks({ orderId }) {
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.links}>
      {orderId ? (
        <Pressable onPress={() => router.push(ROUTES.commerceOrderDetail(orderId))}>
          <Text style={styles.link}>Xem chi tiết đơn</Text>
        </Pressable>
      ) : null}
      <Pressable onPress={() => router.push(ROUTES.commerceOrders)}>
        <Text style={styles.link}>Danh sách đơn hàng</Text>
      </Pressable>
      <Pressable onPress={() => router.push(ROUTES.commerceHome)}>
        <Text style={styles.linkMuted}>Về trang chủ mua sắm</Text>
      </Pressable>
    </View>
  );
}

function OrderStatusLines({ orderStatus, orderPaymentStatus }) {
  const styles = useThemedStyles(createStyles);
  return (
    <Text style={styles.meta}>
      Đơn hàng: {formatOrderStatusLabel(orderStatus)} · Thanh toán đơn:{" "}
      {formatOrderPaymentStatusLabel(orderPaymentStatus)}
    </Text>
  );
}

export function PaymentStatusPanel({
  status,
  orderId,
  amount,
  paidAt,
  expiredAt,
  orderStatus,
  orderPaymentStatus,
  isLoading,
  error,
  showRetry = false,
  onRetryPayment,
  isRetrying,
  paymentMethod,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (isLoading && !status) {
    return (
      <View style={styles.container}>
        <ActivityIndicator color={colors.primary} />
        <Text style={styles.subtitle}>Đang kiểm tra trạng thái thanh toán...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.container}>
        <View style={styles.errorCard}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
        <PaymentResultLinks orderId={orderId} />
      </View>
    );
  }

  const orderCancelled = isOrderCancelledDueToPayment(orderStatus, orderPaymentStatus);

  if (status === PAYMENT_STATUS.PAID) {
    return (
      <View style={styles.container}>
        <Ionicons name="checkmark-circle" size={56} color={colors.primary} />
        <Text style={styles.title}>Thanh toán thành công</Text>
        <Text style={styles.subtitle}>Cảm ơn bạn. Đơn hàng đã được xác nhận thanh toán.</Text>
        {orderId ? <Text style={styles.meta}>Mã đơn: {orderId}</Text> : null}
        {amount != null ? <Text style={styles.amount}>{formatVndPrice(amount)}</Text> : null}
        {paidAt ? <Text style={styles.meta}>Thanh toán lúc: {formatDateTime(paidAt)}</Text> : null}
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        <Pressable style={styles.button} onPress={() => router.replace(ROUTES.commerceHome)}>
          <Text style={styles.buttonText}>Tiếp tục mua sắm</Text>
        </Pressable>
        {orderId ? (
          <Pressable onPress={() => router.push(ROUTES.commerceOrderDetail(orderId))}>
            <Text style={styles.link}>Xem chi tiết đơn</Text>
          </Pressable>
        ) : null}
      </View>
    );
  }

  if (status === PAYMENT_STATUS.PENDING) {
    return (
      <View style={styles.container}>
        <Ionicons name="time-outline" size={56} color="#d97706" />
        <Text style={styles.title}>Chưa nhận được xác nhận thanh toán</Text>
        <Text style={styles.subtitle}>
          Bạn đã quay lại từ {paymentMethod === "VNPAY" ? "VNPay" : "PayOS"}. Hệ thống đang chờ xác nhận từ cổng thanh toán — vui lòng đợi
          thêm vài giây hoặc thử thanh toán lại nếu đã quá lâu.
        </Text>
        {orderId ? <Text style={styles.meta}>Mã đơn: {orderId}</Text> : null}
        {amount != null ? <Text style={styles.amount}>{formatVndPrice(amount)}</Text> : null}
        {expiredAt ? <Text style={styles.meta}>Hạn thanh toán: {formatDateTime(expiredAt)}</Text> : null}
        <Text style={styles.meta}>Trạng thái thanh toán: {formatPaymentStatusLabel(status)}</Text>
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        {isLoading ? <Text style={styles.meta}>Đang cập nhật trạng thái...</Text> : null}
        {showRetry ? (
          <Pressable
            style={[styles.button, isRetrying ? styles.buttonDisabled : null]}
            disabled={isRetrying}
            onPress={onRetryPayment}
          >
            <Text style={styles.buttonText}>
              {isRetrying ? "Đang tạo liên kết..." : "Thanh toán lại"}
            </Text>
          </Pressable>
        ) : null}
        <PaymentResultLinks orderId={orderId} />
      </View>
    );
  }

  if (status === PAYMENT_STATUS.EXPIRED || status === PAYMENT_STATUS.FAILED) {
    const isExpired = status === PAYMENT_STATUS.EXPIRED;
    return (
      <View style={styles.container}>
        <Ionicons name={isExpired ? "timer-outline" : "close-circle"} size={56} color={isExpired ? "#b45309" : colors.error} />
        <Text style={styles.title}>{isExpired ? "Thanh toán đã hết hạn" : "Thanh toán không thành công"}</Text>
        <Text style={styles.subtitle}>
          {isExpired
            ? "Phiên thanh toán hoặc hạn thanh toán đã kết thúc trước khi giao dịch hoàn tất."
            : "Giao dịch thất bại hoặc không được cổng thanh toán chấp nhận."}
        </Text>
        {orderCancelled ? (
          <View style={styles.notice}>
            <Text style={styles.noticeText}>
              Đơn hàng đã bị hủy. Sản phẩm đã được trả lại kho — bạn có thể đặt lại nếu còn hàng.
            </Text>
          </View>
        ) : null}
        {orderId ? <Text style={styles.meta}>Mã đơn: {orderId}</Text> : null}
        {amount != null ? <Text style={styles.amount}>{formatVndPrice(amount)}</Text> : null}
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        {showRetry ? (
          <Pressable
            style={[styles.button, isRetrying ? styles.buttonDisabled : null]}
            disabled={isRetrying}
            onPress={onRetryPayment}
          >
            <Text style={styles.buttonText}>
              {isRetrying ? "Đang tạo liên kết..." : "Tạo liên kết thanh toán mới"}
            </Text>
          </Pressable>
        ) : null}
        <PaymentResultLinks orderId={orderId} />
      </View>
    );
  }

  if (status === PAYMENT_STATUS.CANCELLED) {
    return (
      <View style={styles.container}>
        <Ionicons name="ban-outline" size={56} color={colors.onSurfaceVariant} />
        <Text style={styles.title}>Thanh toán đã hủy</Text>
        <Text style={styles.subtitle}>
          Giao dịch đã bị hủy trên cổng thanh toán. Đơn hàng không được xác nhận thanh toán.
        </Text>
        {orderCancelled ? (
          <View style={styles.notice}>
            <Text style={styles.noticeText}>
              Đơn hàng đã bị hủy. Sản phẩm đã được trả lại kho — bạn có thể đặt lại nếu còn hàng.
            </Text>
          </View>
        ) : null}
        {orderId ? <Text style={styles.meta}>Mã đơn: {orderId}</Text> : null}
        <OrderStatusLines orderStatus={orderStatus} orderPaymentStatus={orderPaymentStatus} />
        <PaymentResultLinks orderId={orderId} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.subtitle}>
        Trạng thái thanh toán: {formatPaymentStatusLabel(status) || "Không xác định"}
      </Text>
      <PaymentResultLinks orderId={orderId} />
    </View>
  );
}
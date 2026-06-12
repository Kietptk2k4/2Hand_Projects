import { useCallback } from "react";
import {
  ActivityIndicator,
  Alert,
  Pressable,
  ScrollView,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router, useLocalSearchParams } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import {
  buildCancelOrderSuccessToast,
  buildConfirmOrderReceivedSuccessToast,
  mapOrderActionApiError,
} from "../constants/orderActionConstants";
import { ORDER_STATUS_LABELS } from "../constants/orderListConstants";
import { isOrderCancelledDueToPayment } from "../constants/paymentStatusLabels";
import { useCancelOrder } from "../hooks/useCancelOrder";
import { useConfirmOrderReceived } from "../hooks/useConfirmOrderReceived";
import { useOrderDetail } from "../hooks/useOrderDetail";
import { useOrderTrackStatus } from "../hooks/useOrderTrackStatus";
import { canCancelOrder, canConfirmOrderReceived } from "../utils/orderActionDisplay";
import { formatOrderDate, formatShortOrderId } from "../utils/formatOrderDate";
import { OrderDetailItemsSection } from "./OrderDetailItemsSection";
import { OrderDetailPaymentSummary } from "./OrderDetailPaymentSummary";
import { OrderDetailShipmentsSection } from "./OrderDetailShipmentsSection";
import { OrderDetailShippingAddress } from "./OrderDetailShippingAddress";
import { OrderDetailSkeleton } from "./OrderDetailSkeleton";
import { OrderDetailTimeline } from "./OrderDetailTimeline";

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

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { padding: 16, paddingBottom: 32, gap: 16 },
    backLink: { flexDirection: "row", alignItems: "center", gap: 4, marginBottom: 4 },
    backText: { fontSize: 14, color: colors.onSurfaceVariant },
    header: { gap: 8 },
    titleRow: { flexDirection: "row", flexWrap: "wrap", alignItems: "center", gap: 8 },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface },
    badge: { borderRadius: 12, paddingHorizontal: 10, paddingVertical: 3 },
    badgeText: { fontSize: 12, fontWeight: "500" },
    meta: { fontSize: 14, color: colors.onSurfaceVariant },
    actions: { flexDirection: "row", flexWrap: "wrap", gap: 8, marginTop: 4 },
    actionBtn: {
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      paddingHorizontal: 14,
      paddingVertical: 10,
    },
    actionBtnText: { fontSize: 14, color: colors.onSurface },
    cancelBtn: { borderColor: colors.error },
    cancelBtnText: { color: colors.error, fontWeight: "600" },
    confirmBtn: { borderColor: colors.primary },
    confirmBtnText: { color: colors.primary, fontWeight: "600" },
    payBtn: { backgroundColor: colors.primary, borderColor: colors.primary },
    payBtnText: { color: colors.onPrimary, fontWeight: "600" },
    trackError: { fontSize: 13, color: colors.error },
    paymentBanner: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.error}33`,
      backgroundColor: `${colors.errorContainer}26`,
      padding: 14,
    },
    paymentBannerText: { fontSize: 14, color: colors.onSurface, lineHeight: 20 },
    notFoundCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 32,
      alignItems: "center",
      gap: 12,
    },
    notFoundText: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center" },
    primaryButton: {
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingHorizontal: 16,
      paddingVertical: 10,
    },
    primaryButtonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
    errorCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: `${colors.error}4D`,
      backgroundColor: colors.errorContainer,
      padding: 24,
      alignItems: "center",
      gap: 12,
    },
    errorText: { fontSize: 14, color: colors.onErrorContainer, textAlign: "center" },
  };
}

function resolveOrderId(raw) {
  if (typeof raw === "string") return raw.trim();
  if (Array.isArray(raw)) return String(raw[0] || "").trim();
  return "";
}

export function CommerceOrderDetailScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const { orderId: rawOrderId } = useLocalSearchParams();
  const orderId = resolveOrderId(rawOrderId);

  const { order, isLoading, isNotFound, isError, errorMessage, retry } = useOrderDetail(orderId);

  const {
    timelineEvents,
    isLoading: isTrackLoading,
    isRefreshing,
    errorMessage: trackErrorMessage,
    refresh: refreshTrack,
  } = useOrderTrackStatus(orderId, {
    enabled: Boolean(order) && !isNotFound,
    pollWhileProcessing: true,
  });

  const handleActionSuccess = useCallback(() => {
    retry();
    refreshTrack();
  }, [retry, refreshTrack]);

  const handleCancelSuccess = useCallback(
    (result) => {
      showToast(buildCancelOrderSuccessToast(result));
      handleActionSuccess();
    },
    [handleActionSuccess, showToast],
  );

  const handleConfirmSuccess = useCallback(
    (result) => {
      showToast(buildConfirmOrderReceivedSuccessToast(result));
      handleActionSuccess();
    },
    [handleActionSuccess, showToast],
  );

  const { isCancelling, cancel } = useCancelOrder({
    onSuccess: handleCancelSuccess,
  });

  const { isConfirming, confirm } = useConfirmOrderReceived({
    onSuccess: handleConfirmSuccess,
  });

  const showPayNow =
    order?.orderStatus === "AWAITING_PAYMENT" &&
    order?.paymentMethod === "PAYOS" &&
    order?.payment?.paymentId &&
    !isOrderCancelledDueToPayment(order.orderStatus, order.orderPaymentStatus);

  const showPaymentFailureBanner = isOrderCancelledDueToPayment(
    order?.orderStatus,
    order?.orderPaymentStatus,
  );

  const showCancel = canCancelOrder(order);
  const showConfirm = canConfirmOrderReceived(order);
  const isMutating = isCancelling || isConfirming;
  const shippingAddress = order?.shipments?.[0]?.shippingAddress;

  const handleRefresh = useCallback(() => {
    retry();
    refreshTrack();
  }, [retry, refreshTrack]);

  const handleCancelPress = useCallback(() => {
    if (!order?.orderId || isMutating) return;

    Alert.alert(
      "Hủy đơn hàng",
      "Bạn có chắc muốn hủy đơn hàng này?",
      [
        { text: "Đóng", style: "cancel" },
        {
          text: "Hủy đơn",
          style: "destructive",
          onPress: async () => {
            try {
              await cancel(order.orderId);
            } catch (error) {
              showToast(mapOrderActionApiError(error), "error");
            }
          },
        },
      ],
    );
  }, [cancel, isMutating, order?.orderId, showToast]);

  const handleConfirmPress = useCallback(() => {
    if (!order?.orderId || isMutating) return;

    Alert.alert("Xác nhận đã nhận hàng", "Bạn xác nhận đã nhận đủ hàng trong đơn này?", [
      { text: "Hủy", style: "cancel" },
      {
        text: "Xác nhận",
        onPress: async () => {
          try {
            await confirm(order.orderId);
          } catch (error) {
            showToast(mapOrderActionApiError(error), "error");
          }
        },
      },
    ]);
  }, [confirm, isMutating, order?.orderId, showToast]);

  const handlePayNow = useCallback(() => {
    if (order?.payment?.paymentId) {
      router.push(ROUTES.commerceCheckoutPaymentResult(order.payment.paymentId));
    }
  }, [order?.payment?.paymentId]);

  if (isLoading) {
    return (
      <ScrollView style={styles.container} contentContainerStyle={styles.content}>
        <OrderDetailSkeleton />
      </ScrollView>
    );
  }

  if (isNotFound) {
    return (
      <View style={[styles.container, styles.content]}>
        <View style={styles.notFoundCard}>
          <Ionicons name="receipt-outline" size={40} color={colors.outline} />
          <Text style={styles.notFoundText}>{errorMessage || "Không tìm thấy đơn hàng."}</Text>
          <Pressable style={styles.primaryButton} onPress={() => router.push(ROUTES.commerceOrders)}>
            <Text style={styles.primaryButtonText}>Quay lại danh sách đơn</Text>
          </Pressable>
        </View>
      </View>
    );
  }

  if (isError || !order) {
    return (
      <View style={[styles.container, styles.content]}>
        <View style={styles.errorCard}>
          <Text style={styles.errorText}>{errorMessage}</Text>
          <Pressable style={styles.primaryButton} onPress={retry}>
            <Text style={styles.primaryButtonText}>Thử lại</Text>
          </Pressable>
        </View>
      </View>
    );
  }

  const statusLabel = ORDER_STATUS_LABELS[order.orderStatus] || order.orderStatus;
  const statusBadge = getOrderStatusBadgeStyle(colors, order.orderStatus);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Pressable style={styles.backLink} onPress={() => router.push(ROUTES.commerceOrders)}>
        <Ionicons name="arrow-back" size={18} color={colors.onSurfaceVariant} />
        <Text style={styles.backText}>Quay lại đơn hàng</Text>
      </Pressable>

      <View style={styles.header}>
        <View style={styles.titleRow}>
          <Text style={styles.title}>Chi tiết đơn hàng</Text>
          <View style={[styles.badge, { backgroundColor: statusBadge.backgroundColor }]}>
            <Text style={[styles.badgeText, { color: statusBadge.color }]}>{statusLabel}</Text>
          </View>
        </View>
        <Text style={styles.meta}>
          {formatShortOrderId(order.orderId)} · Đặt lúc {formatOrderDate(order.createdAt)}
        </Text>

        <View style={styles.actions}>
          <Pressable
            style={styles.actionBtn}
            onPress={handleRefresh}
            disabled={isTrackLoading || isRefreshing || isMutating}
          >
            {isRefreshing ? (
              <ActivityIndicator size="small" color={colors.primary} />
            ) : (
              <Text style={styles.actionBtnText}>Làm mới</Text>
            )}
          </Pressable>

          {showCancel ? (
            <Pressable
              style={[styles.actionBtn, styles.cancelBtn]}
              onPress={handleCancelPress}
              disabled={isMutating}
            >
              <Text style={[styles.actionBtnText, styles.cancelBtnText]}>Hủy đơn</Text>
            </Pressable>
          ) : null}

          {showConfirm ? (
            <Pressable
              style={[styles.actionBtn, styles.confirmBtn]}
              onPress={handleConfirmPress}
              disabled={isMutating}
            >
              <Text style={[styles.actionBtnText, styles.confirmBtnText]}>Đã nhận hàng</Text>
            </Pressable>
          ) : null}

          {showPayNow ? (
            <Pressable style={[styles.actionBtn, styles.payBtn]} onPress={handlePayNow}>
              <Text style={[styles.actionBtnText, styles.payBtnText]}>Thanh toán ngay</Text>
            </Pressable>
          ) : null}
        </View>

        {trackErrorMessage ? <Text style={styles.trackError}>{trackErrorMessage}</Text> : null}

        {showPaymentFailureBanner ? (
          <View style={styles.paymentBanner}>
            <Text style={styles.paymentBannerText}>
              Đơn đã hủy do thanh toán không hoàn tất. Sản phẩm đã được trả lại kho — bạn có thể đặt
              lại nếu còn hàng.
            </Text>
          </View>
        ) : null}
      </View>

      <OrderDetailItemsSection orderId={order.orderId} items={order.items} />
      <OrderDetailShipmentsSection orderId={order.orderId} shipments={order.shipments} />
      <OrderDetailTimeline
        events={timelineEvents}
        isLoading={isTrackLoading && !timelineEvents.length}
      />
      <OrderDetailPaymentSummary order={order} shipments={order.shipments} />
      <OrderDetailShippingAddress address={shippingAddress} />
    </ScrollView>
  );
}
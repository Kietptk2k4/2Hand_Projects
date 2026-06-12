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
  buildConfirmOrderReceivedSuccessToast,
  mapOrderActionApiError,
} from "../constants/orderActionConstants";
import { ORDER_STATUS_LABELS } from "../constants/orderListConstants";
import { SHIPMENT_STATUS_LABELS } from "../constants/orderDetailConstants";
import { SHIPMENT_TYPE_LABELS } from "../constants/shipmentTrackingConstants";
import { useConfirmOrderReceived } from "../hooks/useConfirmOrderReceived";
import { useOrderDetail } from "../hooks/useOrderDetail";
import { useShipmentTrackingPage } from "../hooks/useShipmentTrackingPage";
import { canConfirmOrderReceived } from "../utils/orderActionDisplay";
import { formatOrderDate, formatShortOrderId } from "../utils/formatOrderDate";
import { formatVndPrice } from "../utils/formatVndPrice";
import { OrderDetailShippingAddress } from "./OrderDetailShippingAddress";
import { ShipmentTrackingItemsSection } from "./ShipmentTrackingItemsSection";
import { ShipmentTrackingSkeleton } from "./ShipmentTrackingSkeleton";
import { ShipmentTrackingTimeline } from "./ShipmentTrackingTimeline";

function getShipmentStatusBadgeStyle(colors, status) {
  switch (status) {
    case "SHIPPED":
      return { backgroundColor: `${colors.primary}1A`, color: colors.primary };
    case "DELIVERED":
      return { backgroundColor: "#D1FAE5", color: "#065F46" };
    case "CANCELLED":
      return { backgroundColor: colors.errorContainer, color: colors.onErrorContainer };
    default:
      return { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurfaceVariant };
  }
}

function getOrderStatusBadgeStyle(colors, status) {
  switch (status) {
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
    backLink: { flexDirection: "row", alignItems: "center", gap: 4 },
    backText: { fontSize: 14, color: colors.onSurfaceVariant },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant, marginTop: 4 },
    hero: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 12,
    },
    heroTop: { flexDirection: "row", gap: 12, alignItems: "flex-start" },
    heroIcon: {
      width: 52,
      height: 52,
      borderRadius: 14,
      backgroundColor: `${colors.primary}1A`,
      alignItems: "center",
      justifyContent: "center",
    },
    heroInfo: { flex: 1 },
    badge: { alignSelf: "flex-start", borderRadius: 12, paddingHorizontal: 10, paddingVertical: 3 },
    badgeText: { fontSize: 12, fontWeight: "500" },
    carrier: { fontSize: 16, fontWeight: "600", color: colors.onSurface, marginTop: 8 },
    typeLabel: { fontSize: 13, color: colors.onSurfaceVariant, marginTop: 4 },
    orderBadgeWrap: { alignSelf: "flex-start" },
    meta: { gap: 6 },
    metaRow: { flexDirection: "row", flexWrap: "wrap", gap: 6 },
    metaLabel: { fontSize: 13, color: colors.onSurfaceVariant },
    metaValue: { fontSize: 13, color: colors.onSurface, fontWeight: "500" },
    confirmBanner: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.primary,
      backgroundColor: `${colors.primary}0D`,
      padding: 14,
      gap: 10,
    },
    confirmText: { fontSize: 14, color: colors.onSurface, lineHeight: 20 },
    confirmBtn: {
      alignSelf: "flex-start",
      borderRadius: 10,
      backgroundColor: colors.primary,
      paddingHorizontal: 14,
      paddingVertical: 10,
    },
    confirmBtnText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
    summary: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
    },
    summaryTitle: { fontSize: 16, fontWeight: "600", color: colors.onSurface, marginBottom: 12 },
    summaryRow: { flexDirection: "row", justifyContent: "space-between", marginBottom: 8 },
    summaryLabel: { fontSize: 14, color: colors.onSurfaceVariant },
    summaryValue: { fontSize: 14, fontWeight: "500", color: colors.onSurface },
    refreshBtn: {
      alignSelf: "flex-start",
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      paddingHorizontal: 14,
      paddingVertical: 8,
    },
    refreshText: { fontSize: 14, color: colors.onSurface },
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
    detailLink: { fontSize: 14, color: colors.primary, marginTop: 4 },
  };
}

function resolveParam(raw) {
  if (typeof raw === "string") return raw.trim();
  if (Array.isArray(raw)) return String(raw[0] || "").trim();
  return "";
}

function ShipmentFeeSummary({ detail, styles }) {
  if (!detail) return null;

  const hasFee = detail.shippingFee != null;
  const hasCod = detail.codAmount != null && detail.codAmount > 0;
  const hasWeight = detail.weightGram != null;

  if (!hasFee && !hasCod && !hasWeight) return null;

  return (
    <View style={styles.summary}>
      <Text style={styles.summaryTitle}>Tóm tắt phí</Text>
      {hasFee ? (
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Phí vận chuyển</Text>
          <Text style={styles.summaryValue}>{formatVndPrice(detail.shippingFee)}</Text>
        </View>
      ) : null}
      {hasCod ? (
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Thu hộ (COD)</Text>
          <Text style={styles.summaryValue}>{formatVndPrice(detail.codAmount)}</Text>
        </View>
      ) : null}
      {hasWeight ? (
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>Khối lượng</Text>
          <Text style={styles.summaryValue}>{detail.weightGram} g</Text>
        </View>
      ) : null}
    </View>
  );
}

export function CommerceShipmentTrackingScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const params = useLocalSearchParams();
  const orderId = resolveParam(params.orderId);
  const shipmentId = resolveParam(params.shipmentId);

  const {
    detail,
    tracking,
    timelineEvents,
    shipmentDelivered,
    orderCompleted,
    isLoading,
    isRefreshing,
    errorMessage,
    isNotFound,
    isError,
    refresh,
    retry,
  } = useShipmentTrackingPage(orderId, shipmentId);

  const { order, retry: retryOrder } = useOrderDetail(orderId);

  const handleRefreshAll = useCallback(() => {
    refresh();
    retryOrder();
  }, [refresh, retryOrder]);

  const handleConfirmSuccess = useCallback(
    (result) => {
      showToast(buildConfirmOrderReceivedSuccessToast(result));
      handleRefreshAll();
    },
    [handleRefreshAll, showToast],
  );

  const { isConfirming, confirm } = useConfirmOrderReceived({
    onSuccess: handleConfirmSuccess,
  });

  const showConfirmBanner =
    shipmentDelivered && !orderCompleted && canConfirmOrderReceived(order);

  const handleConfirmPress = useCallback(() => {
    if (!orderId || isConfirming) return;

    Alert.alert("Xác nhận đã nhận hàng", "Bạn xác nhận đã nhận đủ hàng trong đơn này?", [
      { text: "Hủy", style: "cancel" },
      {
        text: "Xác nhận",
        onPress: async () => {
          try {
            await confirm(orderId);
          } catch (error) {
            showToast(mapOrderActionApiError(error), "error");
          }
        },
      },
    ]);
  }, [confirm, isConfirming, orderId, showToast]);

  if (isLoading) {
    return (
      <ScrollView style={styles.container} contentContainerStyle={styles.content}>
        <ShipmentTrackingSkeleton />
      </ScrollView>
    );
  }

  if (isNotFound) {
    return (
      <View style={[styles.container, styles.content]}>
        <View style={styles.notFoundCard}>
          <Ionicons name="car-outline" size={40} color={colors.outline} />
          <Text style={styles.notFoundText}>
            {errorMessage || "Không tìm thấy thông tin vận chuyển."}
          </Text>
          {orderId ? (
            <Pressable
              style={styles.primaryButton}
              onPress={() => router.push(ROUTES.commerceOrderDetail(orderId))}
            >
              <Text style={styles.primaryButtonText}>Quay lại chi tiết đơn</Text>
            </Pressable>
          ) : null}
        </View>
      </View>
    );
  }

  if (isError || !detail) {
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

  const status = tracking?.status || detail?.status;
  const statusLabel = SHIPMENT_STATUS_LABELS[status] || status;
  const shipmentBadge = getShipmentStatusBadgeStyle(colors, status);
  const shipmentType = detail?.shipmentType || tracking?.shipmentType;
  const typeLabel = SHIPMENT_TYPE_LABELS[shipmentType] || shipmentType;
  const carrier = detail?.carrier || tracking?.carrier || "Đơn vị vận chuyển";
  const orderStatus = tracking?.orderStatus;
  const orderStatusLabel = orderStatus ? ORDER_STATUS_LABELS[orderStatus] || orderStatus : "";
  const orderBadge = getOrderStatusBadgeStyle(colors, orderStatus);
  const estimatedDelivery = detail?.estimatedDeliveryDate || tracking?.estimatedDeliveryDate;
  const shippedAt = detail?.shippedAt || tracking?.shippedAt;
  const deliveredAt = detail?.deliveredAt || tracking?.deliveredAt;
  const trackingNumber = detail?.trackingNumber || tracking?.trackingNumber;
  const ghnOrderCode = detail?.ghnOrderCode || tracking?.ghnOrderCode;
  const shippingAddress = detail?.shippingAddress;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Pressable
        style={styles.backLink}
        onPress={() => orderId && router.push(ROUTES.commerceOrderDetail(orderId))}
      >
        <Ionicons name="arrow-back" size={18} color={colors.onSurfaceVariant} />
        <Text style={styles.backText}>Quay lại chi tiết đơn</Text>
      </Pressable>

      <View>
        <Text style={styles.title}>Theo dõi vận chuyển</Text>
        <Text style={styles.subtitle}>
          {[carrier, orderId ? formatShortOrderId(orderId) : null].filter(Boolean).join(" · ")}
        </Text>
        {orderId ? (
          <Pressable onPress={() => router.push(ROUTES.commerceOrderDetail(orderId))}>
            <Text style={styles.detailLink}>Xem chi tiết đơn hàng</Text>
          </Pressable>
        ) : null}
      </View>

      <View style={styles.hero}>
        <View style={styles.heroTop}>
          <View style={styles.heroIcon}>
            <Ionicons name="car-outline" size={28} color={colors.primary} />
          </View>
          <View style={styles.heroInfo}>
            <View style={[styles.badge, { backgroundColor: shipmentBadge.backgroundColor }]}>
              <Text style={[styles.badgeText, { color: shipmentBadge.color }]}>{statusLabel}</Text>
            </View>
            <Text style={styles.carrier}>{carrier}</Text>
            {typeLabel ? <Text style={styles.typeLabel}>{typeLabel}</Text> : null}
          </View>
          {orderStatus ? (
            <View style={styles.orderBadgeWrap}>
              <View style={[styles.badge, { backgroundColor: orderBadge.backgroundColor }]}>
                <Text style={[styles.badgeText, { color: orderBadge.color }]}>
                  Đơn: {orderStatusLabel}
                </Text>
              </View>
            </View>
          ) : null}
        </View>

        <View style={styles.meta}>
          {trackingNumber ? (
            <View style={styles.metaRow}>
              <Text style={styles.metaLabel}>Mã vận đơn:</Text>
              <Text style={styles.metaValue}>{trackingNumber}</Text>
            </View>
          ) : null}
          {ghnOrderCode ? (
            <View style={styles.metaRow}>
              <Text style={styles.metaLabel}>Mã GHN:</Text>
              <Text style={styles.metaValue}>{ghnOrderCode}</Text>
            </View>
          ) : null}
          {estimatedDelivery ? (
            <Text style={styles.metaLabel}>
              Dự kiến giao: <Text style={styles.metaValue}>{estimatedDelivery}</Text>
            </Text>
          ) : null}
          {shippedAt ? (
            <Text style={styles.metaLabel}>
              Đã gửi: <Text style={styles.metaValue}>{formatOrderDate(shippedAt)}</Text>
            </Text>
          ) : null}
          {deliveredAt ? (
            <Text style={styles.metaLabel}>
              Đã giao: <Text style={styles.metaValue}>{formatOrderDate(deliveredAt)}</Text>
            </Text>
          ) : null}
        </View>
      </View>

      {showConfirmBanner ? (
        <View style={styles.confirmBanner}>
          <Text style={styles.confirmText}>
            Hàng đã được giao. Xác nhận đã nhận đủ hàng để hoàn tất đơn và có thể đánh giá sản phẩm.
          </Text>
          <Pressable style={styles.confirmBtn} onPress={handleConfirmPress} disabled={isConfirming}>
            <Text style={styles.confirmBtnText}>Xác nhận đã nhận hàng</Text>
          </Pressable>
        </View>
      ) : null}

      <Pressable style={styles.refreshBtn} onPress={handleRefreshAll} disabled={isRefreshing}>
        {isRefreshing ? (
          <ActivityIndicator size="small" color={colors.primary} />
        ) : (
          <Text style={styles.refreshText}>Làm mới</Text>
        )}
      </Pressable>

      <ShipmentTrackingTimeline
        events={timelineEvents}
        isLoading={isRefreshing && !timelineEvents.length}
      />
      <ShipmentTrackingItemsSection items={detail?.items} />
      <OrderDetailShippingAddress address={shippingAddress} />
      <ShipmentFeeSummary detail={detail} styles={styles} />
    </ScrollView>
  );
}
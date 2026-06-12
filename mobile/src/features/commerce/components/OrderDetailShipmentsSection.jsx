import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { SHIPMENT_STATUS_LABELS } from "../constants/orderDetailConstants";
import { formatOrderDate } from "../utils/formatOrderDate";

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
    list: { gap: 12 },
    card: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.outlineVariant}CC`,
      backgroundColor: colors.surfaceContainerLow,
      padding: 14,
    },
    cardHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", gap: 8 },
    carrierRow: { flexDirection: "row", alignItems: "center", gap: 8, flex: 1 },
    carrier: { fontSize: 14, fontWeight: "500", color: colors.onSurface },
    badge: { borderRadius: 12, paddingHorizontal: 10, paddingVertical: 3 },
    badgeText: { fontSize: 12, fontWeight: "500" },
    trackingRow: { flexDirection: "row", flexWrap: "wrap", gap: 6, marginTop: 10 },
    trackingLabel: { fontSize: 13, color: colors.onSurfaceVariant },
    trackingValue: { fontSize: 13, fontWeight: "500", color: colors.onSurface, fontVariant: ["tabular-nums"] },
    meta: { marginTop: 10, gap: 4 },
    metaText: { fontSize: 13, color: colors.onSurfaceVariant },
    metaValue: { color: colors.onSurface },
    trackBtn: {
      alignSelf: "flex-start",
      marginTop: 12,
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.primary,
      paddingHorizontal: 14,
      paddingVertical: 8,
    },
    trackBtnText: { fontSize: 14, fontWeight: "600", color: colors.primary },
  };
}

export function OrderDetailShipmentsSection({ orderId, shipments }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!shipments?.length) return null;

  return (
    <View style={styles.section}>
      <Text style={styles.title}>Vận chuyển</Text>
      <View style={styles.list}>
        {shipments.map((shipment) => {
          const statusLabel = SHIPMENT_STATUS_LABELS[shipment.status] || shipment.status;
          const badgeStyle = getShipmentStatusBadgeStyle(colors, shipment.status);

          return (
            <View key={shipment.shipmentId} style={styles.card}>
              <View style={styles.cardHeader}>
                <View style={styles.carrierRow}>
                  <Ionicons name="car-outline" size={20} color={colors.primary} />
                  <Text style={styles.carrier}>{shipment.carrier || "Đơn vị vận chuyển"}</Text>
                </View>
                <View style={[styles.badge, { backgroundColor: badgeStyle.backgroundColor }]}>
                  <Text style={[styles.badgeText, { color: badgeStyle.color }]}>{statusLabel}</Text>
                </View>
              </View>

              {shipment.trackingNumber ? (
                <View style={styles.trackingRow}>
                  <Text style={styles.trackingLabel}>Mã vận đơn:</Text>
                  <Text style={styles.trackingValue}>{shipment.trackingNumber}</Text>
                </View>
              ) : null}

              <View style={styles.meta}>
                {shipment.estimatedDeliveryDate ? (
                  <Text style={styles.metaText}>
                    Dự kiến giao:{" "}
                    <Text style={styles.metaValue}>{shipment.estimatedDeliveryDate}</Text>
                  </Text>
                ) : null}
                {shipment.shippedAt ? (
                  <Text style={styles.metaText}>
                    Đã gửi: <Text style={styles.metaValue}>{formatOrderDate(shipment.shippedAt)}</Text>
                  </Text>
                ) : null}
                {shipment.deliveredAt ? (
                  <Text style={styles.metaText}>
                    Đã giao:{" "}
                    <Text style={styles.metaValue}>{formatOrderDate(shipment.deliveredAt)}</Text>
                  </Text>
                ) : null}
              </View>

              {shipment.shipmentId && orderId ? (
                <Pressable
                  style={styles.trackBtn}
                  onPress={() =>
                    router.push(ROUTES.commerceShipmentTracking(orderId, shipment.shipmentId))
                  }
                >
                  <Text style={styles.trackBtnText}>Theo dõi vận chuyển</Text>
                </Pressable>
              ) : null}
            </View>
          );
        })}
      </View>
    </View>
  );
}
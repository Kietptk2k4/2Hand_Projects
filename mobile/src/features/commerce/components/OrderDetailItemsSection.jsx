import { Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { ITEM_STATUS_LABELS } from "../constants/orderDetailConstants";
import { formatVndPrice } from "../utils/formatVndPrice";

function parseAttributes(attributesSnapshot) {
  if (!attributesSnapshot) return null;
  try {
    const parsed = JSON.parse(attributesSnapshot);
    if (!parsed || typeof parsed !== "object") return null;
    return Object.entries(parsed)
      .map(([key, value]) => `${key}: ${value}`)
      .join(" · ");
  } catch {
    return attributesSnapshot;
  }
}

function getItemStatusBadgeStyle(colors, status) {
  switch (status) {
    case "PROCESSING":
      return { backgroundColor: `${colors.primary}1A`, color: colors.primary };
    case "SHIPPED":
      return { backgroundColor: "#E0E7FF", color: "#312E81" };
    case "DELIVERED":
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
    section: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      overflow: "hidden",
    },
    header: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "center",
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      paddingHorizontal: 16,
      paddingVertical: 12,
    },
    title: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    count: { fontSize: 14, color: colors.onSurfaceVariant },
    body: { padding: 16, gap: 16 },
    item: {
      flexDirection: "row",
      gap: 12,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
      paddingBottom: 16,
    },
    itemLast: { borderBottomWidth: 0, paddingBottom: 0 },
    imageWrap: {
      width: 80,
      height: 80,
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
      overflow: "hidden",
      alignItems: "center",
      justifyContent: "center",
    },
    image: { width: 80, height: 80 },
    info: { flex: 1, minWidth: 0 },
    name: { fontSize: 14, fontWeight: "500", color: colors.onSurface },
    shop: { fontSize: 13, color: colors.onSurfaceVariant, marginTop: 2 },
    attrs: { fontSize: 13, color: colors.onSurfaceVariant, marginTop: 4 },
    meta: { flexDirection: "row", flexWrap: "wrap", gap: 12, marginTop: 8 },
    metaText: { fontSize: 14, color: colors.onSurface },
    price: { fontWeight: "600" },
    badge: { alignSelf: "flex-start", borderRadius: 12, paddingHorizontal: 10, paddingVertical: 3 },
    badgeText: { fontSize: 12, fontWeight: "500" },
    reviewBtn: {
      alignSelf: "flex-start",
      marginTop: 10,
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.primary,
      paddingHorizontal: 14,
      paddingVertical: 8,
    },
    reviewBtnText: { fontSize: 14, fontWeight: "600", color: colors.primary },
  };
}

export function OrderDetailItemsSection({ orderId, items }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!items?.length) return null;

  const handleWriteReview = (item) => {
    router.push({
      pathname: "/commerce/reviews/new",
      params: {
        orderItemId: String(item.orderItemId),
        ...(item.productId ? { productId: String(item.productId) } : {}),
        ...(orderId ? { orderId: String(orderId) } : {}),
      },
    });
  };

  const handleEditReview = (reviewId) => {
    router.push(ROUTES.commerceReviewEdit(reviewId));
  };

  return (
    <View style={styles.section}>
      <View style={styles.header}>
        <Text style={styles.title}>Sản phẩm đã đặt</Text>
        <Text style={styles.count}>{items.length} sản phẩm</Text>
      </View>

      <View style={styles.body}>
        {items.map((item, index) => {
          const statusLabel = ITEM_STATUS_LABELS[item.status] || item.status;
          const badgeStyle = getItemStatusBadgeStyle(colors, item.status);
          const attributesText = parseAttributes(item.attributesSnapshot);
          const imageUri = resolveDevMediaUrl(item.imageSnapshot);
          const canReview = item.status === "COMPLETED";
          const hasReview = Boolean(item.reviewId);
          const isLast = index === items.length - 1;

          return (
            <View key={item.orderItemId} style={[styles.item, isLast ? styles.itemLast : null]}>
              <View style={styles.imageWrap}>
                {imageUri ? (
                  <Image source={{ uri: imageUri }} style={styles.image} resizeMode="cover" />
                ) : (
                  <Ionicons name="cube-outline" size={28} color={colors.outline} />
                )}
              </View>

              <View style={styles.info}>
                <Text style={styles.name}>{item.productNameSnapshot}</Text>
                {item.shopNameSnapshot ? (
                  <Text style={styles.shop}>{item.shopNameSnapshot}</Text>
                ) : null}
                {attributesText ? <Text style={styles.attrs}>{attributesText}</Text> : null}
                <View style={styles.meta}>
                  <Text style={styles.metaText}>Số lượng: {item.quantity}</Text>
                  <Text style={[styles.metaText, styles.price]}>
                    {formatVndPrice(item.finalPrice)}
                  </Text>
                </View>

                {canReview && !hasReview ? (
                  <Pressable style={styles.reviewBtn} onPress={() => handleWriteReview(item)}>
                    <Text style={styles.reviewBtnText}>Viết đánh giá</Text>
                  </Pressable>
                ) : null}

                {canReview && hasReview ? (
                  <Pressable style={styles.reviewBtn} onPress={() => handleEditReview(item.reviewId)}>
                    <Text style={styles.reviewBtnText}>Sửa đánh giá</Text>
                  </Pressable>
                ) : null}
              </View>

              <View style={[styles.badge, { backgroundColor: badgeStyle.backgroundColor }]}>
                <Text style={[styles.badgeText, { color: badgeStyle.color }]}>{statusLabel}</Text>
              </View>
            </View>
          );
        })}
      </View>
    </View>
  );
}
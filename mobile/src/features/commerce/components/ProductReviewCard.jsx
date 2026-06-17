import { Image, ScrollView, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { formatReviewDate } from "../utils/formatReviewDate";

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
    header: { flexDirection: "row", gap: 12 },
    avatar: {
      width: 40,
      height: 40,
      borderRadius: 20,
      backgroundColor: colors.surfaceContainerHigh,
      alignItems: "center",
      justifyContent: "center",
    },
    headerBody: { flex: 1, gap: 4 },
    headerTop: { flexDirection: "row", justifyContent: "space-between", gap: 8 },
    buyer: { fontSize: 14, fontWeight: "600", color: colors.onSurface, flex: 1 },
    productName: {
      fontSize: 12,
      color: colors.onSurfaceVariant,
      marginTop: 2,
    },
    date: { fontSize: 12, color: colors.onSurfaceVariant },
    stars: { flexDirection: "row", gap: 2 },
    comment: { fontSize: 14, lineHeight: 20, color: colors.onSurface },
    mediaRow: { gap: 8 },
    mediaImage: {
      width: 88,
      height: 88,
      borderRadius: 10,
      backgroundColor: colors.surfaceContainerHigh,
    },
    replyCard: {
      marginTop: 4,
      borderRadius: 12,
      backgroundColor: colors.surfaceContainerLow,
      padding: 12,
      gap: 4,
    },
    replyLabel: { fontSize: 12, fontWeight: "600", color: colors.onSurfaceVariant },
    replyText: { fontSize: 13, color: colors.onSurface },
  };
}

function StarRow({ rating }) {
  const colors = useThemeColors();
  const rounded = Math.round(rating);

  return (
    <View style={{ flexDirection: "row", gap: 2 }}>
      {[1, 2, 3, 4, 5].map((star) => (
        <Ionicons
          key={star}
          name={star <= rounded ? "star" : "star-outline"}
          size={16}
          color={star <= rounded ? "#F59E0B" : colors.outlineVariant}
        />
      ))}
    </View>
  );
}

export function ProductReviewCard({ review, productName }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!review) return null;

  const mediaItems = (review.media || []).filter((item) => item?.url);
  const buyerLabel = review.buyerDisplayName || "Người mua";
  const resolvedProductName = productName || review.productName;

  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <View style={styles.avatar}>
          <Ionicons name="person" size={20} color={colors.onSurfaceVariant} />
        </View>
        <View style={styles.headerBody}>
          <View style={styles.headerTop}>
            <Text style={styles.buyer} numberOfLines={1}>
              {buyerLabel}
            </Text>
            <Text style={styles.date}>{formatReviewDate(review.createdAt)}</Text>
          </View>
          {resolvedProductName ? (
            <Text style={styles.productName} numberOfLines={1}>
              Sản phẩm: {resolvedProductName}
            </Text>
          ) : null}
          <View style={styles.stars}>
            <StarRow rating={review.rating} />
          </View>
        </View>
      </View>

      {review.comment ? <Text style={styles.comment}>{review.comment}</Text> : null}

      {mediaItems.length > 0 ? (
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.mediaRow}>
          {mediaItems.map((item) => {
            const uri = resolveDevMediaUrl(item.url);
            if (!uri) return null;
            return (
              <Image
                key={item.mediaId || item.url}
                source={{ uri }}
                style={styles.mediaImage}
                resizeMode="cover"
              />
            );
          })}
        </ScrollView>
      ) : null}

      {review.sellerReply?.content ? (
        <View style={styles.replyCard}>
          <Text style={styles.replyLabel}>Phản hồi từ shop</Text>
          <Text style={styles.replyText}>{review.sellerReply.content}</Text>
        </View>
      ) : null}
    </View>
  );
}

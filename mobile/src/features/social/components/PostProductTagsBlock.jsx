import { useCallback } from "react";
import { Alert, Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { formatVndPrice } from "../../commerce/utils/formatVndPrice";

function createStyles(colors) {
  return {
    compact: {
      marginTop: 12,
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 12,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      paddingHorizontal: 12,
      paddingVertical: 10,
    },
    compactLeft: { flexDirection: "row", alignItems: "center", gap: 10, flex: 1, minWidth: 0 },
    thumb: {
      width: 48,
      height: 48,
      borderRadius: 10,
      backgroundColor: colors.surfaceContainerHigh,
    },
    thumbPlaceholder: {
      width: 48,
      height: 48,
      borderRadius: 10,
      backgroundColor: colors.surfaceContainerHigh,
      alignItems: "center",
      justifyContent: "center",
    },
    title: { fontSize: 14, fontWeight: "600", color: colors.onSurface },
    subtitle: { fontSize: 12, color: colors.onSurfaceVariant, marginTop: 2 },
    viewBtn: {
      borderRadius: 10,
      backgroundColor: colors.primary,
      paddingHorizontal: 12,
      paddingVertical: 8,
    },
    viewBtnText: { fontSize: 12, fontWeight: "600", color: colors.onPrimary },
    detailWrap: { marginTop: 12, gap: 8 },
    detailLabel: {
      fontSize: 12,
      fontWeight: "600",
      color: colors.onSurfaceVariant,
      textTransform: "uppercase",
      letterSpacing: 0.5,
    },
    detailItem: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 12,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      padding: 12,
    },
    detailBody: { flex: 1, minWidth: 0, gap: 2 },
    detailName: { fontSize: 14, fontWeight: "700", color: colors.onSurface },
    detailCategory: { fontSize: 12, color: colors.onSurfaceVariant },
    detailPrice: { fontSize: 14, fontWeight: "600", color: colors.primary, marginTop: 4 },
  };
}

function handleViewProduct(tag, onViewProduct, showUnavailable) {
  if (!tag?.productId) return;
  if (tag.available === false) {
    showUnavailable();
    return;
  }
  onViewProduct?.(tag.productId);
}

function CompactStrip({ tags, onViewProduct, showUnavailable, styles }) {
  const first = tags[0];
  const count = tags.length;
  const label = count === 1 ? first.name : `${count} sản phẩm`;
  const priceLabel = count === 1 ? formatVndPrice(first.price) : `từ ${formatVndPrice(first.price)}`;

  return (
    <View style={styles.compact}>
      <View style={styles.compactLeft}>
        {first.imageUrl ? (
          <Image source={{ uri: resolveDevMediaUrl(first.imageUrl) }} style={styles.thumb} />
        ) : (
          <View style={styles.thumbPlaceholder}>
            <Ionicons name="cube-outline" size={20} color="#888" />
          </View>
        )}
        <View style={{ flex: 1, minWidth: 0 }}>
          <Text style={styles.title} numberOfLines={1}>
            {label}
          </Text>
          <Text style={styles.subtitle}>{priceLabel}</Text>
        </View>
      </View>
      <Pressable
        style={styles.viewBtn}
        onPress={() => handleViewProduct(first, onViewProduct, showUnavailable)}
      >
        <Text style={styles.viewBtnText}>Xem</Text>
      </Pressable>
    </View>
  );
}

function DetailList({ tags, onViewProduct, showUnavailable, styles }) {
  return (
    <View style={styles.detailWrap}>
      <Text style={styles.detailLabel}>Sản phẩm gắn kèm</Text>
      {tags.map((tag) => (
        <View key={tag.productId} style={styles.detailItem}>
          <View style={{ flexDirection: "row", alignItems: "center", gap: 12, flex: 1, minWidth: 0 }}>
            {tag.imageUrl ? (
              <Image source={{ uri: resolveDevMediaUrl(tag.imageUrl) }} style={styles.thumb} />
            ) : (
              <View style={styles.thumbPlaceholder}>
                <Ionicons name="cube-outline" size={20} color="#888" />
              </View>
            )}
            <View style={styles.detailBody}>
              <Text style={styles.detailName} numberOfLines={1}>
                {tag.name}
              </Text>
              {tag.category ? <Text style={styles.detailCategory}>{tag.category}</Text> : null}
              <Text style={styles.detailPrice}>{formatVndPrice(tag.price)}</Text>
            </View>
          </View>
          <Pressable
            style={styles.viewBtn}
            onPress={() => handleViewProduct(tag, onViewProduct, showUnavailable)}
          >
            <Text style={styles.viewBtnText}>Xem SP</Text>
          </Pressable>
        </View>
      ))}
    </View>
  );
}

export function PostProductTagsBlock({ tags = [], variant = "compact", onViewProduct }) {
  const styles = useThemedStyles(createStyles);

  const showUnavailable = useCallback(() => {
    Alert.alert("Sản phẩm không khả dụng", "Sản phẩm này hiện không còn hoạt động trên cửa hàng.");
  }, []);

  const defaultViewProduct = useCallback((productId) => {
    if (!productId) return;
    router.push(ROUTES.commerceProductDetail(productId));
  }, []);

  if (!tags.length) return null;

  const viewHandler = onViewProduct || defaultViewProduct;

  if (variant === "detail") {
    return <DetailList tags={tags} onViewProduct={viewHandler} showUnavailable={showUnavailable} styles={styles} />;
  }

  return (
    <CompactStrip tags={tags} onViewProduct={viewHandler} showUnavailable={showUnavailable} styles={styles} />
  );
}
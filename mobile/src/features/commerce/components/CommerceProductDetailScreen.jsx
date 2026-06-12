import { useCallback, useLayoutEffect, useRef, useState } from "react";
import {
  Dimensions,
  FlatList,
  Image,
  Pressable,
  ScrollView,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router, useLocalSearchParams, useNavigation } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { useProductDetail } from "../hooks/useProductDetail";
import { formatVndPrice } from "../utils/formatVndPrice";
import { CommerceProductListError } from "./CommerceProductListStates";
import { ProductDetailSkeleton } from "./ProductDetailSkeleton";

const CONDITION_LABELS = {
  LIKE_NEW: "Như mới",
  GOOD: "Tốt",
  FAIR: "Khá",
  USED: "Đã qua sử dụng",
};

const GALLERY_WIDTH = Dimensions.get("window").width;

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { paddingBottom: 32 },
    gallery: { width: GALLERY_WIDTH, height: 360, backgroundColor: colors.surfaceContainerLow },
    galleryImage: { width: GALLERY_WIDTH, height: 360 },
    galleryPlaceholder: {
      flex: 1,
      alignItems: "center",
      justifyContent: "center",
    },
    placeholderText: { fontSize: 14, color: colors.outline },
    dots: {
      flexDirection: "row",
      justifyContent: "center",
      gap: 6,
      paddingVertical: 10,
    },
    dot: {
      width: 8,
      height: 8,
      borderRadius: 4,
      backgroundColor: colors.outlineVariant,
    },
    dotActive: { backgroundColor: colors.primary, width: 18 },
    section: { paddingHorizontal: 16, paddingTop: 16, gap: 16 },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface },
    metaRow: { flexDirection: "row", flexWrap: "wrap", alignItems: "center", gap: 8 },
    ratingLink: { flexDirection: "row", alignItems: "center", gap: 4 },
    ratingText: { fontSize: 14, color: colors.primary, fontWeight: "500" },
    metaMuted: { fontSize: 14, color: colors.onSurfaceVariant },
    priceRow: { flexDirection: "row", alignItems: "flex-end", gap: 10 },
    price: { fontSize: 24, fontWeight: "700", color: colors.onSurface },
    priceSale: { color: colors.error },
    priceStrike: {
      fontSize: 16,
      color: colors.outlineVariant,
      textDecorationLine: "line-through",
    },
    vacationBanner: {
      borderRadius: 12,
      backgroundColor: colors.secondaryContainer,
      padding: 12,
    },
    vacationText: { fontSize: 14, color: colors.onSecondaryContainer },
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 12,
    },
    cardTitle: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    shopRow: { flexDirection: "row", alignItems: "center", gap: 12 },
    shopAvatar: {
      width: 48,
      height: 48,
      borderRadius: 24,
      backgroundColor: colors.surfaceContainerHigh,
    },
    shopName: { fontSize: 15, fontWeight: "600", color: colors.primary, flex: 1 },
    attributeRow: { flexDirection: "row", justifyContent: "space-between", gap: 12 },
    attributeName: { fontSize: 14, color: colors.onSurfaceVariant },
    attributeValue: { fontSize: 14, color: colors.onSurface, fontWeight: "500" },
    description: { fontSize: 14, lineHeight: 22, color: colors.onSurface },
    actions: { flexDirection: "row", gap: 10 },
    buyButton: {
      flex: 1,
      alignItems: "center",
      justifyContent: "center",
      borderRadius: 10,
      paddingVertical: 14,
      backgroundColor: colors.primary,
    },
    buyButtonDisabled: { opacity: 0.5 },
    buyButtonText: { fontSize: 15, fontWeight: "600", color: colors.onPrimary },
    cartButton: {
      alignItems: "center",
      justifyContent: "center",
      borderRadius: 10,
      paddingHorizontal: 18,
      paddingVertical: 14,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
    },
    reviewsTeaser: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
    reviewsLink: { fontSize: 14, fontWeight: "600", color: colors.primary },
    notFoundCard: {
      margin: 16,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
      gap: 12,
    },
    homeButton: {
      paddingHorizontal: 16,
      paddingVertical: 10,
      borderRadius: 10,
      backgroundColor: colors.primary,
    },
    homeButtonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
  };
}

function resolveParam(raw) {
  if (typeof raw === "string") return raw;
  if (Array.isArray(raw)) return raw[0] ?? "";
  return "";
}

function isProductOnSale(product) {
  return (
    product?.salePrice != null &&
    product?.price != null &&
    Number(product.salePrice) < Number(product.price)
  );
}

function isProductOutOfStock(product) {
  return !product?.inventorySummary?.inStock || product?.status === "OUT_OF_STOCK";
}

function getStockLabel(product) {
  const inv = product?.inventorySummary;
  if (!inv) return "";
  if (!inv.inStock || product?.status === "OUT_OF_STOCK") return "Hết hàng";
  if (inv.lowStock) return `Sắp hết hàng · còn ${inv.stockQuantity ?? 0}`;
  return "Còn hàng";
}

export function CommerceProductDetailScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const { productId: rawProductId } = useLocalSearchParams();
  const productId = resolveParam(rawProductId);
  const navigation = useNavigation();
  const [galleryIndex, setGalleryIndex] = useState(0);
  const galleryRef = useRef(null);

  const { product, isLoading, isNotFound, isError, errorMessage, retry } = useProductDetail(productId);

  const { addToCart, isAddingProduct } = useCommerceAddToCart({
    onSuccess: (message) => showToast(message),
    onError: (message) => showToast(message, "error"),
  });

  const { buyNow, isBuyingProduct } = useCommerceBuyNow({
    onError: (message) => showToast(message, "error"),
  });

  useLayoutEffect(() => {
    if (product?.title) {
      navigation.setOptions({ title: product.title });
    }
  }, [navigation, product?.title]);

  const openShop = useCallback((shopId) => {
    if (!shopId) return;
    router.push(ROUTES.commerceShopProducts(shopId));
  }, []);

  const openReviews = useCallback(() => {
    if (!productId) return;
    router.push(ROUTES.commerceProductReviews(productId));
  }, [productId]);

  const galleryItems = (product?.media || []).filter((item) => item.mediaType === "IMAGE");
  const isOnSale = isProductOnSale(product);
  const isOutOfStock = isProductOutOfStock(product);
  const actionsDisabled = isOutOfStock || Boolean(product?.shopVacation);
  const stockLabel = getStockLabel(product);

  const handleGalleryScroll = useCallback((event) => {
    const index = Math.round(event.nativeEvent.contentOffset.x / GALLERY_WIDTH);
    setGalleryIndex(index);
  }, []);

  if (isLoading) {
    return (
      <ScrollView style={styles.container}>
        <ProductDetailSkeleton />
      </ScrollView>
    );
  }

  if (isNotFound) {
    return (
      <ScrollView style={styles.container} contentContainerStyle={{ padding: 16 }}>
        <View style={styles.notFoundCard}>
          <Ionicons name="cube-outline" size={40} color={colors.outline} />
          <Text style={styles.metaMuted}>{errorMessage || "Sản phẩm không tồn tại."}</Text>
          <Pressable style={styles.homeButton} onPress={() => router.push(ROUTES.commerceHome)}>
            <Text style={styles.homeButtonText}>Về trang Commerce</Text>
          </Pressable>
        </View>
      </ScrollView>
    );
  }

  if (isError || !product) {
    return (
      <ScrollView style={styles.container} contentContainerStyle={{ padding: 16 }}>
        <CommerceProductListError message={errorMessage} onRetry={retry} />
      </ScrollView>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {galleryItems.length > 0 ? (
        <>
          <FlatList
            ref={galleryRef}
            data={galleryItems}
            horizontal
            pagingEnabled
            showsHorizontalScrollIndicator={false}
            keyExtractor={(item) => item.mediaId || item.mediaUrl}
            onMomentumScrollEnd={handleGalleryScroll}
            renderItem={({ item }) => {
              const uri = resolveDevMediaUrl(item.mediaUrl);
              return uri ? (
                <Image source={{ uri }} style={styles.galleryImage} resizeMode="cover" />
              ) : (
                <View style={[styles.gallery, styles.galleryPlaceholder]}>
                  <Text style={styles.placeholderText}>Không có ảnh</Text>
                </View>
              );
            }}
          />
          {galleryItems.length > 1 ? (
            <View style={styles.dots}>
              {galleryItems.map((item, index) => (
                <View
                  key={item.mediaId || index}
                  style={[styles.dot, index === galleryIndex && styles.dotActive]}
                />
              ))}
            </View>
          ) : null}
        </>
      ) : (
        <View style={[styles.gallery, styles.galleryPlaceholder]}>
          <Text style={styles.placeholderText}>Không có ảnh</Text>
        </View>
      )}

      <View style={styles.section}>
        {product.shopVacation ? (
          <View style={styles.vacationBanner}>
            <Text style={styles.vacationText}>
              {product.vacationMessage || "Shop đang nghỉ — không thể đặt hàng lúc này."}
            </Text>
          </View>
        ) : null}

        <Text style={styles.title}>{product.title}</Text>

        <View style={styles.metaRow}>
          <Pressable style={styles.ratingLink} onPress={openReviews}>
            <Ionicons name="star" size={16} color="#F59E0B" />
            <Text style={styles.ratingText}>
              {product.ratingCount > 0
                ? `${product.ratingAvg} · ${product.ratingCount} đánh giá`
                : "Chưa có đánh giá · Xem trang đánh giá"}
            </Text>
          </Pressable>
          {stockLabel ? (
            <>
              <Text style={styles.metaMuted}>·</Text>
              <Text style={styles.metaMuted}>{stockLabel}</Text>
            </>
          ) : null}
        </View>

        <View style={styles.priceRow}>
          <Text style={[styles.price, isOnSale && styles.priceSale]}>
            {formatVndPrice(product.effectivePrice)}
          </Text>
          {isOnSale ? <Text style={styles.priceStrike}>{formatVndPrice(product.price)}</Text> : null}
        </View>

        <View style={styles.actions}>
          <Pressable
            style={[styles.buyButton, actionsDisabled && styles.buyButtonDisabled]}
            disabled={actionsDisabled || isBuyingProduct(product.productId)}
            onPress={() => buyNow(product.productId, 1)}
          >
            <Text style={styles.buyButtonText}>
              {isBuyingProduct(product.productId) ? "..." : "Mua ngay"}
            </Text>
          </Pressable>
          <Pressable
            style={[styles.cartButton, actionsDisabled && styles.buyButtonDisabled]}
            disabled={actionsDisabled || isAddingProduct(product.productId)}
            onPress={() => addToCart(product.productId, 1)}
            accessibilityLabel="Thêm vào giỏ"
          >
            <Ionicons name="cart-outline" size={22} color={colors.onSurface} />
          </Pressable>
        </View>

        {product.shop ? (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Cửa hàng</Text>
            <Pressable style={styles.shopRow} onPress={() => openShop(product.shop.shopId)}>
              {product.shop.avatarUrl ? (
                <Image
                  source={{ uri: resolveDevMediaUrl(product.shop.avatarUrl) }}
                  style={styles.shopAvatar}
                />
              ) : (
                <View style={[styles.shopAvatar, { alignItems: "center", justifyContent: "center" }]}>
                  <Ionicons name="storefront-outline" size={22} />
                </View>
              )}
              <Text style={styles.shopName} numberOfLines={1}>
                {product.shop.shopName}
              </Text>
              <Ionicons name="chevron-forward" size={18} color={colors.onSurfaceVariant} />
            </Pressable>
          </View>
        ) : null}

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Thông tin sản phẩm</Text>
          {product.condition ? (
            <View style={styles.attributeRow}>
              <Text style={styles.attributeName}>Tình trạng</Text>
              <Text style={styles.attributeValue}>
                {CONDITION_LABELS[product.condition] || product.condition}
              </Text>
            </View>
          ) : null}
          {product.category?.name ? (
            <View style={styles.attributeRow}>
              <Text style={styles.attributeName}>Danh mục</Text>
              <Text style={styles.attributeValue}>{product.category.name}</Text>
            </View>
          ) : null}
          {(product.attributes || []).map((attr) => (
            <View key={`${attr.attributeName}-${attr.attributeValue}`} style={styles.attributeRow}>
              <Text style={styles.attributeName}>{attr.attributeName}</Text>
              <Text style={styles.attributeValue}>{attr.attributeValue}</Text>
            </View>
          ))}
        </View>

        {product.description ? (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>Mô tả</Text>
            <Text style={styles.description}>{product.description}</Text>
          </View>
        ) : null}

        <Pressable style={[styles.card, styles.reviewsTeaser]} onPress={openReviews}>
          <View>
            <Text style={styles.cardTitle}>Đánh giá</Text>
            <Text style={styles.metaMuted}>
              {product.ratingCount > 0
                ? `${product.ratingAvg} · ${product.ratingCount} đánh giá`
                : "Chưa có đánh giá"}
            </Text>
          </View>
          <Text style={styles.reviewsLink}>Xem tất cả</Text>
        </Pressable>
      </View>
    </ScrollView>
  );
}

import { Image, Pressable, Text, View } from "react-native";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { formatVndPrice } from "../utils/formatVndPrice";
import { ProductImageStickers } from "./ProductImageStickers";

function createStyles(colors) {
  return {
    card: {
      flex: 1,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      overflow: "hidden",
    },
    imageWrap: {
      height: 160,
      backgroundColor: colors.surfaceContainerLow,
      position: "relative",
    },
    image: {
      width: "100%",
      height: "100%",
    },
    imagePlaceholder: {
      flex: 1,
      alignItems: "center",
      justifyContent: "center",
    },
    placeholderText: {
      fontSize: 12,
      color: colors.outline,
    },
    body: {
      flex: 1,
      padding: 12,
      gap: 6,
    },
    title: {
      fontSize: 15,
      fontWeight: "600",
      color: colors.onSurface,
      minHeight: 40,
    },
    ratingRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 4,
    },
    ratingText: {
      fontSize: 12,
      color: colors.onSurface,
    },
    ratingMuted: {
      fontSize: 12,
      color: colors.onSurfaceVariant,
    },
    shopRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 4,
    },
    shopText: {
      fontSize: 12,
      color: colors.onSurfaceVariant,
      flex: 1,
    },
    shopLink: {
      color: colors.primary,
    },
    vacation: {
      fontSize: 11,
      color: colors.onSurfaceVariant,
    },
    footer: {
      marginTop: "auto",
      paddingTop: 12,
      borderTopWidth: 1,
      borderTopColor: colors.surfaceContainerHigh,
      gap: 8,
    },
    priceStrike: {
      fontSize: 12,
      color: colors.outlineVariant,
      textDecorationLine: "line-through",
    },
    price: {
      fontSize: 16,
      fontWeight: "700",
      color: colors.onSurface,
    },
    priceSale: {
      color: colors.error,
    },
    actions: {
      flexDirection: "row",
      gap: 8,
    },
    buyButton: {
      flex: 1,
      alignItems: "center",
      justifyContent: "center",
      borderRadius: 8,
      paddingVertical: 8,
      backgroundColor: colors.primary,
    },
    buyButtonDisabled: {
      opacity: 0.5,
    },
    buyButtonText: {
      fontSize: 13,
      fontWeight: "600",
      color: colors.onPrimary,
    },
    cartButton: {
      alignItems: "center",
      justifyContent: "center",
      borderRadius: 8,
      paddingHorizontal: 12,
      paddingVertical: 8,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
    },
    cartButtonText: {
      fontSize: 18,
      color: colors.onSurface,
    },
  };
}

export function ProductCard({
  product,
  onOpenProduct,
  onOpenShop,
  onAddToCart,
  onBuyNow,
  isAddingToCart = false,
  isBuyingNow = false,
  disabledActions = false,
}) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);

  const isOnSale =
    product.salePrice != null &&
    product.price != null &&
    Number(product.salePrice) < Number(product.price);
  const isOutOfStock = !product.inStock || product.status === "OUT_OF_STOCK";
  const actionsDisabled = disabledActions || isOutOfStock || isAddingToCart || isBuyingNow;
  const thumbnailUrl = resolveDevMediaUrl(product.thumbnailUrl);
  const canOpenProduct = Boolean(product?.productId && onOpenProduct);

  const handleOpen = () => {
    if (!canOpenProduct) return;
    onOpenProduct(product.productId);
  };

  return (
    <Pressable
      style={styles.card}
      onPress={handleOpen}
      disabled={!canOpenProduct}
      accessibilityRole={canOpenProduct ? "button" : undefined}
      accessibilityLabel={canOpenProduct ? `Xem chi tiết ${product.title}` : undefined}
    >
      <View style={styles.imageWrap}>
        {thumbnailUrl ? (
          <Image source={{ uri: thumbnailUrl }} style={styles.image} resizeMode="cover" />
        ) : (
          <View style={styles.imagePlaceholder}>
            <Text style={styles.placeholderText}>Không có ảnh</Text>
          </View>
        )}
        <ProductImageStickers
          isOnSale={isOnSale}
          isOutOfStock={isOutOfStock}
          lowStock={product.lowStock}
        />
      </View>

      <View style={styles.body}>
        <Text style={styles.title} numberOfLines={2}>
          {product.title}
        </Text>

        {product.ratingCount > 0 ? (
          <View style={styles.ratingRow}>
            <Text style={styles.ratingText}>{product.ratingAvg}</Text>
            <Text style={styles.ratingMuted}>· {product.ratingCount} đánh giá</Text>
          </View>
        ) : null}

        <View style={styles.shopRow}>
          {onOpenShop && product.shopId ? (
            <Pressable onPress={() => onOpenShop(product.shopId)} hitSlop={8}>
              <Text style={[styles.shopText, styles.shopLink]} numberOfLines={1}>
                {product.shopName}
              </Text>
            </Pressable>
          ) : (
            <Text style={styles.shopText} numberOfLines={1}>
              {product.shopName}
            </Text>
          )}
        </View>

        {product.shopVacation ? (
          <Text style={styles.vacation}>{product.vacationMessage || "Shop đang nghỉ"}</Text>
        ) : null}

        <View style={styles.footer}>
          {isOnSale ? (
            <Text style={styles.priceStrike}>{formatVndPrice(product.price)}</Text>
          ) : null}
          <Text style={[styles.price, isOnSale && styles.priceSale]}>
            {formatVndPrice(product.effectivePrice)}
          </Text>

          <View style={styles.actions}>
            <Pressable
              style={[styles.buyButton, actionsDisabled && styles.buyButtonDisabled]}
              disabled={actionsDisabled}
              onPress={() => !actionsDisabled && onBuyNow?.(product.productId)}
            >
              <Text style={styles.buyButtonText}>{isBuyingNow ? "..." : "Mua ngay"}</Text>
            </Pressable>
            <Pressable
              style={[styles.cartButton, actionsDisabled && styles.buyButtonDisabled]}
              disabled={actionsDisabled}
              onPress={() => !actionsDisabled && onAddToCart?.(product.productId, 1)}
              accessibilityLabel={isAddingToCart ? "Đang thêm vào giỏ" : "Thêm vào giỏ"}
            >
              <Text style={styles.cartButtonText}>+</Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Pressable>
  );
}

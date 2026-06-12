import { StyleSheet, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { ProductStickerBadge } from "./ProductStickerBadge";

function createStyles(colors) {
  return {
    overlay: {
      ...StyleSheet.absoluteFillObject,
      backgroundColor: "rgba(23,23,23,0.2)",
      zIndex: 5,
    },
    leftStack: {
      position: "absolute",
      left: 0,
      top: 0,
      zIndex: 10,
      padding: 8,
      gap: 6,
      alignItems: "flex-start",
    },
    rightStack: {
      position: "absolute",
      right: 0,
      top: 0,
      zIndex: 10,
      padding: 8,
    },
  };
}

export function ProductImageStickers({
  isOnSale = false,
  isOutOfStock = false,
  lowStock = false,
  conditionLabel = null,
}) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const hasLeftStickers = Boolean(conditionLabel) || isOnSale || (!isOutOfStock && lowStock);

  return (
    <>
      {isOutOfStock ? <View style={styles.overlay} pointerEvents="none" /> : null}

      {hasLeftStickers ? (
        <View style={styles.leftStack} pointerEvents="none">
          {conditionLabel ? (
            <ProductStickerBadge variant="condition">{conditionLabel}</ProductStickerBadge>
          ) : null}
          {isOnSale ? (
            <ProductStickerBadge variant="sale">Giảm giá</ProductStickerBadge>
          ) : null}
          {!isOutOfStock && lowStock ? (
            <ProductStickerBadge variant="lowStock">Sắp hết</ProductStickerBadge>
          ) : null}
        </View>
      ) : null}

      {isOutOfStock ? (
        <View style={styles.rightStack} pointerEvents="none">
          <ProductStickerBadge variant="soldOut">Hết hàng</ProductStickerBadge>
        </View>
      ) : null}
    </>
  );
}

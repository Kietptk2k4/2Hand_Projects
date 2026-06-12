import { FlatList, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { ProductCard } from "./ProductCard";
import {
  CommerceProductListFooter,
} from "./CommerceProductListStates";

function createStyles() {
  return {
    list: {
      paddingHorizontal: 16,
      paddingBottom: 8,
    },
    column: {
      justifyContent: "space-between",
      gap: 12,
    },
    item: {
      flex: 1,
      maxWidth: "48%",
    },
  };
}

export function CommerceProductGrid({
  items,
  onOpenProduct,
  onOpenShop,
  onAddToCart,
  onBuyNow,
  isAddingProduct,
  isBuyingProduct,
  isLoadingMore = false,
  hasNext = false,
  onLoadMore,
  onEndReachedThreshold = 0.4,
  ListHeaderComponent = null,
  scrollEnabled = true,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  const renderItem = ({ item }) => (
    <View style={styles.item}>
      <ProductCard
        product={item}
        onOpenProduct={onOpenProduct}
        onOpenShop={onOpenShop}
        onAddToCart={onAddToCart}
        onBuyNow={onBuyNow}
        isAddingToCart={isAddingProduct?.(item.productId)}
        isBuyingNow={isBuyingProduct?.(item.productId)}
      />
    </View>
  );

  const handleEndReached = () => {
    if (hasNext && !isLoadingMore) onLoadMore?.();
  };

  return (
    <FlatList
      data={items}
      keyExtractor={(item) => item.productId}
      numColumns={2}
      renderItem={renderItem}
      columnWrapperStyle={styles.column}
      contentContainerStyle={styles.list}
      onEndReached={handleEndReached}
      onEndReachedThreshold={onEndReachedThreshold}
      ListHeaderComponent={ListHeaderComponent}
      scrollEnabled={scrollEnabled}
      ListFooterComponent={
        <CommerceProductListFooter
          isLoadingMore={isLoadingMore}
          hasNext={hasNext}
          onLoadMore={onLoadMore}
          colors={colors}
        />
      }
    />
  );
}

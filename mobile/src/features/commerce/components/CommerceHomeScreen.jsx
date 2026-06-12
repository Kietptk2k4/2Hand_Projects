import { useCallback } from "react";
import { ScrollView, Text, View } from "react-native";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { MIN_KEYWORD_LENGTH } from "../constants/productSearchConstants";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { useCommerceCategories } from "../hooks/useCommerceCategories";
import { useProductList } from "../hooks/useProductList";
import { normalizeSearchKeyword } from "../utils/normalizeSearchKeyword";
import { CommerceHomeHero } from "./CommerceHomeHero";
import { CommerceProductGrid } from "./CommerceProductGrid";
import {
  CommerceProductListEmpty,
  CommerceProductListError,
} from "./CommerceProductListStates";
import { ProductListSkeleton } from "./ProductListSkeleton";
import { ProductListSortSelect } from "./ProductListSortSelect";

function createStyles(colors) {
  return {
    container: {
      flex: 1,
      backgroundColor: colors.surface,
    },
    content: {
      padding: 16,
      gap: 16,
    },
    sectionHeader: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 12,
      flexWrap: "wrap",
    },
    sectionTitle: {
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
    },
  };
}

export function CommerceHomeScreen() {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();

  const {
    items,
    sort,
    changeSort,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useProductList();

  const { homeNavItems, isLoading: isLoadingCategories } = useCommerceCategories({
    minLevel: 1,
    maxLevel: 1,
  });

  const { addToCart, isAddingProduct } = useCommerceAddToCart({
    onSuccess: (message) => showToast(message),
    onError: (message) => showToast(message, "error"),
  });

  const { buyNow, isBuyingProduct } = useCommerceBuyNow({
    onError: (message) => showToast(message, "error"),
  });

  const openProduct = useCallback((productId) => {
    if (!productId) return;
    router.push(ROUTES.commerceProductDetail(productId));
  }, []);

  const openShop = useCallback((shopId) => {
    if (!shopId) return;
    router.push(ROUTES.commerceShopProducts(shopId));
  }, []);

  const navigateToCategory = useCallback((item) => {
    if (!item?.categoryId) return;
    router.push(ROUTES.commerceCategoryProducts(item.categoryId));
  }, []);

  const handleSearchSubmit = useCallback(
    (rawQuery) => {
      const normalized = normalizeSearchKeyword(rawQuery);
      if (!normalized) return;
      if (normalized.length < MIN_KEYWORD_LENGTH) {
        showToast("Nhập ít nhất 2 ký tự.", "error");
        return;
      }
      router.push({ pathname: ROUTES.commerceSearch, params: { q: normalized } });
    },
    [showToast]
  );

  const renderHeader = () => (
    <View style={styles.content}>
      <CommerceHomeHero
        onSearchSubmit={handleSearchSubmit}
        onCategoryClick={navigateToCategory}
        navItems={homeNavItems}
        isLoadingNav={isLoadingCategories}
      />

      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>Sản phẩm nổi bật</Text>
        <ProductListSortSelect value={sort} onChange={changeSort} disabled={isInitialLoading} />
      </View>

      {isInitialLoading ? <ProductListSkeleton count={6} /> : null}
      {!isInitialLoading && errorMessage ? (
        <CommerceProductListError message={errorMessage} onRetry={retry} />
      ) : null}
      {!isInitialLoading && !errorMessage && items.length === 0 ? (
        <CommerceProductListEmpty />
      ) : null}
    </View>
  );

  const showGrid = !isInitialLoading && !errorMessage && items.length > 0;

  if (showGrid) {
    return (
      <View style={styles.container}>
        <CommerceProductGrid
          items={items}
          onOpenProduct={openProduct}
          onOpenShop={openShop}
          onAddToCart={addToCart}
          onBuyNow={buyNow}
          isAddingProduct={isAddingProduct}
          isBuyingProduct={isBuyingProduct}
          isLoadingMore={isLoadingMore}
          hasNext={hasNext}
          onLoadMore={loadMore}
          ListHeaderComponent={renderHeader}
        />
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {renderHeader()}
    </ScrollView>
  );
}

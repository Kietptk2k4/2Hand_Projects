import { useCallback, useLayoutEffect } from "react";
import { Pressable, ScrollView, Text, View } from "react-native";
import { router, useLocalSearchParams, useNavigation } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { useCategoryProducts } from "../hooks/useCategoryProducts";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { CategoryProductsHeader } from "./CategoryProductsHeader";
import { CommerceProductGrid } from "./CommerceProductGrid";
import {
  CommerceProductListEmpty,
  CommerceProductListError,
} from "./CommerceProductListStates";
import { ProductListSkeleton } from "./ProductListSkeleton";

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
    notFoundCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
      gap: 12,
    },
    notFoundText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
    },
    homeButton: {
      paddingHorizontal: 16,
      paddingVertical: 10,
      borderRadius: 10,
      backgroundColor: colors.primary,
    },
    homeButtonText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onPrimary,
    },
  };
}

function resolveCategoryId(raw) {
  if (typeof raw === "string") return raw;
  if (Array.isArray(raw)) return raw[0] ?? "";
  return "";
}

export function CommerceCategoryProductsScreen() {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const { categoryId: rawCategoryId } = useLocalSearchParams();
  const categoryId = resolveCategoryId(rawCategoryId);

  const {
    items,
    category,
    pagination,
    sort,
    includeChildren,
    changeSort,
    changeIncludeChildren,
    isInitialLoading,
    isLoadingMore,
    isNotFound,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useCategoryProducts(categoryId);
  const navigation = useNavigation();

  useLayoutEffect(() => {
    if (isNotFound) {
      navigation.setOptions({ title: "Danh mục không tồn tại" });
      return;
    }
    if (category?.categoryName) {
      navigation.setOptions({ title: category.categoryName });
    }
  }, [category?.categoryName, isNotFound, navigation]);

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

  const showGrid = !isInitialLoading && !errorMessage && items.length > 0;

  const renderHeader = () => (
    <View style={styles.content}>
      {!isNotFound ? (
        <CategoryProductsHeader
          categoryName={category?.categoryName || "..."}
          categorySlug={category?.categorySlug}
          totalItems={pagination?.totalItems}
          sort={sort}
          onSortChange={changeSort}
          includeChildren={includeChildren}
          onIncludeChildrenChange={changeIncludeChildren}
          sortDisabled={isInitialLoading}
        />
      ) : null}

      {isInitialLoading ? <ProductListSkeleton count={6} /> : null}
      {!isInitialLoading && errorMessage && !isNotFound ? (
        <CommerceProductListError message={errorMessage} onRetry={retry} />
      ) : null}
      {!isInitialLoading && !errorMessage && items.length === 0 && !isNotFound ? (
        <CommerceProductListEmpty message="Chưa có sản phẩm trong danh mục này." />
      ) : null}
    </View>
  );

  if (isNotFound) {
    return (
      <ScrollView style={styles.container} contentContainerStyle={styles.content}>
        <View style={styles.notFoundCard}>
          <Text style={styles.notFoundText}>{errorMessage || "Danh mục không tồn tại."}</Text>
          <Pressable style={styles.homeButton} onPress={() => router.push(ROUTES.commerceHome)}>
            <Text style={styles.homeButtonText}>Về trang Commerce</Text>
          </Pressable>
        </View>
      </ScrollView>
    );
  }

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

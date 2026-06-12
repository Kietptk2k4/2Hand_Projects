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
import { useProductSearch } from "../hooks/useProductSearch";
import { CommerceProductGrid } from "./CommerceProductGrid";
import {
  CommerceProductListEmpty,
  CommerceProductListError,
} from "./CommerceProductListStates";
import { CommerceSearchBar } from "./CommerceSearchBar";
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
    messageCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
      gap: 8,
    },
    messageText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
    },
    header: {
      gap: 8,
    },
    title: {
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
    },
    subtitle: {
      fontSize: 13,
      color: colors.onSurfaceVariant,
    },
    sortRow: {
      alignItems: "flex-end",
    },
  };
}

export function CommerceSearchScreen() {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();

  const {
    q,
    keyword,
    items,
    sort,
    changeSort,
    isQueryTooShort,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    totalItems,
    loadMore,
    retry,
  } = useProductSearch();

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

  const handleInvalidKeyword = useCallback(() => {
    showToast("Nhập ít nhất 2 ký tự.", "error");
  }, [showToast]);

  const displayKeyword = keyword || q;
  const emptyQuery = !q;
  const emptyResults =
    !emptyQuery && !isQueryTooShort && !isInitialLoading && !errorMessage && items.length === 0;
  const showResults = !emptyQuery && !isQueryTooShort;
  const showGrid = showResults && !isInitialLoading && !errorMessage && items.length > 0;

  const renderHeader = () => (
    <View style={styles.content}>
      <CommerceSearchBar onInvalidKeyword={handleInvalidKeyword} />

      {emptyQuery ? (
        <View style={styles.messageCard}>
          <Text style={styles.messageText}>Nhập từ khóa để tìm sản phẩm</Text>
        </View>
      ) : null}

      {isQueryTooShort ? (
        <View style={styles.messageCard}>
          <Text style={styles.messageText}>
            Nhập ít nhất {MIN_KEYWORD_LENGTH} ký tự để tìm kiếm.
          </Text>
        </View>
      ) : null}

      {showResults ? (
        <>
          <View style={styles.header}>
            <Text style={styles.title}>Kết quả tìm kiếm cho "{displayKeyword}"</Text>
            {totalItems > 0 ? (
              <Text style={styles.subtitle}>
                Hiển thị {items.length} / {totalItems} sản phẩm
              </Text>
            ) : null}
            <View style={styles.sortRow}>
              <ProductListSortSelect value={sort} onChange={changeSort} disabled={isInitialLoading} />
            </View>
          </View>

          {isInitialLoading ? <ProductListSkeleton count={6} /> : null}
          {!isInitialLoading && errorMessage ? (
            <CommerceProductListError message={errorMessage} onRetry={retry} />
          ) : null}
          {emptyResults ? (
            <CommerceProductListEmpty
              message={`Không tìm thấy sản phẩm phù hợp với "${displayKeyword}".`}
            />
          ) : null}
        </>
      ) : null}
    </View>
  );

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

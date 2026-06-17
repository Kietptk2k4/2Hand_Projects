import { useCallback, useLayoutEffect } from "react";
import { Pressable, ScrollView, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router, useLocalSearchParams, useNavigation } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { useCommerceAddToCart } from "../hooks/useCommerceAddToCart";
import { useCommerceBuyNow } from "../hooks/useCommerceBuyNow";
import { useShopProducts } from "../hooks/useShopProducts";
import { CommerceProductGrid } from "./CommerceProductGrid";
import {
  CommerceProductListEmpty,
  CommerceProductListError,
} from "./CommerceProductListStates";
import { ProductListSkeleton } from "./ProductListSkeleton";
import { ShopProductsHeader } from "./ShopProductsHeader";
import { ShopStorefrontHero, ShopStorefrontHeroSkeleton } from "./ShopStorefrontHero";
import { ShopVacationBanner } from "./ShopVacationBanner";

function createStyles(colors) {
  return {
    container: {
      flex: 1,
      backgroundColor: colors.surface,
    },
    content: {
      padding: 16,
      gap: 0,
    },
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

function resolveShopId(raw) {
  if (typeof raw === "string") return raw;
  if (Array.isArray(raw)) return raw[0] ?? "";
  return "";
}

export function CommerceShopProductsScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const navigation = useNavigation();
  const { shopId: rawShopId } = useLocalSearchParams();
  const shopId = resolveShopId(rawShopId);

  const {
    items,
    shop,
    pagination,
    sort,
    changeSort,
    isInitialLoading,
    isLoadingMore,
    isNotFound,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useShopProducts(shopId);

  useLayoutEffect(() => {
    if (isNotFound) {
      navigation.setOptions({ title: "Shop không tồn tại" });
      return;
    }
    if (shop?.shopName) {
      navigation.setOptions({ title: shop.shopName });
    }
  }, [isNotFound, navigation, shop?.shopName]);

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

  const openShop = useCallback((targetShopId) => {
    if (!targetShopId) return;
    router.push(ROUTES.commerceShopProducts(targetShopId));
  }, []);

  const openShopReviews = useCallback(() => {
    if (!shopId) return;
    router.push(ROUTES.commerceShopReviews(shopId));
  }, [shopId]);

  const showGrid = !isInitialLoading && !errorMessage && items.length > 0;
  const vacationDisabled = Boolean(shop?.shopVacation);

  const renderHeader = () => (
    <View style={styles.content}>
      {isInitialLoading ? <ShopStorefrontHeroSkeleton /> : null}
      {!isInitialLoading && shop ? (
        <ShopStorefrontHero shop={shop} onViewReviews={openShopReviews} />
      ) : null}
      {!isInitialLoading && shop?.shopVacation ? (
        <ShopVacationBanner
          message={shop.vacationMessage || "Shop đang nghỉ — đơn hàng sẽ được xử lý sau."}
        />
      ) : null}
      {!isInitialLoading && shop ? (
        <ShopProductsHeader
          totalItems={pagination?.totalItems}
          sort={sort}
          onSortChange={changeSort}
          sortDisabled={isInitialLoading}
        />
      ) : null}

      {isInitialLoading ? <ProductListSkeleton count={6} /> : null}
      {!isInitialLoading && errorMessage && !isNotFound ? (
        <CommerceProductListError message={errorMessage} onRetry={retry} />
      ) : null}
      {!isInitialLoading && !errorMessage && shop && items.length === 0 ? (
        <CommerceProductListEmpty message="Shop chưa có sản phẩm nào để hiển thị." />
      ) : null}
    </View>
  );

  if (isNotFound) {
    return (
      <ScrollView style={styles.container} contentContainerStyle={styles.content}>
        <View style={styles.notFoundCard}>
          <Ionicons name="storefront-outline" size={40} color={colors.outline} />
          <Text style={styles.notFoundText}>
            {errorMessage || "Shop không tồn tại hoặc không khả dụng."}
          </Text>
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
          disabledActions={vacationDisabled}
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

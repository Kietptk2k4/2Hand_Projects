import { useCallback } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router, useLocalSearchParams } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { useShopReviews } from "../hooks/useShopReviews";
import { CommerceProductListError } from "./CommerceProductListStates";
import { ProductReviewCard } from "./ProductReviewCard";
import { ProductReviewsFilters } from "./ProductReviewsFilters";
import { ProductReviewsSkeleton } from "./ProductReviewsSkeleton";
import { ProductReviewsSummary } from "./ProductReviewsSummary";

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    listContent: { padding: 16, gap: 16, paddingBottom: 32 },
    header: { gap: 16 },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant },
    emptyCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
      gap: 8,
    },
    emptyText: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center" },
    linkText: { fontSize: 14, fontWeight: "600", color: colors.primary },
    footer: { paddingVertical: 20, alignItems: "center" },
    loadMoreButton: {
      paddingHorizontal: 20,
      paddingVertical: 12,
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.primary,
    },
    loadMoreText: { fontSize: 14, fontWeight: "600", color: colors.primary },
    notFoundCard: {
      flex: 1,
      margin: 16,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
      gap: 12,
    },
    primaryButton: {
      paddingHorizontal: 16,
      paddingVertical: 10,
      borderRadius: 10,
      backgroundColor: colors.primary,
    },
    primaryButtonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
    secondaryButton: {
      paddingHorizontal: 16,
      paddingVertical: 10,
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.primary,
    },
    secondaryButtonText: { fontSize: 14, fontWeight: "600", color: colors.primary },
  };
}

function resolveShopId(raw) {
  if (typeof raw === "string") return raw;
  if (Array.isArray(raw)) return raw[0] ?? "";
  return "";
}

export function CommerceShopReviewsScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { shopId: rawShopId } = useLocalSearchParams();
  const shopId = resolveShopId(rawShopId);

  const {
    shopName,
    shop,
    reviews,
    ratingSummary,
    sort,
    ratingFilter,
    changeSort,
    changeRatingFilter,
    isInitialLoading,
    isLoadingMore,
    isNotFound,
    isEmpty,
    hasNext,
    errorMessage,
    loadMore,
    retry,
  } = useShopReviews(shopId);

  const shopTitle = shopName || shopId;

  const renderHeader = useCallback(
    () => (
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Đánh giá shop</Text>
          <Text style={styles.subtitle} numberOfLines={2}>
            {shopTitle}
          </Text>
        </View>

        <ProductReviewsSummary ratingSummary={ratingSummary} />
        <ProductReviewsFilters
          sort={sort}
          ratingFilter={ratingFilter}
          onSortChange={changeSort}
          onRatingFilterChange={changeRatingFilter}
          disabled={isInitialLoading}
        />

        {isEmpty ? (
          <View style={styles.emptyCard}>
            <Ionicons name="chatbubble-ellipses-outline" size={36} color={colors.outline} />
            <Text style={styles.emptyText}>Chưa có đánh giá nào cho shop này.</Text>
            <Pressable onPress={() => router.push(ROUTES.commerceShopProducts(shopId))}>
              <Text style={styles.linkText}>Quay lại shop</Text>
            </Pressable>
          </View>
        ) : null}
      </View>
    ),
    [
      changeRatingFilter,
      changeSort,
      colors.outline,
      isEmpty,
      isInitialLoading,
      ratingFilter,
      ratingSummary,
      shopId,
      shopTitle,
      sort,
      styles,
    ]
  );

  const renderFooter = useCallback(() => {
    if (!hasNext) return null;
    return (
      <View style={styles.footer}>
        {isLoadingMore ? (
          <ActivityIndicator color={colors.primary} />
        ) : (
          <Pressable style={styles.loadMoreButton} onPress={loadMore}>
            <Text style={styles.loadMoreText}>Tải thêm đánh giá</Text>
          </Pressable>
        )}
      </View>
    );
  }, [colors.primary, hasNext, isLoadingMore, loadMore, styles]);

  if (isInitialLoading) {
    return <ProductReviewsSkeleton />;
  }

  if (isNotFound) {
    return (
      <View style={styles.notFoundCard}>
        <Ionicons name="chatbubble-ellipses-outline" size={40} color={colors.outline} />
        <Text style={styles.emptyText}>{errorMessage || "Shop không tồn tại."}</Text>
        <Pressable style={styles.primaryButton} onPress={() => router.push(ROUTES.commerceHome)}>
          <Text style={styles.primaryButtonText}>Về trang Commerce</Text>
        </Pressable>
        <Pressable
          style={styles.secondaryButton}
          onPress={() => router.push(ROUTES.commerceShopProducts(shopId))}
        >
          <Text style={styles.secondaryButtonText}>Xem shop</Text>
        </Pressable>
      </View>
    );
  }

  if (!isInitialLoading && !isNotFound && errorMessage && reviews.length === 0) {
    return (
      <View style={{ padding: 16 }}>
        <CommerceProductListError message={errorMessage} onRetry={retry} />
      </View>
    );
  }

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.listContent}
      data={reviews}
      keyExtractor={(item) => item.reviewId}
      renderItem={({ item }) => (
        <ProductReviewCard review={item} productName={item.productName} shop={shop} />
      )}
      ListHeaderComponent={renderHeader}
      ListFooterComponent={renderFooter}
      onEndReached={() => {
        if (hasNext && !isLoadingMore) loadMore();
      }}
      onEndReachedThreshold={0.4}
    />
  );
}

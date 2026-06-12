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
import { useMyProductReview } from "../hooks/useMyProductReview";
import { useProductDetail } from "../hooks/useProductDetail";
import { useProductReviews } from "../hooks/useProductReviews";
import { CommerceProductListError } from "./CommerceProductListStates";
import { ProductReviewCard } from "./ProductReviewCard";
import { ProductReviewsFilters } from "./ProductReviewsFilters";
import { ProductReviewsSkeleton } from "./ProductReviewsSkeleton";
import { ProductReviewsSummary } from "./ProductReviewsSummary";

const COMMENT_PREVIEW_MAX = 160;

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    listContent: { padding: 16, gap: 16, paddingBottom: 32 },
    header: { gap: 16 },
    title: { fontSize: 22, fontWeight: "700", color: colors.onSurface },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant },
    myReviewCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.primary,
      backgroundColor: colors.surfaceContainerLow,
      padding: 16,
      gap: 10,
    },
    myReviewTitle: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    myReviewComment: { fontSize: 14, color: colors.onSurface, lineHeight: 20 },
    myReviewMuted: { fontSize: 14, color: colors.onSurfaceVariant },
    editButton: {
      alignSelf: "flex-start",
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.primary,
      paddingHorizontal: 14,
      paddingVertical: 8,
    },
    editButtonText: { fontSize: 14, fontWeight: "600", color: colors.primary },
    stars: { flexDirection: "row", gap: 2 },
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
      margin: 16,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
      gap: 12,
    },
    linkButton: {
      paddingHorizontal: 16,
      paddingVertical: 10,
      borderRadius: 10,
      backgroundColor: colors.primary,
    },
    linkButtonText: { fontSize: 14, fontWeight: "600", color: colors.onPrimary },
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

function resolveParam(raw) {
  if (typeof raw === "string") return raw;
  if (Array.isArray(raw)) return raw[0] ?? "";
  return "";
}

function truncateComment(text) {
  if (!text || text.length <= COMMENT_PREVIEW_MAX) return text;
  return `${text.slice(0, COMMENT_PREVIEW_MAX).trimEnd()}…`;
}

function StarRow({ rating }) {
  const colors = useThemeColors();
  const rounded = Math.round(rating);

  return (
    <View style={{ flexDirection: "row", gap: 2 }}>
      {[1, 2, 3, 4, 5].map((star) => (
        <Ionicons
          key={star}
          name={star <= rounded ? "star" : "star-outline"}
          size={16}
          color={star <= rounded ? "#F59E0B" : colors.outlineVariant}
        />
      ))}
    </View>
  );
}

function MyProductReviewStrip({ myReview, isLoading, isError, errorMessage, productId }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (isLoading) {
    return <View style={[styles.myReviewCard, { opacity: 0.6, minHeight: 80 }]} />;
  }

  if (isError) {
    return (
      <View style={styles.myReviewCard}>
        <Text style={styles.myReviewMuted}>{errorMessage}</Text>
      </View>
    );
  }

  if (!myReview) return null;

  if (myReview.hasReview) {
    return (
      <View style={styles.myReviewCard}>
        <Text style={styles.myReviewTitle}>Đánh giá của bạn</Text>
        <View style={styles.stars}>
          <StarRow rating={myReview.rating} />
        </View>
        {myReview.comment ? (
          <Text style={styles.myReviewComment}>{truncateComment(myReview.comment)}</Text>
        ) : (
          <Text style={styles.myReviewMuted}>Bạn chưa viết nhận xét.</Text>
        )}
        {myReview.canEdit && myReview.reviewId ? (
          <Pressable
            style={styles.editButton}
            onPress={() => router.push(ROUTES.commerceReviewEdit(myReview.reviewId))}
          >
            <Text style={styles.editButtonText}>Sửa đánh giá</Text>
          </Pressable>
        ) : null}
      </View>
    );
  }

  return (
    <View style={styles.myReviewCard}>
      <Text style={styles.myReviewTitle}>Đánh giá của bạn</Text>
      <Text style={styles.myReviewMuted}>Bạn chưa đánh giá sản phẩm này.</Text>
      <Text style={styles.myReviewMuted}>Hoàn tất đơn hàng và viết đánh giá từ trang chi tiết đơn.</Text>
      <Pressable onPress={() => router.push(ROUTES.commerceOrders)}>
        <Text style={styles.editButtonText}>Đơn hàng của tôi</Text>
      </Pressable>
    </View>
  );
}

export function CommerceProductReviewsScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { productId: rawProductId } = useLocalSearchParams();
  const productId = resolveParam(rawProductId);

  const { product: productDetail, isLoading: isDetailLoading } = useProductDetail(productId);

  const {
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
  } = useProductReviews(productId);

  const {
    myReview,
    isLoading: isMyReviewLoading,
    isError: isMyReviewError,
    errorMessage: myReviewErrorMessage,
  } = useMyProductReview(productId);

  const isPageLoading = isInitialLoading || (isDetailLoading && !productDetail);

  const renderHeader = useCallback(
    () => (
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Đánh giá sản phẩm</Text>
          <Text style={styles.subtitle} numberOfLines={2}>
            {productDetail?.title || productId}
          </Text>
        </View>

        <MyProductReviewStrip
          myReview={myReview}
          isLoading={isMyReviewLoading}
          isError={isMyReviewError}
          errorMessage={myReviewErrorMessage}
          productId={productId}
        />

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
            <Text style={styles.emptyText}>Chưa có đánh giá nào cho sản phẩm này.</Text>
            <Pressable onPress={() => router.push(ROUTES.commerceProductDetail(productId))}>
              <Text style={styles.editButtonText}>Quay lại sản phẩm</Text>
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
      isMyReviewError,
      isMyReviewLoading,
      myReview,
      myReviewErrorMessage,
      productDetail?.title,
      productId,
      ratingFilter,
      ratingSummary,
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

  if (isPageLoading) {
    return <ProductReviewsSkeleton />;
  }

  if (isNotFound) {
    return (
      <View style={styles.notFoundCard}>
        <Ionicons name="chatbubble-ellipses-outline" size={40} color={colors.outline} />
        <Text style={styles.emptyText}>{errorMessage || "Sản phẩm không tồn tại."}</Text>
        <Pressable style={styles.linkButton} onPress={() => router.push(ROUTES.commerceHome)}>
          <Text style={styles.linkButtonText}>Về trang Commerce</Text>
        </Pressable>
        <Pressable
          style={styles.secondaryButton}
          onPress={() => router.push(ROUTES.commerceProductDetail(productId))}
        >
          <Text style={styles.secondaryButtonText}>Xem sản phẩm</Text>
        </Pressable>
      </View>
    );
  }

  if (!isPageLoading && !isNotFound && errorMessage && reviews.length === 0) {
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
      renderItem={({ item }) => <ProductReviewCard review={item} />}
      ListHeaderComponent={renderHeader}
      ListFooterComponent={renderFooter}
      onEndReached={loadMore}
      onEndReachedThreshold={0.4}
    />
  );
}

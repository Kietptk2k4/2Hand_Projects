import { useCallback, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Image,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router, useLocalSearchParams } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { REVIEW_MEDIA_TOAST } from "../constants/reviewMediaConstants";
import { useReviewFormPage } from "../hooks/useReviewFormPage";
import { useUpdateProductReview } from "../hooks/useUpdateProductReview";
import { useUploadReviewMedia } from "../hooks/useUploadReviewMedia";
import { formatOrderDate } from "../utils/formatOrderDate";
import { formatVndPrice } from "../utils/formatVndPrice";
import { ProductReviewForm } from "./ProductReviewForm";

function createStyles(colors) {
  return {
    container: { flex: 1, backgroundColor: colors.surface },
    content: { padding: 16, gap: 16, paddingBottom: 32 },
    summaryCard: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 12,
    },
    summaryTitle: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    summaryImage: {
      width: "100%",
      height: 180,
      borderRadius: 12,
      backgroundColor: colors.surfaceContainerHigh,
    },
    productName: { fontSize: 16, fontWeight: "600", color: colors.onSurface },
    shopName: { fontSize: 14, color: colors.onSurfaceVariant },
    metaRow: { flexDirection: "row", justifyContent: "space-between", gap: 8 },
    metaLabel: { fontSize: 14, color: colors.onSurfaceVariant },
    metaValue: { fontSize: 14, fontWeight: "600", color: colors.onSurface },
    errorCard: {
      margin: 16,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 24,
      alignItems: "center",
      gap: 12,
    },
    errorText: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: "center" },
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
      borderColor: colors.outlineVariant,
    },
    secondaryButtonText: { fontSize: 14, color: colors.onSurface },
    loadingWrap: { flex: 1, alignItems: "center", justifyContent: "center", padding: 32 },
  };
}

function resolveParam(raw) {
  if (typeof raw === "string") return raw;
  if (Array.isArray(raw)) return raw[0] ?? "";
  return "";
}

function ReviewSummaryCard({ summary }) {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const imageUri = resolveDevMediaUrl(summary?.imageUrl);

  return (
    <View style={styles.summaryCard}>
      <Text style={styles.summaryTitle}>Sản phẩm đã mua</Text>
      {imageUri ? (
        <Image source={{ uri: imageUri }} style={styles.summaryImage} resizeMode="cover" />
      ) : (
        <View style={[styles.summaryImage, { alignItems: "center", justifyContent: "center" }]}>
          <Ionicons name="cube-outline" size={36} />
        </View>
      )}
      <Text style={styles.productName}>{summary?.productName}</Text>
      {summary?.shopName ? <Text style={styles.shopName}>{summary.shopName}</Text> : null}
      {summary?.price != null ? (
        <View style={styles.metaRow}>
          <Text style={styles.metaLabel}>Giá đã mua</Text>
          <Text style={styles.metaValue}>{formatVndPrice(summary.price)}</Text>
        </View>
      ) : null}
      {summary?.completedAt ? (
        <Text style={styles.metaLabel}>Hoàn thành {formatOrderDate(summary.completedAt)}</Text>
      ) : null}
    </View>
  );
}

export function CommerceEditReviewScreen() {
  useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { showToast } = useSocialToast();
  const { reviewId: rawReviewId } = useLocalSearchParams();
  const reviewId = resolveParam(rawReviewId);

  const {
    summary,
    initialRating,
    initialComment,
    productId,
    orderId,
    reviewStatus,
    existingMediaCount,
    isLoading,
    isError,
    errorMessage,
    retry,
  } = useReviewFormPage({ mode: "edit", reviewId });

  const { submit: submitUpdate, isSubmitting: isUpdating } = useUpdateProductReview();
  const { upload, isUploading } = useUploadReviewMedia();
  const [apiError, setApiError] = useState("");

  const backPath = useMemo(() => {
    if (orderId) return ROUTES.commerceOrderDetail(orderId);
    if (productId) return ROUTES.commerceProductReviews(productId);
    return ROUTES.commerceOrders;
  }, [orderId, productId]);

  const handleCancel = useCallback(() => {
    if (router.canGoBack()) {
      router.back();
      return;
    }
    router.replace(backPath);
  }, [backPath]);

  const navigateAfterSuccess = useCallback(() => {
    if (productId) {
      router.replace(ROUTES.commerceProductReviews(productId));
      return;
    }
    router.replace(backPath);
  }, [backPath, productId]);

  const handleSubmit = useCallback(
    async ({ payload, pendingFiles }) => {
      setApiError("");

      try {
        const hasPatch = payload && Object.keys(payload).length > 0;

        if (hasPatch) {
          await submitUpdate(reviewId, payload);
        }

        if (pendingFiles?.length) {
          await upload(reviewId, pendingFiles);
          showToast(
            hasPatch
              ? `Đã cập nhật đánh giá. ${REVIEW_MEDIA_TOAST.uploadSuccess}`
              : REVIEW_MEDIA_TOAST.uploadSuccess
          );
        } else if (hasPatch) {
          showToast("Đã cập nhật đánh giá.");
        }

        navigateAfterSuccess();
      } catch (error) {
        setApiError(error?.message || "Không thể lưu đánh giá. Vui lòng thử lại.");
      }
    },
    [navigateAfterSuccess, reviewId, showToast, submitUpdate, upload]
  );

  if (isLoading) {
    return (
      <View style={styles.loadingWrap}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  if (isError) {
    return (
      <View style={styles.errorCard}>
        <Text style={styles.errorText}>{errorMessage}</Text>
        <Pressable style={styles.primaryButton} onPress={retry}>
          <Text style={styles.primaryButtonText}>Thử lại</Text>
        </Pressable>
        <Pressable style={styles.secondaryButton} onPress={handleCancel}>
          <Text style={styles.secondaryButtonText}>Quay lại</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        {summary ? <ReviewSummaryCard summary={summary} /> : null}
        <ProductReviewForm
          mode="edit"
          initialRating={initialRating}
          initialComment={initialComment}
          existingMediaCount={existingMediaCount}
          reviewStatus={reviewStatus}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          isSubmitting={isUpdating}
          isUploading={isUploading}
          apiError={apiError}
        />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

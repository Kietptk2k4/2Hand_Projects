import { useCallback, useEffect, useMemo, useState } from "react";
import { ActivityIndicator, Pressable, Text, TextInput, View } from "react-native";
import {
  MAX_COMMENT_LENGTH,
  RATING_MAX,
  RATING_MIN,
} from "../constants/productReviewFormConstants";
import { MAX_REVIEW_MEDIA } from "../constants/reviewMediaConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { ReviewMediaPicker } from "./ReviewMediaPicker";
import { StarRatingInput } from "./StarRatingInput";

function createStyles(colors) {
  return {
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 20,
    },
    title: { fontSize: 20, fontWeight: "700", color: colors.onSurface },
    subtitle: { fontSize: 14, color: colors.onSurfaceVariant, marginTop: 4 },
    field: { gap: 8 },
    label: { fontSize: 14, fontWeight: "600", color: colors.onSurface },
    required: { color: colors.error },
    input: {
      minHeight: 120,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      borderRadius: 12,
      paddingHorizontal: 12,
      paddingVertical: 12,
      fontSize: 15,
      color: colors.onSurface,
      backgroundColor: colors.surfaceContainerLowest,
      textAlignVertical: "top",
    },
    counterRow: {
      flexDirection: "row",
      justifyContent: "space-between",
      gap: 8,
    },
    counterHint: { flex: 1, fontSize: 12, color: colors.onSurfaceVariant },
    counter: { fontSize: 12, color: colors.onSurfaceVariant },
    error: { fontSize: 12, color: colors.error },
    apiError: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.error,
      backgroundColor: colors.errorContainer,
      padding: 12,
    },
    apiErrorText: { fontSize: 14, color: colors.onErrorContainer },
    info: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      padding: 12,
    },
    infoText: { fontSize: 14, color: colors.onSurfaceVariant },
    actions: {
      flexDirection: "row",
      justifyContent: "flex-end",
      gap: 12,
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
      paddingTop: 16,
    },
    cancelButton: { paddingHorizontal: 16, paddingVertical: 12 },
    cancelText: { fontSize: 15, color: colors.onSurfaceVariant },
    submitButton: {
      minWidth: 140,
      paddingHorizontal: 16,
      paddingVertical: 12,
      borderRadius: 10,
      backgroundColor: colors.primary,
      alignItems: "center",
      justifyContent: "center",
    },
    submitDisabled: { opacity: 0.5 },
    submitText: { fontSize: 15, fontWeight: "600", color: colors.onPrimary },
  };
}

export function ProductReviewForm({
  mode = "create",
  initialRating = 0,
  initialComment = "",
  existingMediaCount = 0,
  maxMedia = MAX_REVIEW_MEDIA,
  reviewStatus = "VISIBLE",
  onSubmit,
  onCancel,
  isSubmitting = false,
  isUploading = false,
  apiError = "",
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  const [rating, setRating] = useState(initialRating);
  const [comment, setComment] = useState(initialComment);
  const [pendingFiles, setPendingFiles] = useState([]);
  const [ratingError, setRatingError] = useState("");
  const [noChangesMessage, setNoChangesMessage] = useState("");

  useEffect(() => {
    setRating(initialRating);
    setComment(initialComment);
  }, [initialRating, initialComment]);

  const isEdit = mode === "edit";
  const isBusy = isSubmitting || isUploading;
  const canUploadMedia = reviewStatus === "VISIBLE";

  const hasTextChanges = useMemo(() => {
    if (!isEdit) return true;
    return rating !== initialRating || comment !== initialComment;
  }, [comment, initialComment, initialRating, isEdit, rating]);

  const hasPendingMedia = pendingFiles.length > 0;
  const hasChanges = hasTextChanges || hasPendingMedia;

  const validate = useCallback(() => {
    if (!rating || rating < RATING_MIN || rating > RATING_MAX) {
      setRatingError("Vui lòng chọn điểm đánh giá từ 1 đến 5 sao.");
      return false;
    }
    setRatingError("");
    return true;
  }, [rating]);

  const handleSubmit = useCallback(async () => {
    setNoChangesMessage("");
    if (!validate()) return;

    if (isEdit && !hasChanges) {
      setNoChangesMessage("Không có thay đổi để lưu.");
      return;
    }

    const payload = isEdit
      ? {
          ...(rating !== initialRating ? { rating } : {}),
          ...(comment !== initialComment ? { comment } : {}),
        }
      : { rating, comment: comment.trim() || undefined };

    await onSubmit?.({ payload, pendingFiles });
  }, [
    comment,
    hasChanges,
    initialComment,
    initialRating,
    isEdit,
    onSubmit,
    pendingFiles,
    rating,
    validate,
  ]);

  const submitLabel = isEdit ? "Lưu thay đổi" : "Gửi đánh giá";
  const submitDisabled = isBusy || (isEdit && !hasChanges);

  return (
    <View style={styles.card}>
      <View>
        <Text style={styles.title}>{isEdit ? "Sửa đánh giá" : "Viết đánh giá"}</Text>
        <Text style={styles.subtitle}>
          {isEdit
            ? "Cập nhật đánh giá của bạn để giúp người mua khác."
            : "Chia sẻ trải nghiệm để giúp người mua khác quyết định tốt hơn."}
        </Text>
      </View>

      <View style={styles.field}>
        <Text style={styles.label}>
          Đánh giá tổng thể <Text style={styles.required}>*</Text>
        </Text>
        <StarRatingInput value={rating} onChange={setRating} disabled={isBusy} />
        {ratingError ? <Text style={styles.error}>{ratingError}</Text> : null}
      </View>

      <View style={styles.field}>
        <Text style={styles.label}>Nhận xét chi tiết</Text>
        <TextInput
          value={comment}
          onChangeText={(text) => setComment(text.slice(0, MAX_COMMENT_LENGTH))}
          editable={!isBusy}
          multiline
          placeholder="Bạn thích hoặc chưa hài lòng điều gì? Sản phẩm có đúng mô tả không?"
          placeholderTextColor={colors.outline}
          style={styles.input}
        />
        <View style={styles.counterRow}>
          <Text style={styles.counterHint}>Gợi ý: mô tả càng cụ thể càng hữu ích (không bắt buộc).</Text>
          <Text style={styles.counter}>
            {comment.length} / {MAX_COMMENT_LENGTH}
          </Text>
        </View>
      </View>

      <View style={styles.field}>
        <Text style={styles.label}>Ảnh / video đính kèm</Text>
        <ReviewMediaPicker
          existingMediaCount={existingMediaCount}
          maxMedia={maxMedia}
          disabled={isBusy || !canUploadMedia}
          onFilesChange={setPendingFiles}
        />
        {!canUploadMedia ? (
          <Text style={styles.error}>Đánh giá đã bị ẩn — không thể thêm ảnh/video.</Text>
        ) : null}
      </View>

      {apiError ? (
        <View style={styles.apiError}>
          <Text style={styles.apiErrorText}>{apiError}</Text>
        </View>
      ) : null}

      {noChangesMessage ? (
        <View style={styles.info}>
          <Text style={styles.infoText}>{noChangesMessage}</Text>
        </View>
      ) : null}

      <View style={styles.actions}>
        <Pressable style={styles.cancelButton} onPress={onCancel} disabled={isBusy}>
          <Text style={styles.cancelText}>Hủy</Text>
        </Pressable>
        <Pressable
          style={[styles.submitButton, submitDisabled && styles.submitDisabled]}
          onPress={handleSubmit}
          disabled={submitDisabled}
        >
          {isBusy ? (
            <ActivityIndicator color={colors.onPrimary} />
          ) : (
            <Text style={styles.submitText}>{submitLabel}</Text>
          )}
        </Pressable>
      </View>
    </View>
  );
}

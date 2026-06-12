import { ActivityIndicator, Pressable, Text, View } from "react-native";
import { COMMENT_SORT_OPTIONS, EMPTY_COMMENTS_MESSAGE } from "../constants/commentConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { CommentItem } from "./CommentItem";
import { CommentSkeleton } from "./CommentSkeleton";

function createStyles(colors) {
  return {
    container: { marginTop: 8 },
    headerRow: { marginBottom: 12, gap: 8 },
    title: { fontSize: 14, fontWeight: "600", color: colors.onSurface },
    sortRow: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
    sortChip: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      paddingHorizontal: 10,
      paddingVertical: 6,
    },
    sortChipActive: {
      borderColor: colors.primary,
      backgroundColor: colors.surfaceContainerLow,
    },
    sortText: { fontSize: 12, color: colors.onSurfaceVariant },
    sortTextActive: { color: colors.primary, fontWeight: "600" },
    submitError: { fontSize: 12, color: colors.error, marginBottom: 8 },
    errorCard: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.error,
      backgroundColor: colors.errorContainer,
      padding: 12,
      marginBottom: 12,
    },
    errorText: { fontSize: 14, color: colors.onErrorContainer },
    retryButton: { marginTop: 8, alignSelf: "flex-start" },
    retryButtonText: { fontSize: 14, fontWeight: "600", color: colors.primary },
    emptyText: { fontSize: 14, color: colors.onSurfaceVariant, marginBottom: 8 },
    loadMoreButton: { alignItems: "center", paddingVertical: 12, minHeight: 44 },
    loadMoreText: { fontSize: 14, fontWeight: "600", color: colors.primary },
    anchor: { height: 1, width: "100%" },
  };
}

export function PostDetailComments({
  commentsState,
  onViewProfile,
  commentAnchorRef,
}) {
  const {
    comments,
    isLoading,
    isLoadingMore,
    isEmpty,
    errorMessage,
    hasNext,
    loadMore,
    retry,
    repliesByParent,
    replyStatusByParent,
    replyingToId,
    isSubmittingReplyId,
    submitError,
    startReply,
    cancelReply,
    submitReply,
    expandReplies,
    commentSort,
    setCommentSort,
  } = commentsState;

  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={styles.container}>
      <View style={styles.headerRow}>
        <Text style={styles.title}>Bình luận</Text>
        <View style={styles.sortRow}>
          {COMMENT_SORT_OPTIONS.map((option) => {
            const active = commentSort === option.value;
            return (
              <Pressable
                key={option.value}
                onPress={() => setCommentSort(option.value)}
                disabled={isLoading}
                style={[styles.sortChip, active && styles.sortChipActive]}
              >
                <Text style={[styles.sortText, active && styles.sortTextActive]}>
                  {option.label}
                </Text>
              </Pressable>
            );
          })}
        </View>
      </View>

      {submitError && !replyingToId ? (
        <Text style={styles.submitError} accessibilityRole="alert">
          {submitError}
        </Text>
      ) : null}

      {isLoading ? (
        <View>
          <CommentSkeleton />
          <CommentSkeleton />
          <CommentSkeleton />
        </View>
      ) : null}

      {!isLoading && errorMessage ? (
        <View style={styles.errorCard}>
          <Text style={styles.errorText}>{errorMessage}</Text>
          <Pressable style={styles.retryButton} onPress={retry}>
            <Text style={styles.retryButtonText}>Thử lại</Text>
          </Pressable>
        </View>
      ) : null}

      {!isLoading && !errorMessage && isEmpty ? (
        <Text style={styles.emptyText}>{EMPTY_COMMENTS_MESSAGE}</Text>
      ) : null}

      {!isLoading && !errorMessage && comments.length > 0 ? (
        <View>
          {comments.map((item) => {
            const expanded = repliesByParent[item.commentId] !== undefined;
            return (
              <CommentItem
                key={item.commentId}
                comment={item}
                replies={repliesByParent[item.commentId] || []}
                isRepliesExpanded={expanded}
                isRepliesLoading={replyStatusByParent[item.commentId] === "loading"}
                onExpandReplies={expandReplies}
                onViewProfile={onViewProfile}
                replyingToId={replyingToId}
                onStartReply={startReply}
                onCancelReply={cancelReply}
                onSubmitReply={submitReply}
                isSubmittingReply={isSubmittingReplyId === item.commentId}
              />
            );
          })}
          {hasNext ? (
            <Pressable
              style={styles.loadMoreButton}
              onPress={loadMore}
              disabled={isLoadingMore}
            >
              {isLoadingMore ? (
                <ActivityIndicator color={colors.primary} />
              ) : (
                <Text style={styles.loadMoreText}>Xem thêm bình luận</Text>
              )}
            </Pressable>
          ) : null}
        </View>
      ) : null}

      <View ref={commentAnchorRef} style={styles.anchor} />
    </View>
  );
}

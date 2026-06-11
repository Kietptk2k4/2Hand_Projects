import { useEffect, useState } from "react";
import { Image, Pressable, StyleSheet, Text, View } from "react-native";
import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { formatSocialCount } from "../utils/formatSocialCount";
import { authorAvatarUrl } from "../utils/authorDisplay";
import { CommentComposer } from "./CommentComposer";
import { CommentMediaDisplay } from "./CommentMediaDisplay";
import { colors } from "../../../shared/theme/colors";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

function CommentMeta({
  createdAt,
  likeCount,
  showReply,
  onStartReply,
  isSubmittingReply,
  replyCount,
  isRepliesExpanded,
  isRepliesLoading,
  onExpandReplies,
  disabled,
}) {
  return (
    <View style={styles.metaRow}>
      <Text style={styles.metaText}>{formatRelativeTime(createdAt)}</Text>
      {likeCount > 0 ? (
        <Text style={styles.metaText}>{formatSocialCount(likeCount)} thích</Text>
      ) : null}
      {showReply ? (
        <Pressable onPress={onStartReply} disabled={disabled || isSubmittingReply}>
          <Text style={styles.metaAction}>Trả lời</Text>
        </Pressable>
      ) : null}
      {replyCount > 0 && !isRepliesExpanded ? (
        <Pressable onPress={onExpandReplies} disabled={isRepliesLoading}>
          <Text style={styles.metaAction}>
            {isRepliesLoading ? "Đang tải..." : `Xem ${replyCount} phản hồi`}
          </Text>
        </Pressable>
      ) : null}
    </View>
  );
}

export function CommentItem({
  comment,
  replies = [],
  isRepliesLoading,
  isRepliesExpanded,
  onExpandReplies,
  onViewProfile,
  replyingToId,
  onStartReply,
  onCancelReply,
  onSubmitReply,
  isSubmittingReply = false,
}) {
  const [replyDraft, setReplyDraft] = useState("");
  const avatarUrl =
    comment.author?.avatarUrl || authorAvatarUrl(comment.author?.userId) || DEFAULT_AVATAR;
  const displayName = comment.author?.displayName || DEFAULT_USER_DISPLAY_NAME;
  const authorUserId = comment.author?.userId;
  const isTopLevel = !comment.parentCommentId;
  const isReplying = replyingToId === comment.commentId;

  useEffect(() => {
    if (!isReplying) {
      setReplyDraft("");
    }
  }, [isReplying]);

  const handleSubmitReply = async () => {
    const result = await onSubmitReply?.(comment.commentId, replyDraft);
    if (result?.ok) {
      setReplyDraft("");
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.row}>
        <Pressable
          onPress={() => authorUserId && onViewProfile?.(authorUserId)}
          accessibilityLabel={`Xem hồ sơ ${displayName}`}
        >
          <Image source={{ uri: avatarUrl }} style={styles.avatar} />
        </Pressable>
        <View style={styles.content}>
          <View style={styles.bubble}>
            <Pressable onPress={() => authorUserId && onViewProfile?.(authorUserId)}>
              <Text style={styles.authorName}>{displayName}</Text>
            </Pressable>
            <Text style={styles.commentText}>{comment.contentText}</Text>
            <CommentMediaDisplay media={comment.media} />
          </View>
          <CommentMeta
            createdAt={comment.createdAt}
            likeCount={comment.likeCount ?? 0}
            showReply={isTopLevel}
            onStartReply={() => onStartReply?.(comment.commentId)}
            isSubmittingReply={isSubmittingReply}
            replyCount={comment.replyCount}
            isRepliesExpanded={isRepliesExpanded}
            isRepliesLoading={isRepliesLoading}
            onExpandReplies={() => onExpandReplies?.(comment.commentId)}
            disabled={false}
          />

          {isTopLevel && isReplying ? (
            <View style={styles.replyComposerBlock}>
              <CommentComposer
                variant="compact"
                value={replyDraft}
                onChange={setReplyDraft}
                onSubmit={handleSubmitReply}
                placeholder="Viết phản hồi..."
                isSubmitting={isSubmittingReply}
              />
              <Pressable onPress={onCancelReply} disabled={isSubmittingReply}>
                <Text style={styles.cancelReply}>Hủy</Text>
              </Pressable>
            </View>
          ) : null}
        </View>
      </View>

      {isRepliesExpanded ? (
        <View style={styles.repliesBlock}>
          {replies.map((reply) => {
            const replyAvatar =
              reply.author?.avatarUrl ||
              authorAvatarUrl(reply.author?.userId) ||
              DEFAULT_AVATAR;
            const replyName = reply.author?.displayName || DEFAULT_USER_DISPLAY_NAME;

            return (
              <View key={reply.commentId} style={styles.replyRow}>
                <Pressable
                  onPress={() =>
                    reply.author?.userId && onViewProfile?.(reply.author.userId)
                  }
                >
                  <Image source={{ uri: replyAvatar }} style={styles.replyAvatar} />
                </Pressable>
                <View style={styles.content}>
                  <View style={styles.bubble}>
                    <Text style={styles.authorName}>{replyName}</Text>
                    <Text style={styles.commentText}>{reply.contentText}</Text>
                    <CommentMediaDisplay media={reply.media} />
                  </View>
                  <CommentMeta
                    createdAt={reply.createdAt}
                    likeCount={reply.likeCount ?? 0}
                    showReply={false}
                    replyCount={0}
                    isRepliesExpanded={false}
                    isRepliesLoading={false}
                    onExpandReplies={() => {}}
                  />
                </View>
              </View>
            );
          })}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
  },
  row: {
    flexDirection: "row",
    gap: 12,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.surfaceContainerHigh,
  },
  content: {
    flex: 1,
    minWidth: 0,
  },
  bubble: {
    backgroundColor: colors.surfaceContainerLow,
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  authorName: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.onSurface,
  },
  commentText: {
    marginTop: 4,
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurface,
  },
  metaRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    alignItems: "center",
    gap: 12,
    marginTop: 6,
  },
  metaText: {
    fontSize: 12,
    color: colors.onSurfaceVariant,
  },
  metaAction: {
    fontSize: 12,
    fontWeight: "600",
    color: colors.primary,
  },
  replyComposerBlock: {
    marginTop: 8,
    gap: 8,
  },
  cancelReply: {
    fontSize: 12,
    fontWeight: "600",
    color: colors.onSurfaceVariant,
  },
  repliesBlock: {
    marginLeft: 52,
    marginTop: 12,
    paddingLeft: 12,
    borderLeftWidth: 2,
    borderLeftColor: colors.outlineVariant,
    gap: 12,
  },
  replyRow: {
    flexDirection: "row",
    gap: 10,
  },
  replyAvatar: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: colors.surfaceContainerHigh,
  },
});

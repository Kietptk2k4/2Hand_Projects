import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useHashtagPosts } from "../hooks/useHashtagPosts";
import { usePostScreenActions } from "../hooks/usePostScreenActions";
import { formatHashtagLabel } from "../utils/normalizeHashtag";
import { FeedPostSkeleton } from "./FeedPostSkeleton";
import { PostCard } from "./PostCard";
import { colors } from "../../../shared/theme/colors";

export function HashtagFeedScreen({ hashtag: rawHashtag }) {
  const hashtagState = useHashtagPosts(rawHashtag);
  const actions = usePostScreenActions();
  const title = formatHashtagLabel(hashtagState.resolvedHashtag || hashtagState.hashtag);

  const renderHeader = () => (
    <View style={styles.header}>
      <Text style={styles.title}>{title}</Text>
      {hashtagState.totalElements > 0 ? (
        <Text style={styles.subtitle}>{hashtagState.totalElements} bài viết</Text>
      ) : null}
    </View>
  );

  const renderEmpty = () => {
    if (hashtagState.isInvalidHashtag) {
      return (
        <View style={styles.messageCard}>
          <Text style={styles.emptyText}>Hashtag không hợp lệ.</Text>
        </View>
      );
    }

    if (hashtagState.isInitialLoading) {
      return (
        <View style={styles.skeletonBlock}>
          <FeedPostSkeleton />
          <FeedPostSkeleton />
        </View>
      );
    }

    if (hashtagState.errorMessage) {
      return (
        <View style={[styles.messageCard, styles.errorCard]}>
          <Text style={styles.errorText}>{hashtagState.errorMessage}</Text>
          <Pressable style={styles.retryButton} onPress={hashtagState.retry}>
            <Text style={styles.retryButtonText}>Thử lại</Text>
          </Pressable>
        </View>
      );
    }

    return (
      <View style={styles.messageCard}>
        <Text style={styles.emptyText}>Chưa có bài viết nào với hashtag này.</Text>
      </View>
    );
  };

  return (
    <FlatList
      data={hashtagState.isInvalidHashtag ? [] : hashtagState.items}
      keyExtractor={(item) => item.postId}
      ListHeaderComponent={renderHeader}
      renderItem={({ item }) => (
        <PostCard
          post={item}
          currentUserId={actions.currentUserId}
          onOpenPost={actions.onOpenPost}
          onViewProfile={actions.onViewProfile}
          onHashtagClick={actions.onHashtagClick}
          onEditPost={actions.onEditPost}
          onToggleLike={actions.toggleLike}
          onToggleSave={actions.toggleSave}
          onDeletePost={(postId) => actions.confirmDelete(postId)}
          isLikingPost={actions.isLikingPost(item.postId)}
          isSavingPost={actions.isSavingPost(item.postId)}
          isDeletingPost={actions.isDeletingPost(item.postId)}
        />
      )}
      ListEmptyComponent={renderEmpty}
      contentContainerStyle={styles.listContent}
      refreshControl={
        <RefreshControl
          refreshing={hashtagState.isRefreshing}
          onRefresh={hashtagState.refetch}
        />
      }
      onEndReached={() => {
        if (hashtagState.hasNext) hashtagState.loadMore();
      }}
      onEndReachedThreshold={0.4}
      ListFooterComponent={
        hashtagState.isLoadingMore ? (
          <View style={styles.footer}>
            <ActivityIndicator color={colors.primary} />
          </View>
        ) : null
      }
    />
  );
}

const styles = StyleSheet.create({
  listContent: {
    paddingHorizontal: 16,
    paddingBottom: 24,
    flexGrow: 1,
  },
  header: {
    paddingTop: 8,
    paddingBottom: 16,
  },
  title: {
    fontSize: 22,
    fontWeight: "700",
    color: colors.onSurface,
  },
  subtitle: {
    marginTop: 4,
    fontSize: 14,
    color: colors.onSurfaceVariant,
  },
  skeletonBlock: {
    gap: 0,
  },
  messageCard: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
    padding: 24,
    alignItems: "center",
  },
  errorCard: {
    borderColor: colors.error,
    backgroundColor: colors.errorContainer,
  },
  errorText: {
    fontSize: 14,
    color: colors.onErrorContainer,
    textAlign: "center",
    marginBottom: 16,
  },
  emptyText: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
    textAlign: "center",
  },
  retryButton: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    minHeight: 44,
    paddingHorizontal: 20,
    alignItems: "center",
    justifyContent: "center",
  },
  retryButtonText: {
    color: colors.onPrimary,
    fontSize: 14,
    fontWeight: "600",
  },
  footer: {
    paddingVertical: 16,
  },
});

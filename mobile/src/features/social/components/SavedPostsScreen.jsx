import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSavedPosts } from "../hooks/useSavedPosts";
import { usePostScreenActions } from "../hooks/usePostScreenActions";
import { PostCard } from "./PostCard";
import { FeedPostSkeleton } from "./FeedPostSkeleton";
import { colors } from "../../../shared/theme/colors";

export function SavedPostsScreen() {
  const postsState = useSavedPosts();
  const actions = usePostScreenActions();

  const renderEmpty = () => {
    if (postsState.isInitialLoading) {
      return (
        <View style={styles.skeletonBlock}>
          <FeedPostSkeleton />
          <FeedPostSkeleton />
        </View>
      );
    }

    if (postsState.errorMessage) {
      return (
        <View style={[styles.messageCard, styles.errorCard]}>
          <Text style={styles.errorText}>{postsState.errorMessage}</Text>
          <Pressable style={styles.retryButton} onPress={postsState.retry}>
            <Text style={styles.retryButtonText}>Thử lại</Text>
          </Pressable>
        </View>
      );
    }

    return (
      <View style={styles.messageCard}>
        <Text style={styles.emptyText}>Bạn chưa lưu bài viết nào.</Text>
      </View>
    );
  };

  return (
    <FlatList
      data={postsState.items}
      keyExtractor={(item) => item.postId}
      renderItem={({ item }) => (
        <PostCard
          post={{ ...item, savedByMe: true }}
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
          refreshing={postsState.isRefreshing}
          onRefresh={postsState.refetch}
        />
      }
      onEndReached={() => {
        if (postsState.hasNext) postsState.loadMore();
      }}
      onEndReachedThreshold={0.4}
      ListFooterComponent={
        postsState.isLoadingMore ? (
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
    paddingTop: 8,
    paddingBottom: 24,
    flexGrow: 1,
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
    marginTop: 16,
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

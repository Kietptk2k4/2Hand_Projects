import { useCallback, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { router } from "expo-router";
import { FEED_TABS } from "../constants/feedTabs";
import { useCurrentUserId } from "../hooks/useCurrentUserId";
import { useDeletePost } from "../hooks/useDeletePost";
import { useFeed } from "../hooks/useFeed";
import { useLikePost } from "../hooks/useLikePost";
import { useSavePost } from "../hooks/useSavePost";
import { FeedComposer } from "./FeedComposer";
import { FeedHeaderActions } from "./FeedHeaderActions";
import { FeedPostSkeleton } from "./FeedPostSkeleton";
import { FeedTabs } from "./FeedTabs";
import { PostCard } from "./PostCard";
import { ROUTES } from "../../../shared/constants/routes";
import { colors } from "../../../shared/theme/colors";

const EMPTY_BY_TAB = {
  [FEED_TABS.GLOBAL]: "Chưa có bài viết công khai nào trên feed đề xuất.",
  [FEED_TABS.FOLLOWING]:
    "Bạn chưa theo dõi ai hoặc chưa có bài viết từ người bạn theo dõi.",
};

export function FeedScreen() {
  const [activeTab, setActiveTab] = useState(FEED_TABS.GLOBAL);
  const currentUserId = useCurrentUserId();
  const {
    items,
    errorMessage,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    loadMore,
    retry,
  } = useFeed(activeTab);

  const { toggleLike, isLikingPost } = useLikePost();
  const { toggleSave, isSavingPost } = useSavePost();
  const { confirmDelete, isDeletingPost } = useDeletePost();

  const onOpenPost = useCallback((postId, options = {}) => {
    router.push(ROUTES.postDetail(postId, options));
  }, []);

  const onViewProfile = useCallback((userId) => {
    if (!userId) return;
    router.push(ROUTES.userProfile(userId));
  }, []);

  const onHashtagClick = useCallback((tag) => {
    if (!tag) return;
    router.push(ROUTES.hashtag(tag));
  }, []);

  const onOpenCreatePost = useCallback(() => {
    router.push(ROUTES.postCreate());
  }, []);

  const onOpenCreatePostWithPicker = useCallback(() => {
    router.push(ROUTES.postCreate({ pickMedia: true }));
  }, []);

  const onEditPost = useCallback((postId) => {
    if (!postId) return;
    router.push(ROUTES.postEdit(postId));
  }, []);

  const renderHeader = () => (
    <View style={styles.headerBlock}>
      <FeedHeaderActions />
      <FeedComposer
        onOpenCreatePost={onOpenCreatePost}
        onOpenCreatePostWithPicker={onOpenCreatePostWithPicker}
      />
      <FeedTabs activeTab={activeTab} onChange={setActiveTab} />
    </View>
  );

  const renderEmpty = () => {
    if (isInitialLoading) {
      return (
        <View style={styles.skeletonBlock}>
          <FeedPostSkeleton />
          <FeedPostSkeleton />
        </View>
      );
    }

    if (errorMessage) {
      return (
        <View style={[styles.messageCard, styles.errorCard]}>
          <Text style={styles.errorText}>{errorMessage}</Text>
          <Pressable style={styles.retryButton} onPress={retry}>
            <Text style={styles.retryButtonText}>Thử lại</Text>
          </Pressable>
        </View>
      );
    }

    return (
      <View style={styles.messageCard}>
        <Text style={styles.emptyIcon}>feed</Text>
        <Text style={styles.emptyText}>{EMPTY_BY_TAB[activeTab]}</Text>
      </View>
    );
  };

  return (
    <FlatList
      data={items}
      keyExtractor={(item) => item.postId}
      renderItem={({ item }) => (
        <PostCard
          post={item}
          currentUserId={currentUserId}
          onOpenPost={onOpenPost}
          onViewProfile={onViewProfile}
          onHashtagClick={onHashtagClick}
          onToggleLike={toggleLike}
          onToggleSave={toggleSave}
          onEditPost={onEditPost}
          onDeletePost={(postId) => confirmDelete(postId)}
          isLikingPost={isLikingPost(item.postId)}
          isSavingPost={isSavingPost(item.postId)}
          isDeletingPost={isDeletingPost(item.postId)}
        />
      )}
      ListHeaderComponent={renderHeader}
      ListEmptyComponent={renderEmpty}
      ListFooterComponent={
        isLoadingMore ? (
          <View style={styles.footer}>
            <ActivityIndicator color={colors.primary} />
          </View>
        ) : null
      }
      contentContainerStyle={styles.listContent}
      onEndReached={() => {
        if (hasNext) loadMore();
      }}
      onEndReachedThreshold={0.4}
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
  headerBlock: {
    marginBottom: 16,
    marginTop: 8,
    gap: 16,
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
  emptyIcon: {
    fontSize: 36,
    color: colors.outline,
    marginBottom: 8,
  },
  emptyText: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
    textAlign: "center",
    lineHeight: 20,
  },
  footer: {
    paddingVertical: 16,
  },
});

import { useCallback } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  Text,
  View,
} from "react-native";
import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useHashtagPosts } from "../hooks/useHashtagPosts";
import { usePostScreenActions } from "../hooks/usePostScreenActions";
import { formatHashtagLabel } from "../utils/normalizeHashtag";
import { HashtagDiscoveryPanel } from "./HashtagDiscoveryPanel";
import { HashtagPageHeader } from "./HashtagPageHeader";
import { HashtagPostCard } from "./HashtagPostCard";
import { HashtagPostCardSkeleton } from "./HashtagPostCardSkeleton";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    root: {
      flex: 1,
      backgroundColor: colors.surface,
    },
    listContent: {
      paddingHorizontal: 16,
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
      marginTop: 8,
    },
    invalidRoot: {
      flex: 1,
      alignItems: "center",
      justifyContent: "center",
      paddingHorizontal: 24,
      backgroundColor: colors.surface,
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
    emptyIcon: {
      marginBottom: 8,
    },
    emptyText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
      lineHeight: 20,
    },
    feedLink: {
      marginTop: 16,
      fontSize: 14,
      fontWeight: "600",
      color: colors.primary,
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
      alignItems: "center",
    },
    loadMoreButton: {
      borderWidth: 1,
      borderColor: colors.primary,
      borderRadius: 8,
      paddingHorizontal: 24,
      paddingVertical: 10,
      minHeight: 44,
      justifyContent: "center",
    },
    loadMoreText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.primary,
    },
  };
}

export function HashtagFeedScreen({ hashtag: rawHashtag }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const hashtagState = useHashtagPosts(rawHashtag);
  const actions = usePostScreenActions();
  const displayHashtag = hashtagState.resolvedHashtag || hashtagState.hashtag;

  const navigateToHashtag = useCallback((tag) => {
    if (!tag) return;
    router.replace(ROUTES.hashtag(tag));
  }, []);

  const onOpenLikesList = useCallback(({ type, targetId, likeCount }) => {
    if (!targetId) return;
    router.push(
      ROUTES.postLikes(targetId, {
        targetType: type || "post",
        likeCount: likeCount ?? 0,
      })
    );
  }, []);

  const onOpenComments = useCallback(
    (postId) => {
      actions.onOpenPost(postId, { focusComments: true });
    },
    [actions]
  );

  if (hashtagState.isInvalidHashtag) {
    return (
      <View style={styles.invalidRoot}>
        <Text style={styles.emptyText}>Hashtag không hợp lệ.</Text>
        <Pressable onPress={() => router.replace(ROUTES.feed)} accessibilityRole="button">
          <Text style={styles.feedLink}>Về feed</Text>
        </Pressable>
      </View>
    );
  }

  const renderListHeader = () => (
    <HashtagDiscoveryPanel
      currentHashtag={displayHashtag}
      onSelectTag={navigateToHashtag}
    />
  );

  const renderEmpty = () => {
    if (hashtagState.isInitialLoading) {
      return (
        <View style={styles.skeletonBlock}>
          <HashtagPostCardSkeleton />
          <HashtagPostCardSkeleton />
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
        <Ionicons
          name="pricetag-outline"
          size={36}
          color={colors.outline}
          style={styles.emptyIcon}
        />
        <Text style={styles.emptyText}>
          Chưa có bài viết cho {formatHashtagLabel(displayHashtag)}
        </Text>
      </View>
    );
  };

  const renderFooter = () => {
    if (hashtagState.isInitialLoading || hashtagState.errorMessage) {
      return null;
    }

    if (hashtagState.isLoadingMore) {
      return (
        <View style={styles.footer}>
          <ActivityIndicator color={colors.primary} />
        </View>
      );
    }

    if (hashtagState.hasNext && hashtagState.items.length > 0) {
      return (
        <View style={styles.footer}>
          <Pressable style={styles.loadMoreButton} onPress={hashtagState.loadMore}>
            <Text style={styles.loadMoreText}>Tải thêm</Text>
          </Pressable>
        </View>
      );
    }

    return null;
  };

  return (
    <View style={styles.root}>
      <HashtagPageHeader
        hashtag={displayHashtag}
        totalElements={hashtagState.totalElements}
        onSearchTag={navigateToHashtag}
      />

      <FlatList
        data={hashtagState.items}
        keyExtractor={(item) => item.postId}
        ListHeaderComponent={renderListHeader}
        ListEmptyComponent={renderEmpty}
        ListFooterComponent={renderFooter}
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl
            refreshing={hashtagState.isRefreshing}
            onRefresh={hashtagState.refetch}
          />
        }
        onEndReached={() => {
          if (hashtagState.hasNext && !hashtagState.isLoadingMore) {
            hashtagState.loadMore();
          }
        }}
        onEndReachedThreshold={0.4}
        renderItem={({ item }) => (
          <HashtagPostCard
            post={item}
            currentUserId={actions.currentUserId}
            onOpenPost={actions.onOpenPost}
            onOpenComments={onOpenComments}
            onViewProfile={actions.onViewProfile}
            onHashtagClick={actions.onHashtagClick}
            onEditPost={actions.onEditPost}
            onToggleLike={actions.toggleLike}
            onToggleSave={actions.toggleSave}
            onDeletePost={(postId) => actions.confirmDelete(postId)}
            onOpenLikesList={onOpenLikesList}
            isLikingPost={actions.isLikingPost(item.postId)}
            isSavingPost={actions.isSavingPost(item.postId)}
            isDeletingPost={actions.isDeletingPost(item.postId)}
          />
        )}
      />
    </View>
  );
}

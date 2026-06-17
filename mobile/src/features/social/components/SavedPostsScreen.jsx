import { useCallback, useState } from "react";
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
import { toggleSavePost } from "../api/savePostApi";
import { useCurrentUserId } from "../hooks/useCurrentUserId";
import { useFeedSidebarStats } from "../hooks/useFeedSidebarStats";
import { useSavedPosts } from "../hooks/useSavedPosts";
import { patchPostEngagement } from "../utils/postEngagementCache";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { FeedProfileSummary } from "./FeedProfileSummary";
import { SavedPostCard } from "./SavedPostCard";
import { SavedPostCardSkeleton } from "./SavedPostCardSkeleton";
import { SavedPostsHeader } from "./SavedPostsHeader";
import { ROUTES } from "../../../shared/constants/routes";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useQueryClient } from "@tanstack/react-query";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    listContent: {
      paddingBottom: 24,
      flexGrow: 1,
      backgroundColor: colors.surface,
    },
    headerBlock: {
      gap: 12,
      paddingTop: 8,
    },
    listBody: {
      paddingHorizontal: 16,
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
      gap: 8,
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

export function SavedPostsScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const queryClient = useQueryClient();
  const { showToast } = useSocialToast();
  const currentUserId = useCurrentUserId();
  const sidebarStats = useFeedSidebarStats(currentUserId);
  const [unsavingId, setUnsavingId] = useState(null);

  const postsState = useSavedPosts();

  const onOpenPost = useCallback((postId, options = {}) => {
    router.push(ROUTES.postDetail(postId, options));
  }, []);

  const onViewProfile = useCallback((userId) => {
    if (!userId) return;
    router.push(ROUTES.userProfile(userId));
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

  const handleUnsave = useCallback(
    async (targetPostId) => {
      if (!targetPostId || unsavingId) return;

      postsState.removeItem(targetPostId);
      sidebarStats.adjustSavedCount(-1);
      setUnsavingId(targetPostId);

      try {
        const result = await toggleSavePost(targetPostId);
        const saved = Boolean(result?.saved);
        patchPostEngagement(queryClient, targetPostId, { savedByMe: saved });

        if (saved) {
          sidebarStats.adjustSavedCount(1);
          postsState.refetch();
        } else {
          showToast("Đã bỏ lưu.");
        }
      } catch (error) {
        const mapped = mapSocialWriteError(error);
        if (mapped.type === "session") {
          await handleSocialQueryError({ code: 401, message: mapped.message });
        } else if (mapped.type !== "suspended") {
          showToast(mapped.message || "Không bỏ lưu được bài viết.", "error");
        }
        sidebarStats.adjustSavedCount(1);
        postsState.refetch();
      } finally {
        setUnsavingId(null);
      }
    },
    [postsState, queryClient, showToast, sidebarStats, unsavingId]
  );

  const renderHeader = () => (
    <View style={styles.headerBlock}>
      {currentUserId ? (
        <View style={{ paddingHorizontal: 16 }}>
          <FeedProfileSummary userId={currentUserId} stats={sidebarStats} />
        </View>
      ) : null}
      <SavedPostsHeader />
    </View>
  );

  const renderEmpty = () => {
    if (postsState.isInitialLoading) {
      return (
        <View style={styles.listBody}>
          <SavedPostCardSkeleton />
          <SavedPostCardSkeleton />
          <SavedPostCardSkeleton />
        </View>
      );
    }

    if (postsState.errorMessage) {
      return (
        <View style={[styles.messageCard, styles.errorCard, styles.listBody]}>
          <Text style={styles.errorText}>{postsState.errorMessage}</Text>
          <Pressable style={styles.retryButton} onPress={postsState.retry}>
            <Text style={styles.retryButtonText}>Thử lại</Text>
          </Pressable>
        </View>
      );
    }

    return (
      <View style={[styles.messageCard, styles.listBody]}>
        <Ionicons
          name="bookmark-outline"
          size={36}
          color={colors.outline}
          style={styles.emptyIcon}
        />
        <Text style={styles.emptyText}>Bạn chưa lưu bài viết nào.</Text>
      </View>
    );
  };

  const renderFooter = () => {
    if (postsState.isLoadingMore) {
      return (
        <View style={styles.footer}>
          <ActivityIndicator color={colors.primary} />
        </View>
      );
    }

    if (postsState.hasNext && postsState.items.length > 0) {
      return (
        <View style={styles.footer}>
          <Pressable style={styles.loadMoreButton} onPress={postsState.loadMore}>
            <Text style={styles.loadMoreText}>Tải thêm</Text>
          </Pressable>
        </View>
      );
    }

    return null;
  };

  return (
    <FlatList
      data={postsState.isInitialLoading || postsState.errorMessage ? [] : postsState.items}
      keyExtractor={(item) => item.postId}
      ListHeaderComponent={renderHeader}
      ListEmptyComponent={renderEmpty}
      ListFooterComponent={renderFooter}
      contentContainerStyle={styles.listContent}
      refreshControl={
        <RefreshControl
          refreshing={postsState.isRefreshing}
          onRefresh={postsState.refetch}
        />
      }
      onEndReached={() => {
        if (postsState.hasNext && !postsState.isLoadingMore) {
          postsState.loadMore();
        }
      }}
      onEndReachedThreshold={0.4}
      renderItem={({ item }) => (
        <View style={styles.listBody}>
          <SavedPostCard
            post={item}
            onOpenPost={onOpenPost}
            onOpenComments={(postId) => onOpenPost(postId, { focusComments: true })}
            onViewProfile={onViewProfile}
            onUnsave={handleUnsave}
            onOpenLikesList={onOpenLikesList}
            isUnsaveLoading={unsavingId === item.postId}
          />
        </View>
      )}
    />
  );
}

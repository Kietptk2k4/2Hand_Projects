import { useCallback, useMemo, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { router } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { DEFAULT_PROFILE_STATUS_FILTER } from "../constants/profileConstants";
import { useCurrentUserId } from "../hooks/useCurrentUserId";
import { useDeletePost } from "../hooks/useDeletePost";
import { useFollowUser } from "../hooks/useFollowUser";
import { useLikePost } from "../hooks/useLikePost";
import { useProfile } from "../hooks/useProfile";
import { useProfilePosts } from "../hooks/useProfilePosts";
import { usePublicProfileDetails } from "../hooks/usePublicProfileDetails";
import { useSavePost } from "../hooks/useSavePost";
import { ProfileHeader } from "./ProfileHeader";
import { ProfilePostsFilter } from "./ProfilePostsFilter";
import { PostCard } from "./PostCard";
import { ROUTES } from "../../../shared/constants/routes";
import { colors } from "../../../shared/theme/colors";

function ProfilePostsEmpty({ isPrivateLocked }) {
  if (isPrivateLocked) {
    return (
      <View style={styles.messageCard}>
        <Text style={styles.emptyTitle}>Tài khoản riêng tư — theo dõi để xem bài viết</Text>
        <Text style={styles.emptyText}>
          Gửi yêu cầu theo dõi để xem bài viết của người dùng này.
        </Text>
      </View>
    );
  }

  return (
    <View style={styles.messageCard}>
      <Text style={styles.emptyText}>Chưa có bài viết nào.</Text>
    </View>
  );
}

function ProfilePostsError({ message, errorCode, onRetry }) {
  return (
    <View style={[styles.messageCard, styles.errorCard]}>
      <Text style={styles.errorText}>{message}</Text>
      {errorCode !== 403 ? (
        <Pressable style={styles.retryButton} onPress={onRetry}>
          <Text style={styles.retryButtonText}>Thử lại</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

export function ProfileScreen({ userId }) {
  const insets = useSafeAreaInsets();
  const currentUserId = useCurrentUserId();
  const [statusFilter, setStatusFilter] = useState(DEFAULT_PROFILE_STATUS_FILTER);

  const {
    profile,
    isLoading: isProfileLoading,
    isError: isProfileError,
    errorMessage: profileErrorMessage,
    errorCode: profileErrorCode,
    retry: retryProfile,
  } = useProfile(userId);

  const isSelf =
    profile?.followStatus === "SELF" ||
    Boolean(currentUserId && userId && currentUserId === userId);

  const publicDetailsState = usePublicProfileDetails(userId, {
    enabled: Boolean(userId) && Boolean(profile),
  });

  const details = publicDetailsState.details;

  const canViewPosts = Boolean(profile?.canViewFullProfile);
  const effectiveStatusFilter = isSelf ? statusFilter : "published";

  const postsState = useProfilePosts(userId, {
    enabled: canViewPosts,
    statusFilter: effectiveStatusFilter,
  });

  const { toggleFollow, isFollowLoading } = useFollowUser(userId);
  const { toggleLike, isLikingPost } = useLikePost();
  const { toggleSave, isSavingPost } = useSavePost();
  const { confirmDelete, isDeletingPost } = useDeletePost({
    onDeleted: () => postsState.refetch(),
  });

  const onOpenPost = useCallback((postId, options = {}) => {
    router.push(ROUTES.postDetail(postId, options));
  }, []);

  const onViewProfile = useCallback((targetUserId) => {
    if (!targetUserId) return;
    router.push(ROUTES.userProfile(targetUserId));
  }, []);

  const onEditPost = useCallback((postId) => {
    if (!postId) return;
    router.push(ROUTES.postEdit(postId));
  }, []);

  const onHashtagClick = useCallback((tag) => {
    if (!tag) return;
    router.push(ROUTES.hashtag(tag));
  }, []);

  const onFollowersPress = useCallback(() => {
    router.push(ROUTES.profileFollowers(userId));
  }, [userId]);

  const onFollowingPress = useCallback(() => {
    router.push(ROUTES.profileFollowing(userId));
  }, [userId]);

  const onRefresh = useCallback(async () => {
    await Promise.all([
      retryProfile(),
      publicDetailsState.retry(),
      canViewPosts ? postsState.refetch() : Promise.resolve(),
    ]);
  }, [canViewPosts, postsState, publicDetailsState, retryProfile]);

  const isRefreshing =
    postsState.isRefreshing || publicDetailsState.isLoading;

  const listHeader = useMemo(() => {
    if (!profile) return null;

    return (
      <View>
        <ProfileHeader
          profile={profile}
          details={details}
          isDetailsLoading={!isSelf && publicDetailsState.isLoading}
          detailsError={
            !isSelf && publicDetailsState.isError && publicDetailsState.errorCode !== 404
              ? publicDetailsState.errorMessage
              : ""
          }
          postCount={postsState.meta?.totalElements ?? null}
          onFollowPress={() => toggleFollow(profile)}
          isFollowLoading={isFollowLoading}
          onFollowersPress={onFollowersPress}
          onFollowingPress={onFollowingPress}
        />

        <View style={styles.postsSection}>
          <Text style={styles.postsTitle}>Bài viết</Text>
          {isSelf && canViewPosts ? (
            <ProfilePostsFilter
              value={statusFilter}
              onChange={setStatusFilter}
              disabled={postsState.isInitialLoading}
            />
          ) : null}
        </View>

        {postsState.isInitialLoading ? (
          <View style={styles.postsLoading}>
            <ActivityIndicator color={colors.primary} />
          </View>
        ) : null}

        {!canViewPosts ? <ProfilePostsEmpty isPrivateLocked /> : null}

        {canViewPosts && postsState.errorMessage ? (
          <ProfilePostsError
            message={postsState.errorMessage}
            errorCode={postsState.errorCode}
            onRetry={postsState.retry}
          />
        ) : null}

        {canViewPosts &&
        !postsState.isInitialLoading &&
        !postsState.errorMessage &&
        postsState.items.length === 0 ? (
          <ProfilePostsEmpty isPrivateLocked={false} />
        ) : null}
      </View>
    );
  }, [
    profile,
    details,
    isSelf,
    publicDetailsState,
    postsState,
    statusFilter,
    canViewPosts,
    toggleFollow,
    isFollowLoading,
    onFollowersPress,
    onFollowingPress,
  ]);

  if (isProfileLoading && !profile) {
    return (
      <View style={[styles.centered, { paddingTop: insets.top }]}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={styles.loadingText}>Đang tải hồ sơ…</Text>
      </View>
    );
  }

  if (isProfileError) {
    return (
      <View style={[styles.centered, { paddingTop: insets.top }]}>
        <Text style={styles.errorText}>{profileErrorMessage}</Text>
        {profileErrorCode !== 404 ? (
          <Pressable style={styles.retryButton} onPress={retryProfile}>
            <Text style={styles.retryButtonText}>Thử lại</Text>
          </Pressable>
        ) : (
          <Pressable style={styles.retryButton} onPress={() => router.back()}>
            <Text style={styles.retryButtonText}>Quay lại</Text>
          </Pressable>
        )}
      </View>
    );
  }

  return (
    <FlatList
      data={canViewPosts && !postsState.errorMessage ? postsState.items : []}
      keyExtractor={(item) => item.postId}
      renderItem={({ item }) => (
        <PostCard
          post={item}
          currentUserId={currentUserId}
          onOpenPost={onOpenPost}
          onViewProfile={onViewProfile}
          onEditPost={onEditPost}
          onHashtagClick={onHashtagClick}
          onToggleLike={toggleLike}
          onToggleSave={toggleSave}
          onDeletePost={(postId) => confirmDelete(postId)}
          isLikingPost={isLikingPost(item.postId)}
          isSavingPost={isSavingPost(item.postId)}
          isDeletingPost={isDeletingPost(item.postId)}
        />
      )}
      ListHeaderComponent={listHeader}
      contentContainerStyle={styles.listContent}
      refreshControl={
        <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
      }
      onEndReached={() => {
        if (canViewPosts && postsState.hasNext) postsState.loadMore();
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
    paddingBottom: 24,
    flexGrow: 1,
    backgroundColor: colors.surface,
  },
  centered: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
    backgroundColor: colors.surface,
    gap: 12,
  },
  loadingText: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
  },
  postsSection: {
    marginTop: 8,
    borderTopWidth: 1,
    borderTopColor: colors.outlineVariant,
    paddingTop: 16,
  },
  postsTitle: {
    fontSize: 17,
    fontWeight: "600",
    color: colors.onSurface,
    paddingHorizontal: 16,
    marginBottom: 8,
  },
  postsLoading: {
    paddingVertical: 24,
  },
  messageCard: {
    marginHorizontal: 16,
    marginTop: 8,
    marginBottom: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
    padding: 20,
    alignItems: "center",
  },
  errorCard: {
    borderColor: colors.error,
    backgroundColor: colors.errorContainer,
  },
  emptyTitle: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.onSurface,
    textAlign: "center",
    marginBottom: 6,
  },
  emptyText: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
    textAlign: "center",
  },
  errorText: {
    fontSize: 14,
    color: colors.onErrorContainer,
    textAlign: "center",
    marginBottom: 12,
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

import { useCallback, useLayoutEffect, useMemo, useState } from "react";
import { useNavigation } from "expo-router";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
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
import { AccountSettingsHeaderButton } from "../../auth/account/components/AccountSettingsHeaderButton";
import { useAccountProfile } from "../../auth/account/hooks/useAccountProfile";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { resolveSelfProfileDetails } from "../utils/resolveProfileDetails";

function createProfileScreenStyles(colors) {
  return {
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
  };
}

function ProfilePostsEmpty({ isPrivateLocked }) {
  const styles = useThemedStyles(createProfileScreenStyles);

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
  const styles = useThemedStyles(createProfileScreenStyles);

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
  const colors = useThemeColors();
  const styles = useThemedStyles(createProfileScreenStyles);
  const insets = useSafeAreaInsets();
  const navigation = useNavigation();
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

  const isSelfCandidate = Boolean(
    currentUserId && userId && currentUserId === userId
  );

  const isSelf = profile?.followStatus === "SELF" || isSelfCandidate;

  const accountProfileState = useAccountProfile({
    enabled: isSelfCandidate,
  });

  const publicDetailsState = usePublicProfileDetails(userId, {
    enabled: Boolean(userId) && Boolean(profile) && !isSelf,
  });

  const selfDetails = useMemo(() => {
    if (!isSelf) return null;
    return resolveSelfProfileDetails(accountProfileState.profile);
  }, [accountProfileState.profile, isSelf]);

  const details = isSelf ? selfDetails : publicDetailsState.details;
  const isDetailsLoading = isSelf
    ? accountProfileState.isLoading
    : publicDetailsState.isLoading;
  const detailsError = isSelf
    ? accountProfileState.isError || accountProfileState.isEmpty
      ? accountProfileState.errorMessage
      : ""
    : publicDetailsState.isError && publicDetailsState.errorCode !== 404
      ? publicDetailsState.errorMessage
      : "";

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

  const onEditProfilePress = useCallback(() => {
    router.push(ROUTES.accountEdit);
  }, []);

  useLayoutEffect(() => {
    if (!isSelf) {
      navigation.setOptions({ headerRight: undefined });
      return;
    }

    navigation.setOptions({
      headerRight: () => <AccountSettingsHeaderButton />,
    });
  }, [isSelf, navigation]);

  const onRefresh = useCallback(async () => {
    await Promise.all([
      retryProfile(),
      isSelf ? accountProfileState.retry() : publicDetailsState.retry(),
      canViewPosts ? postsState.refetch() : Promise.resolve(),
    ]);
  }, [
    accountProfileState,
    canViewPosts,
    isSelf,
    postsState,
    publicDetailsState,
    retryProfile,
  ]);

  const isRefreshing =
    postsState.isRefreshing ||
    (isSelf ? accountProfileState.isLoading : publicDetailsState.isLoading);

  const listHeader = useMemo(() => {
    if (!profile) return null;

    return (
      <View>
        <ProfileHeader
          profile={profile}
          details={details}
          isDetailsLoading={isDetailsLoading}
          detailsError={detailsError}
          postCount={postsState.meta?.totalElements ?? null}
          onFollowPress={() => toggleFollow(profile)}
          isFollowLoading={isFollowLoading}
          onFollowersPress={onFollowersPress}
          onFollowingPress={onFollowingPress}
          onEditProfilePress={onEditProfilePress}
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
    isDetailsLoading,
    detailsError,
    isSelf,
    postsState,
    statusFilter,
    canViewPosts,
    toggleFollow,
    isFollowLoading,
    onFollowersPress,
    onFollowingPress,
    onEditProfilePress,
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

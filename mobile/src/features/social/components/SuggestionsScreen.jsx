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
import { useSuggestedUsers } from "../hooks/useSuggestedUsers";
import { ROUTES } from "../../../shared/constants/routes";
import { SocialWriteBlockedBanner } from "./SocialWriteBlockedBanner";
import { SuggestedUsersHeader } from "./SuggestedUsersHeader";
import { UserSuggestionCard } from "./UserSuggestionCard";
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
      gap: 0,
    },
    skeletonBlock: {
      paddingHorizontal: 16,
      gap: 12,
    },
    skeleton: {
      height: 68,
      borderRadius: 12,
      backgroundColor: colors.surfaceContainerHigh,
    },
    messageCard: {
      marginHorizontal: 16,
      marginTop: 8,
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

function SuggestionSkeleton() {
  const styles = useThemedStyles(createStyles);
  return <View style={styles.skeleton} />;
}

export function SuggestionsScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const {
    items,
    errorMessage,
    isInitialLoading,
    isLoadingMore,
    isRefreshing,
    hasNext,
    loadMore,
    retry,
    refetch,
    toggleFollow,
    isFollowLoading,
    followButtonLabel,
    suggestionSubtitle,
    followDisabled,
  } = useSuggestedUsers();

  const onViewProfile = (userId) => {
    if (!userId) return;
    router.push(ROUTES.userProfile(userId));
  };

  const renderHeader = () => (
    <View style={styles.headerBlock}>
      <SocialWriteBlockedBanner />
      <SuggestedUsersHeader />
    </View>
  );

  const renderEmpty = () => {
    if (isInitialLoading) {
      return (
        <View style={styles.skeletonBlock}>
          <SuggestionSkeleton />
          <SuggestionSkeleton />
          <SuggestionSkeleton />
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
        <Ionicons
          name="people-outline"
          size={36}
          color={colors.outline}
          style={styles.emptyIcon}
        />
        <Text style={styles.emptyText}>Chưa có gợi ý người dùng.</Text>
      </View>
    );
  };

  const renderFooter = () => {
    if (isInitialLoading || errorMessage) {
      return null;
    }

    if (isLoadingMore) {
      return (
        <View style={styles.footer}>
          <ActivityIndicator color={colors.primary} />
        </View>
      );
    }

    if (hasNext && items.length > 0) {
      return (
        <View style={styles.footer}>
          <Pressable style={styles.loadMoreButton} onPress={loadMore}>
            <Text style={styles.loadMoreText}>Tải thêm</Text>
          </Pressable>
        </View>
      );
    }

    return null;
  };

  return (
    <FlatList
      data={items}
      keyExtractor={(item) => item.userId}
      ListHeaderComponent={renderHeader}
      renderItem={({ item }) => (
        <UserSuggestionCard
          user={item}
          subtitle={suggestionSubtitle(item.mutualFollowCount)}
          followLabel={followButtonLabel(item.followStatus)}
          onPressProfile={onViewProfile}
          onToggleFollow={toggleFollow}
          isFollowLoading={isFollowLoading(item.userId)}
          followDisabled={followDisabled}
        />
      )}
      ListEmptyComponent={renderEmpty}
      ListFooterComponent={renderFooter}
      contentContainerStyle={styles.listContent}
      refreshControl={<RefreshControl refreshing={isRefreshing} onRefresh={refetch} />}
      onEndReached={() => {
        if (hasNext && !isLoadingMore) {
          loadMore();
        }
      }}
      onEndReachedThreshold={0.4}
    />
  );
}

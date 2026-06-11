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
import { useSuggestedUsers } from "../hooks/useSuggestedUsers";
import { ROUTES } from "../../../shared/constants/routes";
import { UserSuggestionCard } from "./UserSuggestionCard";
import { colors } from "../../../shared/theme/colors";

function SuggestionSkeleton() {
  return <View style={styles.skeleton} />;
}

export function SuggestionsScreen() {
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
  } = useSuggestedUsers();

  const onViewProfile = (userId) => {
    if (!userId) return;
    router.push(ROUTES.userProfile(userId));
  };

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
        <Text style={styles.emptyText}>Chưa có gợi ý người dùng.</Text>
      </View>
    );
  };

  return (
    <FlatList
      data={items}
      keyExtractor={(item) => item.userId}
      renderItem={({ item }) => (
        <UserSuggestionCard
          user={item}
          subtitle={suggestionSubtitle(item.mutualFollowCount)}
          followLabel={followButtonLabel(item.followStatus)}
          onPressProfile={onViewProfile}
          onToggleFollow={toggleFollow}
          isFollowLoading={isFollowLoading(item.userId)}
        />
      )}
      ListEmptyComponent={renderEmpty}
      contentContainerStyle={styles.listContent}
      refreshControl={<RefreshControl refreshing={isRefreshing} onRefresh={refetch} />}
      onEndReached={() => {
        if (hasNext) loadMore();
      }}
      onEndReachedThreshold={0.4}
      ListFooterComponent={
        isLoadingMore ? (
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
    paddingTop: 12,
    paddingBottom: 24,
    flexGrow: 1,
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
    marginTop: 16,
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

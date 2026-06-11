import { useCallback } from "react";
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { router } from "expo-router";
import { useFollowList } from "../hooks/useFollowList";
import { FollowListRow } from "./FollowListRow";
import { ROUTES } from "../../../shared/constants/routes";
import { colors } from "../../../shared/theme/colors";

const TITLES = {
  followers: "Người theo dõi",
  following: "Đang theo dõi",
};

const SEARCH_PLACEHOLDERS = {
  followers: "Tìm kiếm người theo dõi...",
  following: "Tìm kiếm đang theo dõi...",
};

export function FollowListScreen({ userId, relationType }) {
  const {
    items,
    searchQuery,
    setSearchQuery,
    errorMessage,
    errorCode,
    isInitialLoading,
    isLoadingMore,
    isRefreshing,
    hasNext,
    loadMore,
    retry,
    refetch,
  } = useFollowList(userId, relationType);

  const onViewProfile = useCallback((targetUserId) => {
    if (!targetUserId) return;
    router.push(ROUTES.userProfile(targetUserId));
  }, []);

  const renderEmpty = () => {
    if (isInitialLoading) {
      return (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      );
    }

    if (errorMessage) {
      return (
        <View style={styles.messageCard}>
          <Text style={styles.errorText}>{errorMessage}</Text>
          {errorCode !== 403 ? (
            <Pressable style={styles.retryButton} onPress={retry}>
              <Text style={styles.retryButtonText}>Thử lại</Text>
            </Pressable>
          ) : null}
        </View>
      );
    }

    return (
      <View style={styles.messageCard}>
        <Text style={styles.emptyText}>
          {searchQuery.trim()
            ? "Không tìm thấy kết quả phù hợp."
            : "Chưa có ai trong danh sách này."}
        </Text>
      </View>
    );
  };

  return (
    <View style={styles.root}>
      <View style={styles.searchWrap}>
        <TextInput
          value={searchQuery}
          onChangeText={setSearchQuery}
          placeholder={SEARCH_PLACEHOLDERS[relationType]}
          placeholderTextColor={colors.outline}
          style={styles.searchInput}
        />
      </View>

      <FlatList
        data={errorMessage ? [] : items}
        keyExtractor={(item) => item.userId}
        renderItem={({ item }) => (
          <FollowListRow item={item} onPress={onViewProfile} />
        )}
        ListEmptyComponent={renderEmpty}
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
        contentContainerStyle={items.length === 0 ? styles.emptyContent : undefined}
      />
    </View>
  );
}

export function getFollowListTitle(relationType) {
  return TITLES[relationType] || "Kết nối";
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.surface,
  },
  searchWrap: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.outlineVariant,
  },
  searchInput: {
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 8,
    backgroundColor: colors.surfaceContainerLowest,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 14,
    color: colors.onSurface,
  },
  centered: {
    paddingVertical: 48,
    alignItems: "center",
  },
  messageCard: {
    margin: 16,
    padding: 20,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
    alignItems: "center",
  },
  emptyContent: {
    flexGrow: 1,
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
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  retryButtonText: {
    color: colors.onPrimary,
    fontWeight: "600",
  },
  footer: {
    paddingVertical: 16,
  },
});

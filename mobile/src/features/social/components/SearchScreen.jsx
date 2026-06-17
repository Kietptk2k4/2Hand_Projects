import { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  Text,
  View,
} from "react-native";
import { router, useLocalSearchParams } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import { usePostScreenActions } from "../hooks/usePostScreenActions";
import { useSearchPosts } from "../hooks/useSearchPosts";
import { FeedPostSkeleton } from "./FeedPostSkeleton";
import { PostCard } from "./PostCard";
import { SearchBar } from "./SearchBar";
import { SearchDiscoveryPanel } from "./SearchDiscoveryPanel";
import { SearchResultsHeader } from "./SearchResultsHeader";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    root: {
      flex: 1,
      backgroundColor: colors.surface,
    },
    searchSection: {
      paddingHorizontal: 16,
      paddingTop: 12,
      paddingBottom: 8,
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
    },
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
    emptyIcon: {
      marginBottom: 8,
    },
    emptyText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
      lineHeight: 20,
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

export function SearchScreen() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { q: urlQ } = useLocalSearchParams();
  const initialQuery = String(urlQ ?? "");

  const [inputValue, setInputValue] = useState(initialQuery);
  const debouncedQuery = useDebouncedValue(inputValue);
  const postsState = useSearchPosts(debouncedQuery);
  const actions = usePostScreenActions();

  const trimmedInput = inputValue.trim();
  const trimmedDebounced = debouncedQuery.trim();
  const isDebouncing = trimmedInput !== trimmedDebounced;
  const displayKeyword = postsState.keyword || trimmedDebounced || trimmedInput;

  useEffect(() => {
    setInputValue(String(urlQ ?? ""));
  }, [urlQ]);

  useEffect(() => {
    const nextQ = trimmedDebounced;
    const currentUrlQ = String(urlQ ?? "").trim();
    if (nextQ === currentUrlQ) return;

    if (nextQ) {
      router.setParams({ q: nextQ });
    } else {
      router.setParams({ q: "" });
    }
  }, [trimmedDebounced, urlQ]);

  const onOpenLikesList = useCallback(({ type, targetId, likeCount }) => {
    if (!targetId) return;
    router.push(
      ROUTES.postLikes(targetId, {
        targetType: type || "post",
        likeCount: likeCount ?? 0,
      })
    );
  }, []);

  const handleSelectKeyword = useCallback((keyword) => {
    const trimmed = String(keyword || "").trim();
    if (!trimmed) return;
    setInputValue(trimmed);
    router.setParams({ q: trimmed });
  }, []);

  const handleSelectHashtag = useCallback((tag) => {
    if (!tag) return;
    router.push(ROUTES.hashtag(tag));
  }, []);

  const handleClearSearch = useCallback(() => {
    setInputValue("");
    router.setParams({ q: "" });
  }, []);

  const renderHeader = () => (
    <View>
      <SearchDiscoveryPanel
        onSelectKeyword={handleSelectKeyword}
        onSelectHashtag={handleSelectHashtag}
        refreshKey={displayKeyword}
      />
      <SearchResultsHeader
        keyword={displayKeyword}
        totalElements={postsState.totalElements}
      />
    </View>
  );

  const renderEmpty = () => {
    if (!trimmedDebounced) {
      return (
        <View style={styles.messageCard}>
          <Ionicons
            name="search-outline"
            size={36}
            color={colors.outline}
            style={styles.emptyIcon}
          />
          <Text style={styles.emptyText}>Nhập từ khóa để tìm bài viết</Text>
        </View>
      );
    }

    if (postsState.isInitialLoading || isDebouncing) {
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
        <Ionicons
          name="search-outline"
          size={36}
          color={colors.outline}
          style={styles.emptyIcon}
        />
        <Text style={styles.emptyText}>
          Không tìm thấy bài viết cho "{postsState.keyword || trimmedDebounced}"
        </Text>
      </View>
    );
  };

  const renderFooter = () => {
    if (!trimmedDebounced || postsState.isInitialLoading || postsState.errorMessage) {
      return null;
    }

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
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <View style={styles.searchSection}>
        <SearchBar
          value={inputValue}
          onChangeText={setInputValue}
          onClear={handleClearSearch}
          placeholder="Tìm bài viết..."
          autoFocus={!initialQuery}
        />
      </View>

      <FlatList
        data={trimmedDebounced && !isDebouncing ? postsState.items : []}
        keyExtractor={(item) => item.postId}
        ListHeaderComponent={renderHeader}
        ListEmptyComponent={renderEmpty}
        ListFooterComponent={renderFooter}
        contentContainerStyle={styles.listContent}
        onEndReached={() => {
          if (postsState.hasNext && !postsState.isLoadingMore) {
            postsState.loadMore();
          }
        }}
        onEndReachedThreshold={0.4}
        keyboardShouldPersistTaps="handled"
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
            onOpenLikesList={onOpenLikesList}
            isLikingPost={actions.isLikingPost(item.postId)}
            isSavingPost={actions.isSavingPost(item.postId)}
            isDeletingPost={actions.isDeletingPost(item.postId)}
          />
        )}
      />
    </KeyboardAvoidingView>
  );
}

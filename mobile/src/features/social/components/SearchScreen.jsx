import { useState } from "react";
import {
  ActivityIndicator,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import { usePostScreenActions } from "../hooks/usePostScreenActions";
import { useSearchPosts } from "../hooks/useSearchPosts";
import { useSearchUsers } from "../hooks/useSearchUsers";
import { FeedPostSkeleton } from "./FeedPostSkeleton";
import { PostCard } from "./PostCard";
import { SearchBar } from "./SearchBar";
import { colors } from "../../../shared/theme/colors";

const TABS = {
  POSTS: "posts",
  USERS: "users",
};

export function SearchScreen() {
  const [inputValue, setInputValue] = useState("");
  const [activeTab, setActiveTab] = useState(TABS.POSTS);
  const debouncedQuery = useDebouncedValue(inputValue);
  const postsState = useSearchPosts(debouncedQuery);
  const usersState = useSearchUsers(debouncedQuery);
  const actions = usePostScreenActions();

  const trimmedInput = inputValue.trim();
  const trimmedDebounced = debouncedQuery.trim();
  const isDebouncing = trimmedInput !== trimmedDebounced;
  const showPostsTab = activeTab === TABS.POSTS;

  const renderPostsEmpty = () => {
    if (!trimmedDebounced) {
      return (
        <View style={styles.messageCard}>
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
        <Text style={styles.emptyText}>
          Không tìm thấy bài viết cho "{postsState.keyword || trimmedDebounced}"
        </Text>
      </View>
    );
  };

  const renderUsersEmpty = () => {
    if (!usersState.isAvailable) {
      return (
        <View style={styles.messageCard}>
          <Text style={styles.emptyText}>
            Tìm kiếm người dùng chưa khả dụng trên mobile (API chưa có trên social-service).
          </Text>
        </View>
      );
    }

    return (
      <View style={styles.messageCard}>
        <Text style={styles.emptyText}>Không tìm thấy người dùng phù hợp.</Text>
      </View>
    );
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
          onClear={() => setInputValue("")}
          placeholder="Tìm bài viết..."
          autoFocus
        />

        <View style={styles.tabs}>
          <Pressable
            style={[styles.tab, showPostsTab && styles.tabActive]}
            onPress={() => setActiveTab(TABS.POSTS)}
          >
            <Text style={[styles.tabText, showPostsTab && styles.tabTextActive]}>Bài viết</Text>
          </Pressable>
          <Pressable
            style={[styles.tab, !showPostsTab && styles.tabActive]}
            onPress={() => setActiveTab(TABS.USERS)}
          >
            <Text style={[styles.tabText, !showPostsTab && styles.tabTextActive]}>
              Người dùng
            </Text>
          </Pressable>
        </View>
      </View>

      {showPostsTab ? (
        <FlatList
          data={trimmedDebounced ? postsState.items : []}
          keyExtractor={(item) => item.postId}
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
              isLikingPost={actions.isLikingPost(item.postId)}
              isSavingPost={actions.isSavingPost(item.postId)}
              isDeletingPost={actions.isDeletingPost(item.postId)}
            />
          )}
          ListEmptyComponent={renderPostsEmpty}
          contentContainerStyle={styles.listContent}
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
          keyboardShouldPersistTaps="handled"
        />
      ) : (
        <FlatList
          data={[]}
          renderItem={null}
          ListEmptyComponent={renderUsersEmpty}
          contentContainerStyle={styles.listContent}
        />
      )}
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.surface,
  },
  searchSection: {
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 8,
    gap: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
  },
  tabs: {
    flexDirection: "row",
    gap: 8,
  },
  tab: {
    flex: 1,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    paddingVertical: 8,
    alignItems: "center",
  },
  tabActive: {
    borderColor: colors.primary,
    backgroundColor: colors.surfaceContainerLow,
  },
  tabText: {
    fontSize: 14,
    fontWeight: "500",
    color: colors.onSurfaceVariant,
  },
  tabTextActive: {
    color: colors.primary,
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
  },
});

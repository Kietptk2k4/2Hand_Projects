import {
  ActivityIndicator,
  FlatList,
  Pressable,
  Text,
  TextInput,
  View,
} from "react-native";
import { router } from "expo-router";
import { useLikeUsersList } from "../hooks/useLikeUsersList";
import { ROUTES } from "../../../shared/constants/routes";
import { LikesListRow } from "./LikesListRow";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    root: {
      flex: 1,
      backgroundColor: colors.surface,
    },
    searchWrap: {
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
      paddingHorizontal: 16,
      paddingVertical: 12,
    },
    searchInput: {
      borderRadius: 10,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      paddingHorizontal: 14,
      paddingVertical: 10,
      fontSize: 14,
      color: colors.onSurface,
    },
    listContent: {
      flexGrow: 1,
      paddingBottom: 24,
    },
    centered: {
      padding: 32,
      alignItems: "center",
      gap: 12,
    },
    mutedText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
    },
    errorText: {
      fontSize: 14,
      color: colors.onErrorContainer,
      textAlign: "center",
    },
    retryBtn: {
      marginTop: 8,
      paddingHorizontal: 16,
      paddingVertical: 10,
      borderRadius: 8,
      backgroundColor: colors.primary,
    },
    retryText: {
      color: colors.onPrimary,
      fontSize: 14,
      fontWeight: "600",
    },
    footer: {
      paddingVertical: 16,
      alignItems: "center",
    },
    loadMoreBtn: {
      paddingVertical: 8,
      paddingHorizontal: 16,
    },
    loadMoreText: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.primary,
    },
  };
}

export function LikesListScreen({ targetType = "post", targetId, likeCount = 0 }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const likes = useLikeUsersList(targetType, targetId, {
    enabled: Boolean(targetType && targetId),
  });

  const onViewProfile = (userId) => {
    if (!userId) return;
    router.push(ROUTES.userProfile(userId));
  };

  const renderEmpty = () => {
    if (likes.isInitialLoading) {
      return (
        <View style={styles.centered}>
          <ActivityIndicator color={colors.primary} />
        </View>
      );
    }

    if (likes.errorMessage) {
      return (
        <View style={styles.centered}>
          <Text style={styles.errorText}>{likes.errorMessage}</Text>
          {likes.errorCode !== 403 ? (
            <Pressable style={styles.retryBtn} onPress={likes.retry}>
              <Text style={styles.retryText}>Thử lại</Text>
            </Pressable>
          ) : null}
        </View>
      );
    }

    return (
      <View style={styles.centered}>
        <Text style={styles.mutedText}>
          {likes.searchQuery.trim()
            ? "Không tìm thấy kết quả phù hợp."
            : likeCount > 0
              ? "Chưa tải được danh sách."
              : "Chưa có ai thích."}
        </Text>
      </View>
    );
  };

  return (
    <View style={styles.root}>
      <View style={styles.searchWrap}>
        <TextInput
          value={likes.searchQuery}
          onChangeText={likes.setSearchQuery}
          placeholder="Tìm kiếm..."
          placeholderTextColor={colors.onSurfaceVariant}
          style={styles.searchInput}
          autoCapitalize="none"
          autoCorrect={false}
        />
      </View>

      <FlatList
        data={likes.items}
        keyExtractor={(item) => item.userId}
        renderItem={({ item }) => (
          <LikesListRow item={item} onViewProfile={onViewProfile} />
        )}
        ListEmptyComponent={renderEmpty}
        contentContainerStyle={styles.listContent}
        onEndReached={() => {
          if (likes.hasNext) likes.loadMore();
        }}
        onEndReachedThreshold={0.4}
        ListFooterComponent={
          likes.isLoadingMore ? (
            <View style={styles.footer}>
              <ActivityIndicator color={colors.primary} />
            </View>
          ) : likes.hasNext && likes.items.length > 0 ? (
            <Pressable style={styles.loadMoreBtn} onPress={likes.loadMore}>
              <Text style={styles.loadMoreText}>Tải thêm</Text>
            </Pressable>
          ) : null
        }
      />
    </View>
  );
}

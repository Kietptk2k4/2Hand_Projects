import { ActivityIndicator, Pressable, Text, View } from "react-native";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useFeedDiscoverySuggestions } from "../hooks/useFeedDiscoverySuggestions";
import { useTrendingHashtags } from "../hooks/useTrendingHashtags";
import { UserSuggestionCard } from "./UserSuggestionCard";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    section: {
      gap: 12,
    },
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
    },
    cardTitle: {
      fontSize: 17,
      fontWeight: "600",
      color: colors.onSurface,
      marginBottom: 12,
    },
    mutedText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
    },
    errorText: {
      fontSize: 14,
      color: colors.error,
    },
    hashtagList: {
      gap: 10,
    },
    hashtagRow: {
      gap: 2,
    },
    hashtagTag: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.primary,
    },
    hashtagCount: {
      fontSize: 12,
      fontWeight: "600",
      color: colors.onSurfaceVariant,
    },
    seeMoreBtn: {
      alignItems: "center",
      paddingVertical: 10,
      marginTop: 4,
    },
    seeMoreText: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.primary,
    },
    suggestionWrap: {
      marginHorizontal: -16,
    },
  };
}

export function FeedDiscoverySection({ onViewProfile }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const trending = useTrendingHashtags({ limit: 5 });
  const suggestions = useFeedDiscoverySuggestions();

  const onHashtagPress = (tag) => {
    if (!tag) return;
    router.push(ROUTES.hashtag(tag));
  };

  const onSeeAllSuggestions = () => {
    router.push(ROUTES.suggestions);
  };

  return (
    <View style={styles.section}>
      <View style={styles.card}>
        <Text style={styles.cardTitle}>Đang thịnh hành</Text>
        {trending.isLoading ? (
          <Text style={styles.mutedText}>Đang tải...</Text>
        ) : trending.isError ? (
          <Text style={styles.errorText}>{trending.errorMessage}</Text>
        ) : trending.items.length === 0 ? (
          <Text style={styles.mutedText}>Chưa có hashtag thịnh hành.</Text>
        ) : (
          <View style={styles.hashtagList}>
            {trending.items.map((item) => (
              <Pressable
                key={item.tag}
                style={styles.hashtagRow}
                onPress={() => onHashtagPress(item.tag)}
              >
                <Text style={styles.hashtagTag}>#{item.tag}</Text>
                <Text style={styles.hashtagCount}>
                  {trending.formatPostCount(item.postCount)}
                </Text>
              </Pressable>
            ))}
          </View>
        )}
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Những người bạn có thể biết</Text>
        {suggestions.isLoading ? (
          <ActivityIndicator color={colors.primary} />
        ) : suggestions.isError ? (
          <Text style={styles.errorText}>{suggestions.errorMessage}</Text>
        ) : suggestions.items.length === 0 ? (
          <Text style={styles.mutedText}>Chưa có gợi ý người dùng.</Text>
        ) : (
          <View style={styles.suggestionWrap}>
            {suggestions.items.map((user) => (
              <UserSuggestionCard
                key={user.userId}
                user={user}
                subtitle={suggestions.suggestionSubtitle(user.mutualFollowCount)}
                followLabel={suggestions.followButtonLabel(user.followStatus)}
                onPressProfile={onViewProfile}
                onToggleFollow={suggestions.toggleFollow}
                isFollowLoading={suggestions.isFollowLoading(user.userId)}
                followDisabled={suggestions.followDisabled}
              />
            ))}
          </View>
        )}
        {suggestions.hasMore && suggestions.items.length > 0 ? (
          <Pressable style={styles.seeMoreBtn} onPress={onSeeAllSuggestions}>
            <Text style={styles.seeMoreText}>Xem tất cả gợi ý</Text>
          </Pressable>
        ) : null}
      </View>
    </View>
  );
}

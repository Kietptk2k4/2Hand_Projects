import { useCallback, useEffect, useState } from "react";
import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { POPULAR_SEARCH_HASHTAGS } from "../constants/searchPostsConstants";
import { clearSearchHistory, getSearchHistory } from "../utils/searchHistoryStorage";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    section: {
      gap: 12,
      paddingBottom: 8,
    },
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
    },
    cardHeader: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      marginBottom: 12,
    },
    cardTitle: {
      fontSize: 12,
      fontWeight: "600",
      letterSpacing: 0.6,
      textTransform: "uppercase",
      color: colors.onSurface,
    },
    clearText: {
      fontSize: 12,
      fontWeight: "600",
      color: colors.primary,
    },
    mutedText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
    },
    historyList: {
      gap: 4,
    },
    historyItem: {
      flexDirection: "row",
      alignItems: "center",
      gap: 10,
      borderRadius: 8,
      paddingHorizontal: 8,
      paddingVertical: 10,
      minHeight: 44,
    },
    historyItemPressed: {
      backgroundColor: colors.surfaceContainerLow,
    },
    historyText: {
      flex: 1,
      fontSize: 14,
      color: colors.onSurface,
    },
    hashtagWrap: {
      flexDirection: "row",
      flexWrap: "wrap",
      gap: 8,
    },
    hashtagChip: {
      borderRadius: 999,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      paddingHorizontal: 12,
      paddingVertical: 6,
    },
    hashtagText: {
      fontSize: 13,
      fontWeight: "600",
      color: colors.primary,
    },
  };
}

export function SearchDiscoveryPanel({
  onSelectKeyword,
  onSelectHashtag,
  refreshKey = "",
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [history, setHistory] = useState([]);

  const refreshHistory = useCallback(async () => {
    const items = await getSearchHistory();
    setHistory(items);
  }, []);

  useEffect(() => {
    refreshHistory();
  }, [refreshKey, refreshHistory]);

  const handleClearHistory = async () => {
    await clearSearchHistory();
    await refreshHistory();
  };

  const handleSelect = async (keyword) => {
    onSelectKeyword?.(keyword);
    await refreshHistory();
  };

  return (
    <View style={styles.section}>
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.cardTitle}>Lịch sử tìm kiếm</Text>
          {history.length > 0 ? (
            <Pressable onPress={handleClearHistory} accessibilityRole="button">
              <Text style={styles.clearText}>Xóa lịch sử</Text>
            </Pressable>
          ) : null}
        </View>

        {history.length === 0 ? (
          <Text style={styles.mutedText}>Chưa có lịch sử tìm kiếm.</Text>
        ) : (
          <View style={styles.historyList}>
            {history.map((item) => (
              <Pressable
                key={item}
                style={({ pressed }) => [
                  styles.historyItem,
                  pressed && styles.historyItemPressed,
                ]}
                onPress={() => handleSelect(item)}
                accessibilityRole="button"
              >
                <Ionicons name="time-outline" size={18} color={colors.onSurfaceVariant} />
                <Text style={styles.historyText} numberOfLines={1}>
                  {item}
                </Text>
              </Pressable>
            ))}
          </View>
        )}
      </View>

      <View style={styles.card}>
        <Text style={[styles.cardTitle, { marginBottom: 12 }]}>Hashtag phổ biến</Text>
        <View style={styles.hashtagWrap}>
          {POPULAR_SEARCH_HASHTAGS.map((tag) => (
            <Pressable
              key={tag}
              style={styles.hashtagChip}
              onPress={() => onSelectHashtag?.(tag)}
              accessibilityRole="button"
            >
              <Text style={styles.hashtagText}>#{tag}</Text>
            </Pressable>
          ))}
        </View>
      </View>
    </View>
  );
}

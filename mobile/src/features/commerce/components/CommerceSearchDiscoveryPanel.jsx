import { useCallback, useEffect, useState } from "react";
import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import {
  clearCommerceSearchHistory,
  getCommerceSearchHistory,
} from "../utils/commerceSearchHistoryStorage";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    section: { gap: 12 },
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
    },
    cardMuted: { opacity: 0.7 },
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
    clearText: { fontSize: 12, fontWeight: "600", color: colors.primary },
    mutedText: { fontSize: 14, color: colors.onSurfaceVariant },
    historyList: { gap: 4 },
    historyItem: {
      flexDirection: "row",
      alignItems: "center",
      gap: 10,
      borderRadius: 8,
      paddingHorizontal: 8,
      paddingVertical: 10,
      minHeight: 44,
    },
    historyItemPressed: { backgroundColor: colors.surfaceContainerLow },
    historyText: { flex: 1, fontSize: 14, color: colors.onSurface },
    link: { fontSize: 14, fontWeight: "600", color: colors.primary },
  };
}

export function CommerceSearchDiscoveryPanel({
  onSelectKeyword,
  onComingSoon,
  refreshKey = "",
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [history, setHistory] = useState([]);

  const refreshHistory = useCallback(async () => {
    const items = await getCommerceSearchHistory();
    setHistory(items);
  }, []);

  useEffect(() => {
    refreshHistory();
  }, [refreshHistory, refreshKey]);

  const handleClear = async () => {
    await clearCommerceSearchHistory();
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
          <Text style={styles.cardTitle}>Tìm kiếm gần đây</Text>
          {history.length > 0 ? (
            <Pressable onPress={handleClear} accessibilityRole="button">
              <Text style={styles.clearText}>Xóa</Text>
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
                  pressed ? styles.historyItemPressed : null,
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

      <View style={[styles.card, styles.cardMuted]}>
        <Text style={styles.cardTitle}>Bộ lọc</Text>
        <Text style={[styles.mutedText, { marginTop: 8, marginBottom: 12 }]}>
          Lọc theo giá, tình trạng và đánh giá sẽ sớm có mặt.
        </Text>
        <Pressable onPress={onComingSoon} accessibilityRole="button">
          <Text style={styles.link}>Tính năng đang phát triển</Text>
        </Pressable>
      </View>
    </View>
  );
}

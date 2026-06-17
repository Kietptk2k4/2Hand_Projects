import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { TRENDING_HASHTAGS } from "../constants/hashtagPostsConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    card: {
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      marginBottom: 16,
    },
    titleRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 8,
      marginBottom: 12,
    },
    title: {
      fontSize: 17,
      fontWeight: "600",
      color: colors.onSurface,
    },
    chipWrap: {
      flexDirection: "row",
      flexWrap: "wrap",
      gap: 8,
    },
    chip: {
      borderRadius: 999,
      paddingHorizontal: 12,
      paddingVertical: 6,
      backgroundColor: colors.surfaceContainer,
    },
    chipActive: {
      backgroundColor: colors.primary,
    },
    chipText: {
      fontSize: 12,
      fontWeight: "600",
      color: colors.onSurface,
    },
    chipTextActive: {
      color: colors.onPrimary,
    },
  };
}

export function HashtagDiscoveryPanel({ currentHashtag, onSelectTag }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const activeTag = String(currentHashtag || "").toLowerCase();

  return (
    <View style={styles.card}>
      <View style={styles.titleRow}>
        <Ionicons name="trending-up" size={20} color={colors.primary} />
        <Text style={styles.title}>Hashtag thịnh hành</Text>
      </View>
      <View style={styles.chipWrap}>
        {TRENDING_HASHTAGS.map((tag) => {
          const isActive = activeTag === tag.toLowerCase();
          return (
            <Pressable
              key={tag}
              style={[styles.chip, isActive && styles.chipActive]}
              onPress={() => onSelectTag?.(tag)}
              accessibilityRole="button"
            >
              <Text style={[styles.chipText, isActive && styles.chipTextActive]}>
                #{tag}
              </Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

import { useEffect, useState } from "react";
import { Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { SearchBar } from "./SearchBar";
import { formatHashtagLabel } from "../utils/normalizeHashtag";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    root: {
      borderBottomWidth: 1,
      borderBottomColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      paddingHorizontal: 16,
      paddingTop: 12,
      paddingBottom: 12,
      gap: 12,
    },
    topRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
    },
    iconWrap: {
      width: 48,
      height: 48,
      borderRadius: 12,
      alignItems: "center",
      justifyContent: "center",
      backgroundColor: colors.surfaceContainerHigh,
    },
    titleBlock: {
      flex: 1,
      minWidth: 0,
    },
    title: {
      fontSize: 22,
      fontWeight: "600",
      color: colors.onSurface,
    },
    subtitle: {
      marginTop: 2,
      fontSize: 14,
      color: colors.onSurfaceVariant,
    },
  };
}

export function HashtagPageHeader({ hashtag, totalElements = 0, onSearchTag }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [tagInput, setTagInput] = useState("");

  useEffect(() => {
    setTagInput("");
  }, [hashtag]);

  if (!hashtag) return null;

  const handleSubmit = () => {
    const trimmed = tagInput.trim();
    if (!trimmed) return;
    onSearchTag?.(trimmed);
  };

  return (
    <View style={styles.root}>
      <View style={styles.topRow}>
        <View style={styles.iconWrap}>
          <Ionicons name="pricetag" size={28} color={colors.primary} />
        </View>
        <View style={styles.titleBlock}>
          <Text style={styles.title} numberOfLines={1}>
            {formatHashtagLabel(hashtag)}
          </Text>
          <Text style={styles.subtitle}>{totalElements} bài viết</Text>
        </View>
      </View>

      <SearchBar
        value={tagInput}
        onChangeText={setTagInput}
        onClear={() => setTagInput("")}
        placeholder="Tìm hashtag khác..."
        onSubmitEditing={handleSubmit}
      />
    </View>
  );
}

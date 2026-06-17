import { Pressable, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles() {
  return {
    row: {
      flexDirection: "row",
      justifyContent: "flex-end",
      gap: 4,
      paddingHorizontal: 4,
    },
    btn: {
      width: 40,
      height: 40,
      borderRadius: 20,
      alignItems: "center",
      justifyContent: "center",
    },
  };
}

export function FeedHeaderActions() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const iconColor = colors.onSurfaceVariant;

  return (
    <View style={styles.row}>
      <Pressable
        style={styles.btn}
        onPress={() => router.push(ROUTES.search())}
        accessibilityLabel="Tìm kiếm"
      >
        <Ionicons name="search-outline" size={22} color={iconColor} />
      </Pressable>
      <Pressable
        style={styles.btn}
        onPress={() => router.push(ROUTES.saved)}
        accessibilityLabel="Bài đã lưu"
      >
        <Ionicons name="bookmark-outline" size={22} color={iconColor} />
      </Pressable>
      <Pressable
        style={styles.btn}
        onPress={() => router.push(ROUTES.suggestions)}
        accessibilityLabel="Gợi ý người dùng"
      >
        <Ionicons name="people-outline" size={22} color={iconColor} />
      </Pressable>
    </View>
  );
}

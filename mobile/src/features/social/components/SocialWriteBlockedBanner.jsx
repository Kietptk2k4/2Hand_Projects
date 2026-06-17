import { useState } from "react";
import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    banner: {
      flexDirection: "row",
      alignItems: "flex-start",
      gap: 10,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.secondary,
      backgroundColor: colors.surfaceContainerLow,
      paddingHorizontal: 14,
      paddingVertical: 12,
    },
    message: {
      flex: 1,
      fontSize: 14,
      lineHeight: 20,
      color: colors.onSurface,
    },
    closeBtn: {
      width: 32,
      height: 32,
      alignItems: "center",
      justifyContent: "center",
      borderRadius: 16,
    },
  };
}

export function SocialWriteBlockedBanner() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const { isWriteBlocked, suspendMessage } = useSocialWriteBlock();
  const [dismissed, setDismissed] = useState(false);

  if (!isWriteBlocked || dismissed) {
    return null;
  }

  return (
    <View style={styles.banner} accessibilityRole="text">
      <Ionicons name="information-circle" size={20} color={colors.secondary} />
      <Text style={styles.message}>
        {suspendMessage || "Tài khoản bị đình chỉ. Bạn chỉ có thể xem nội dung."}
      </Text>
      <Pressable
        style={styles.closeBtn}
        onPress={() => setDismissed(true)}
        accessibilityRole="button"
        accessibilityLabel="Đóng thông báo"
      >
        <Ionicons name="close" size={18} color={colors.onSurfaceVariant} />
      </Pressable>
    </View>
  );
}

import { Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { useAccountProfile } from "../../auth/account/hooks/useAccountProfile";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { useCurrentUserId } from "../hooks/useCurrentUserId";
import { ROUTES } from "../../../shared/constants/routes";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

const DEFAULT_AVATAR = "https://i.pravatar.cc/96?img=11";

function createStyles(colors) {
  return {
    card: {
      flexDirection: "row",
      gap: 12,
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      opacity: 1,
    },
    cardBlocked: {
      opacity: 0.72,
    },
    avatarRing: {
      borderRadius: 24,
      borderWidth: 2,
      borderColor: colors.primary,
      padding: 2,
    },
    avatar: {
      width: 40,
      height: 40,
      borderRadius: 20,
      backgroundColor: colors.surfaceContainerHigh,
    },
    avatarFallback: {
      width: 40,
      height: 40,
      borderRadius: 20,
      backgroundColor: colors.surfaceContainerHigh,
      alignItems: "center",
      justifyContent: "center",
    },
    avatarFallbackText: {
      fontSize: 12,
      fontWeight: "600",
      color: colors.primary,
    },
    body: {
      flex: 1,
      gap: 12,
    },
    input: {
      borderRadius: 8,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      paddingHorizontal: 14,
      paddingVertical: 12,
      minHeight: 44,
      justifyContent: "center",
    },
    placeholder: {
      fontSize: 15,
      color: colors.onSurfaceVariant,
    },
    actions: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
    },
    iconRow: {
      flexDirection: "row",
      gap: 4,
    },
    iconBtn: {
      width: 40,
      height: 40,
      borderRadius: 20,
      alignItems: "center",
      justifyContent: "center",
    },
    postLabel: {
      paddingHorizontal: 16,
      paddingVertical: 8,
    },
    postLabelText: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.onSurfaceVariant,
    },
    postLabelTextDisabled: {
      color: colors.outline,
    },
  };
}

export function FeedComposer({ onOpenCreatePost, onOpenCreatePostWithPicker }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const currentUserId = useCurrentUserId();
  const { isWriteBlocked, suspendMessage } = useSocialWriteBlock();
  const { profile } = useAccountProfile({ enabled: Boolean(currentUserId) });

  const avatarUrl = resolveDevMediaUrl(
    profile?.profile?.avatar_url || profile?.profile?.avatarUrl || DEFAULT_AVATAR
  );
  const displayInitial =
    (profile?.profile?.display_name || profile?.email || "B").trim().charAt(0) || "B";

  const openSelfProfile = () => {
    if (!currentUserId) return;
    router.push(ROUTES.userProfile(currentUserId));
  };

  const openModal = () => {
    if (isWriteBlocked) return;
    onOpenCreatePost?.();
  };

  const openWithPicker = () => {
    if (isWriteBlocked) return;
    onOpenCreatePostWithPicker?.();
  };

  const blockedHint = isWriteBlocked ? suspendMessage : undefined;

  return (
    <View style={[styles.card, isWriteBlocked && styles.cardBlocked]}>
      <Pressable
        style={styles.avatarRing}
        onPress={openSelfProfile}
        accessibilityLabel="Xem hồ sơ của bạn"
        accessibilityHint={blockedHint}
      >
        {profile?.profile?.avatar_url || profile?.profile?.avatarUrl ? (
          <Image source={{ uri: avatarUrl }} style={styles.avatar} />
        ) : (
          <View style={styles.avatarFallback}>
            <Text style={styles.avatarFallbackText}>{displayInitial}</Text>
          </View>
        )}
      </Pressable>

      <View style={styles.body}>
        <Pressable
          onPress={openModal}
          style={styles.input}
          disabled={isWriteBlocked}
          accessibilityState={{ disabled: isWriteBlocked }}
          accessibilityHint={blockedHint}
        >
          <Text style={styles.placeholder}>
            {isWriteBlocked
              ? "Tài khoản bị đình chỉ — chỉ xem nội dung"
              : "Bắt đầu đăng bài hoặc chia sẻ cập nhật..."}
          </Text>
        </Pressable>

        <View style={styles.actions}>
          <View style={styles.iconRow}>
            <Pressable
              onPress={openWithPicker}
              style={styles.iconBtn}
              disabled={isWriteBlocked}
              accessibilityLabel="Thêm ảnh"
              accessibilityState={{ disabled: isWriteBlocked }}
            >
              <Ionicons
                name="image-outline"
                size={22}
                color={isWriteBlocked ? colors.outline : colors.onSurfaceVariant}
              />
            </Pressable>
            <Pressable
              onPress={openModal}
              style={styles.iconBtn}
              disabled={isWriteBlocked}
              accessibilityLabel="Soạn bài viết"
              accessibilityState={{ disabled: isWriteBlocked }}
            >
              <Ionicons
                name="document-text-outline"
                size={22}
                color={isWriteBlocked ? colors.outline : colors.onSurfaceVariant}
              />
            </Pressable>
          </View>
          <Pressable onPress={openModal} style={styles.postLabel} disabled={isWriteBlocked}>
            <Text
              style={[
                styles.postLabelText,
                isWriteBlocked && styles.postLabelTextDisabled,
              ]}
            >
              Đăng bài
            </Text>
          </Pressable>
        </View>
      </View>
    </View>
  );
}

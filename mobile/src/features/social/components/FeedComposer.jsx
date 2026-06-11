import { Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { colors } from "../../../shared/theme/colors";

export function FeedComposer({ onOpenCreatePost, onOpenCreatePostWithPicker }) {
  return (
    <View style={styles.card}>
      <View style={styles.avatarRing}>
        <View style={styles.avatar}>
          <Text style={styles.avatarFallback}>Bạn</Text>
        </View>
      </View>

      <View style={styles.body}>
        <Pressable onPress={onOpenCreatePost} style={styles.input}>
          <Text style={styles.placeholder}>
            Bắt đầu đăng bài hoặc chia sẻ cập nhật...
          </Text>
        </Pressable>

        <View style={styles.actions}>
          <View style={styles.iconRow}>
            <Pressable
              onPress={onOpenCreatePostWithPicker}
              style={styles.iconBtn}
              accessibilityLabel="Thêm ảnh"
            >
              <Ionicons name="image-outline" size={22} color={colors.onSurfaceVariant} />
            </Pressable>
            <Pressable
              onPress={onOpenCreatePost}
              style={styles.iconBtn}
              accessibilityLabel="Soạn bài viết"
            >
              <Ionicons name="document-text-outline" size={22} color={colors.onSurfaceVariant} />
            </Pressable>
          </View>
          <Pressable onPress={onOpenCreatePost} style={styles.postLabel}>
            <Text style={styles.postLabelText}>Đăng bài</Text>
          </Pressable>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    gap: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
    padding: 16,
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
    alignItems: "center",
    justifyContent: "center",
    overflow: "hidden",
  },
  avatarFallback: {
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
});

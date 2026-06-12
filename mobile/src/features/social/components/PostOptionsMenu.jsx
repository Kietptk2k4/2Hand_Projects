import { useState } from "react";
import {
  ActivityIndicator,
  Modal,
  Pressable,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    trigger: {
      width: 44,
      height: 44,
      alignItems: "center",
      justifyContent: "center",
    },
    backdrop: {
      flex: 1,
      backgroundColor: "rgba(0,0,0,0.4)",
      justifyContent: "flex-end",
    },
    sheet: {
      backgroundColor: colors.surfaceContainerLowest,
      borderTopLeftRadius: 16,
      borderTopRightRadius: 16,
      paddingBottom: 24,
      paddingTop: 8,
    },
    menuItem: {
      flexDirection: "row",
      alignItems: "center",
      gap: 12,
      paddingHorizontal: 20,
      paddingVertical: 16,
      minHeight: 48,
    },
    menuItemText: {
      fontSize: 16,
      color: colors.onSurface,
    },
    menuItemDanger: {
      fontSize: 16,
      color: colors.error,
      fontWeight: "600",
    },
    cancelItem: {
      marginTop: 8,
      paddingVertical: 16,
      alignItems: "center",
      borderTopWidth: 1,
      borderTopColor: colors.outlineVariant,
    },
    cancelText: {
      fontSize: 16,
      fontWeight: "600",
      color: colors.onSurfaceVariant,
    },
  };
}

export function PostOptionsMenu({
  postId,
  isOwner = false,
  savedByMe = false,
  onEdit,
  onDelete,
  onToggleSave,
  isSaving = false,
  isDeleting = false,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [open, setOpen] = useState(false);
  const busy = isSaving || isDeleting;
  const saveLabel = savedByMe ? "Bỏ lưu" : "Lưu bài";

  const close = () => setOpen(false);

  return (
    <>
      <Pressable
        onPress={() => setOpen(true)}
        style={styles.trigger}
        accessibilityRole="button"
        accessibilityLabel="Tuy chon bai viet"
      >
        <Ionicons name="ellipsis-horizontal" size={22} color={colors.onSurfaceVariant} />
      </Pressable>

      <Modal visible={open} transparent animationType="fade" onRequestClose={close}>
        <Pressable style={styles.backdrop} onPress={close}>
          <View style={styles.sheet}>
            {isOwner ? (
              <>
                <Pressable
                  style={styles.menuItem}
                  disabled={busy}
                  onPress={() => {
                    close();
                    onEdit?.();
                  }}
                >
                  <Ionicons name="create-outline" size={20} color={colors.onSurfaceVariant} />
                  <Text style={styles.menuItemText}>Chỉnh sửa bài viết</Text>
                </Pressable>
                <Pressable
                  style={styles.menuItem}
                  disabled={busy}
                  onPress={() => {
                    close();
                    onDelete?.();
                  }}
                >
                  <Ionicons name="trash-outline" size={20} color={colors.error} />
                  <Text style={styles.menuItemDanger}>
                    {isDeleting ? "Đang xóa…" : "Xóa bài viết"}
                  </Text>
                </Pressable>
              </>
            ) : null}

            <Pressable
              style={styles.menuItem}
              disabled={busy}
              onPress={() => {
                close();
                onToggleSave?.();
              }}
            >
              {isSaving ? (
                <ActivityIndicator size="small" color={colors.primary} />
              ) : (
                <Ionicons
                  name={savedByMe ? "bookmark" : "bookmark-outline"}
                  size={20}
                  color={colors.onSurfaceVariant}
                />
              )}
              <Text style={styles.menuItemText}>
                {isSaving ? "Đang xử lý…" : saveLabel}
              </Text>
            </Pressable>

            <Pressable style={styles.cancelItem} onPress={close}>
              <Text style={styles.cancelText}>Hủy</Text>
            </Pressable>
          </View>
        </Pressable>
      </Modal>
    </>
  );
}

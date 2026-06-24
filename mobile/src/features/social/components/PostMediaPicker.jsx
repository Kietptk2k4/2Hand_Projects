import {
  ActivityIndicator,
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { colors } from "../../../shared/theme/colors";
import { isPostVideoMedia, getPostMediaUrl } from "../utils/postMediaType";

export function PostMediaPicker({
  mediaItems,
  activeMediaIndex,
  onSelectIndex,
  onAddMedia,
  onRemoveMedia,
  disabled = false,
  fieldError = "",
}) {
  const activeMedia = mediaItems[activeMediaIndex] || null;
  const activePreviewUri =
    activeMedia?.previewUrl ||
    (activeMedia?.mediaUrl ? getPostMediaUrl(activeMedia) : null);

  return (
    <View style={styles.root}>
      <View style={styles.preview}>
        {activePreviewUri ? (
          isPostVideoMedia(activeMedia) ? (
            <View style={styles.videoPlaceholder}>
              <Ionicons name="videocam" size={48} color={colors.onPrimary} />
              <Text style={styles.videoLabel}>Video đã chọn</Text>
            </View>
          ) : (
            <Image
              source={{ uri: activePreviewUri }}
              style={styles.previewImage}
              resizeMode="cover"
            />
          )
        ) : (
          <View style={styles.emptyPreview}>
            <Ionicons name="images-outline" size={48} color={colors.outline} />
            <Text style={styles.emptyText}>Thêm ảnh hoặc video</Text>
          </View>
        )}

        {activeMedia?.status === "uploading" ? (
          <View style={styles.progressBar}>
            <View
              style={[styles.progressFill, { width: `${activeMedia.progress || 0}%` }]}
            />
          </View>
        ) : null}

        {activeMedia?.status === "error" ? (
          <View style={styles.errorBadge}>
            <Text style={styles.errorBadgeText}>{activeMedia.errorMessage}</Text>
          </View>
        ) : null}

        <Pressable
          style={[styles.addFab, disabled && styles.disabled]}
          onPress={onAddMedia}
          disabled={disabled || mediaItems.length >= 10}
          accessibilityRole="button"
          accessibilityLabel="Thêm media"
        >
          <Ionicons name="add" size={24} color={colors.onPrimary} />
        </Pressable>
      </View>

      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.thumbRow}
      >
        {mediaItems.map((item, index) => (
          <View key={item.id} style={styles.thumbWrap}>
            <Pressable
              onPress={() => onSelectIndex(index)}
              style={[
                styles.thumb,
                index === activeMediaIndex && styles.thumbActive,
              ]}
            >
              {item.previewUrl || item.mediaUrl ? (
                <Image
                  source={{ uri: item.previewUrl || getPostMediaUrl(item) }}
                  style={styles.thumbImage}
                />
              ) : (
                <View style={styles.thumbPlaceholder}>
                  <Ionicons name="image-outline" size={20} color={colors.outline} />
                </View>
              )}
              {item.status === "uploading" ? (
                <View style={styles.thumbOverlay}>
                  <ActivityIndicator size="small" color={colors.onPrimary} />
                </View>
              ) : null}
            </Pressable>
            <Pressable
              style={styles.removeBtn}
              onPress={() => onRemoveMedia(item.id)}
              accessibilityLabel="Xóa media"
            >
              <Ionicons name="close" size={14} color={colors.onPrimary} />
            </Pressable>
          </View>
        ))}

        <Pressable
          style={[styles.addThumb, disabled && styles.disabled]}
          onPress={onAddMedia}
          disabled={disabled || mediaItems.length >= 10}
          accessibilityLabel="Thêm file"
        >
          <Ionicons name="add" size={24} color={colors.outline} />
        </Pressable>
      </ScrollView>

      {fieldError ? <Text style={styles.fieldError}>{fieldError}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    gap: 8,
  },
  preview: {
    minHeight: 220,
    borderRadius: 12,
    overflow: "hidden",
    backgroundColor: colors.surfaceContainerHigh,
    position: "relative",
  },
  previewImage: {
    width: "100%",
    height: 220,
  },
  videoPlaceholder: {
    height: 220,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.onSurface,
    gap: 8,
  },
  videoLabel: {
    color: colors.onPrimary,
    fontSize: 14,
  },
  emptyPreview: {
    height: 220,
    alignItems: "center",
    justifyContent: "center",
    gap: 8,
  },
  emptyText: {
    color: colors.onSurfaceVariant,
    fontSize: 14,
  },
  progressBar: {
    position: "absolute",
    left: 16,
    right: 16,
    bottom: 16,
    height: 4,
    borderRadius: 2,
    backgroundColor: "rgba(255,255,255,0.5)",
    overflow: "hidden",
  },
  progressFill: {
    height: "100%",
    backgroundColor: colors.primary,
  },
  errorBadge: {
    position: "absolute",
    left: 12,
    right: 12,
    bottom: 12,
    backgroundColor: colors.errorContainer,
    borderRadius: 8,
    padding: 8,
  },
  errorBadgeText: {
    color: colors.onErrorContainer,
    fontSize: 12,
  },
  addFab: {
    position: "absolute",
    right: 12,
    bottom: 12,
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: colors.primary,
    alignItems: "center",
    justifyContent: "center",
  },
  thumbRow: {
    gap: 8,
    paddingVertical: 4,
  },
  thumbWrap: {
    position: "relative",
  },
  thumb: {
    width: 64,
    height: 64,
    borderRadius: 8,
    overflow: "hidden",
    borderWidth: 2,
    borderColor: colors.outlineVariant,
  },
  thumbActive: {
    borderColor: colors.primary,
  },
  thumbImage: {
    width: "100%",
    height: "100%",
  },
  thumbPlaceholder: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.surfaceContainerHigh,
  },
  thumbOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: "rgba(0,0,0,0.4)",
    alignItems: "center",
    justifyContent: "center",
  },
  removeBtn: {
    position: "absolute",
    top: -4,
    right: -4,
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: colors.error,
    alignItems: "center",
    justifyContent: "center",
  },
  addThumb: {
    width: 64,
    height: 64,
    borderRadius: 8,
    borderWidth: 1,
    borderStyle: "dashed",
    borderColor: colors.outlineVariant,
    alignItems: "center",
    justifyContent: "center",
  },
  disabled: {
    opacity: 0.5,
  },
  fieldError: {
    fontSize: 12,
    color: colors.error,
  },
});

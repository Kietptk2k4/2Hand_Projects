import { useCallback, useEffect, useMemo, useState } from "react";
import { Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import * as ImagePicker from "expo-image-picker";
import { MAX_REVIEW_MEDIA } from "../constants/reviewMediaConstants";
import { validateReviewMediaSelection } from "../utils/reviewMediaValidation";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    picker: {
      borderRadius: 12,
      borderWidth: 2,
      borderStyle: "dashed",
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      padding: 24,
      alignItems: "center",
      gap: 8,
    },
    pickerDisabled: { opacity: 0.6 },
    pickerTitle: { fontSize: 15, fontWeight: "600", color: colors.onSurface },
    pickerHint: { fontSize: 13, color: colors.onSurfaceVariant, textAlign: "center" },
    helper: { marginTop: 8, fontSize: 13, color: colors.onSurfaceVariant },
    error: { marginTop: 8, fontSize: 13, color: colors.error },
    grid: { flexDirection: "row", flexWrap: "wrap", gap: 10, marginTop: 12 },
    thumbWrap: {
      width: 96,
      height: 96,
      borderRadius: 10,
      overflow: "hidden",
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
    },
    thumb: { width: "100%", height: "100%" },
    removeButton: {
      position: "absolute",
      top: 4,
      right: 4,
      width: 28,
      height: 28,
      borderRadius: 14,
      backgroundColor: "rgba(0,0,0,0.6)",
      alignItems: "center",
      justifyContent: "center",
    },
    videoBadge: {
      position: "absolute",
      bottom: 4,
      left: 4,
      borderRadius: 4,
      paddingHorizontal: 6,
      paddingVertical: 2,
      backgroundColor: "rgba(0,0,0,0.65)",
    },
    videoBadgeText: { fontSize: 10, color: "#fff", fontWeight: "600" },
  };
}

function buildEntry(asset) {
  const mimeType = asset.mimeType || (asset.type === "video" ? "video/mp4" : "image/jpeg");
  const isVideo = mimeType.startsWith("video/") || asset.type === "video";
  return {
    id: `${asset.uri}-${asset.fileSize ?? 0}`,
    asset,
    uri: asset.uri,
    isVideo,
  };
}

export function ReviewMediaPicker({
  existingMediaCount = 0,
  maxMedia = MAX_REVIEW_MEDIA,
  disabled = false,
  onFilesChange,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [entries, setEntries] = useState([]);
  const [validationError, setValidationError] = useState("");

  const remainingSlots = Math.max(0, maxMedia - existingMediaCount - entries.length);
  const totalSelected = existingMediaCount + entries.length;
  const canAddMore = remainingSlots > 0 && !disabled;

  useEffect(() => {
    onFilesChange?.(entries.map((entry) => entry.asset));
  }, [entries, onFilesChange]);

  const helperText = useMemo(() => {
    if (disabled) return "Không thể thêm ảnh/video cho đánh giá này.";
    return `JPEG, PNG, WebP (tối đa 5MB) · MP4, WebM (tối đa 50MB). Đã chọn ${totalSelected}/${maxMedia}.`;
  }, [disabled, maxMedia, totalSelected]);

  const handlePick = useCallback(async () => {
    if (!canAddMore) return;

    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      setValidationError("Cần quyền truy cập thư viện ảnh để đính kèm media.");
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ["images", "videos"],
      allowsMultipleSelection: true,
      selectionLimit: remainingSlots,
      quality: 0.85,
    });

    if (result.canceled || !result.assets?.length) return;

    setValidationError("");
    const validation = validateReviewMediaSelection(result.assets, existingMediaCount + entries.length);
    if (!validation.valid) {
      setValidationError(validation.message);
      return;
    }

    const nextEntries = validation.files.map(buildEntry);
    setEntries((prev) => [...prev, ...nextEntries]);
  }, [canAddMore, entries.length, existingMediaCount, remainingSlots]);

  const handleRemove = useCallback((id) => {
    setEntries((prev) => prev.filter((entry) => entry.id !== id));
    setValidationError("");
  }, []);

  return (
    <View>
      <Pressable
        style={[styles.picker, !canAddMore && styles.pickerDisabled]}
        onPress={handlePick}
        disabled={!canAddMore}
      >
        <Ionicons name="cloud-upload-outline" size={36} color={colors.onSurfaceVariant} />
        <Text style={styles.pickerTitle}>
          {canAddMore ? "Chọn ảnh hoặc video" : "Đã đạt giới hạn file"}
        </Text>
        <Text style={styles.pickerHint}>
          {canAddMore ? `Còn thêm được ${remainingSlots} file` : `Tối đa ${maxMedia} file mỗi đánh giá`}
        </Text>
      </Pressable>

      <Text style={styles.helper}>{helperText}</Text>
      {validationError ? <Text style={styles.error}>{validationError}</Text> : null}

      {entries.length > 0 ? (
        <View style={styles.grid}>
          {entries.map((entry) => (
            <View key={entry.id} style={styles.thumbWrap}>
              <Image source={{ uri: entry.uri }} style={styles.thumb} resizeMode="cover" />
              {entry.isVideo ? (
                <View style={styles.videoBadge}>
                  <Text style={styles.videoBadgeText}>Video</Text>
                </View>
              ) : null}
              <Pressable
                style={styles.removeButton}
                onPress={() => handleRemove(entry.id)}
                disabled={disabled}
                accessibilityLabel="Xóa file"
              >
                <Ionicons name="close" size={16} color="#fff" />
              </Pressable>
            </View>
          ))}
        </View>
      ) : null}
    </View>
  );
}

import { useMemo } from "react";
import {
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";
import { colors } from "../../../shared/theme/colors";

export function PostMediaCarousel({ media = [], onMediaPress }) {
  const items = useMemo(
    () =>
      (media || [])
        .filter((item) => item?.url || item?.mediaUrl)
        .map((item) => ({ ...item, url: getPostMediaUrl(item) })),
    [media]
  );

  if (items.length === 0) return null;

  if (items.length === 1) {
    return (
      <MediaTile
        item={items[0]}
        style={styles.single}
        onPress={onMediaPress ? () => onMediaPress(0) : undefined}
      />
    );
  }

  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.strip}
    >
      {items.map((item, index) => (
        <MediaTile
          key={`${item.url}-${index}`}
          item={item}
          style={styles.stripItem}
          onPress={onMediaPress ? () => onMediaPress(index) : undefined}
        />
      ))}
    </ScrollView>
  );
}

function MediaTile({ item, style, onPress }) {
  const isVideo = isPostVideoMedia(item);
  const content = (
    <View style={[styles.tile, style]}>
      {isVideo ? (
        <View style={styles.videoPlaceholder}>
          <Ionicons name="play-circle" size={48} color={colors.onPrimary} />
          <Text style={styles.videoLabel}>Video</Text>
        </View>
      ) : (
        <Image source={{ uri: item.url }} style={styles.image} resizeMode="cover" />
      )}
    </View>
  );

  if (!onPress) return content;

  return (
    <Pressable onPress={onPress} accessibilityRole="button" accessibilityLabel="Xem bai viet">
      {content}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  single: {
    width: "100%",
    aspectRatio: 1,
  },
  strip: {
    gap: 4,
    paddingHorizontal: 0,
  },
  stripItem: {
    width: 280,
    aspectRatio: 1,
  },
  tile: {
    overflow: "hidden",
    backgroundColor: colors.surfaceContainerHigh,
  },
  image: {
    width: "100%",
    height: "100%",
  },
  videoPlaceholder: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.onSurface,
    minHeight: 200,
  },
  videoLabel: {
    marginTop: 8,
    color: colors.onPrimary,
    fontSize: 14,
    fontWeight: "600",
  },
});

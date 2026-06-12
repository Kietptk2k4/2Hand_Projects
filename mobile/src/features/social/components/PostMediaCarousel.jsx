import { useMemo } from "react";
import { Image, Pressable, ScrollView, View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";
import { PostVideoPlayer } from "./PostVideoPlayer";

function createStyles(colors) {
  return {
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
  };
}

export function PostMediaCarousel({ media = [], onMediaPress }) {
  const styles = useThemedStyles(createStyles);

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
        styles={styles}
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
          styles={styles}
          style={styles.stripItem}
          onPress={onMediaPress ? () => onMediaPress(index) : undefined}
        />
      ))}
    </ScrollView>
  );
}

function MediaTile({ item, styles, style, onPress }) {
  const isVideo = isPostVideoMedia(item);
  const content = (
    <View style={[styles.tile, style]}>
      {isVideo ? (
        <PostVideoPlayer uri={item.url} style={styles.image} />
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

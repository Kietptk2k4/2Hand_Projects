import { useMemo } from "react";
import { Pressable, ScrollView, View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { isPostVideoMedia } from "../utils/postMediaType";
import { buildPlaybackId, VIDEO_PLAYBACK_SURFACES } from "../utils/videoPlaybackId";
import { PostMediaItem } from "./PostMediaItem";

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

export function PostMediaCarousel({
  media = [],
  postId,
  surface = VIDEO_PLAYBACK_SURFACES.FEED,
  onMediaPress,
}) {
  const styles = useThemedStyles(createStyles);

  const items = useMemo(
    () => (media || []).filter((item) => item?.url || item?.mediaUrl),
    [media]
  );

  if (items.length === 0) return null;

  if (items.length === 1) {
    return (
      <MediaTile
        item={items[0]}
        index={0}
        postId={postId}
        surface={surface}
        styles={styles}
        style={styles.single}
        onMediaPress={onMediaPress}
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
          key={`${item.url || item.mediaUrl}-${index}`}
          item={item}
          index={index}
          postId={postId}
          surface={surface}
          styles={styles}
          style={styles.stripItem}
          onMediaPress={onMediaPress}
        />
      ))}
    </ScrollView>
  );
}

function MediaTile({ item, index, postId, surface, styles, style, onMediaPress }) {
  const isVideo = isPostVideoMedia(item);
  const playbackId = buildPlaybackId(postId, index, surface);
  const handlePress = onMediaPress ? () => onMediaPress(index) : undefined;

  const tile = (
    <View style={[styles.tile, style]}>
      <PostMediaItem
        item={item}
        variant="inline"
        style={styles.image}
        playbackId={playbackId}
      />
    </View>
  );

  if (!handlePress || isVideo) {
    return tile;
  }

  return (
    <Pressable
      onPress={handlePress}
      accessibilityRole="button"
      accessibilityLabel="Xem bai viet"
    >
      {tile}
    </Pressable>
  );
}

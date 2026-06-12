import { Image, Pressable, StyleSheet, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";
import { PostVideoPlayer } from "./PostVideoPlayer";

function createStyles(colors) {
  return {
    row: {
      flexDirection: "row",
      flexWrap: "wrap",
      gap: 8,
      marginTop: 8,
    },
    tile: {
      width: 80,
      height: 80,
      borderRadius: 8,
      overflow: "hidden",
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerHigh,
    },
    image: {
      width: "100%",
      height: "100%",
    },
    videoOverlay: {
      ...StyleSheet.absoluteFillObject,
      alignItems: "center",
      justifyContent: "center",
      backgroundColor: "rgba(0,0,0,0.25)",
    },
  };
}

export function CommentMediaDisplay({ media = [], onMediaPress }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  const items = (media || []).filter((item) => item?.url).map((item) => ({
    ...item,
    url: getPostMediaUrl(item),
  }));

  if (items.length === 0) return null;

  return (
    <View style={styles.row}>
      {items.map((item, index) => {
        const tile = (
          <View style={styles.tile}>
            {isPostVideoMedia(item) ? (
              <>
                <PostVideoPlayer
                  uri={item.url}
                  style={StyleSheet.absoluteFill}
                  contentFit="cover"
                  nativeControls={false}
                />
                <View style={styles.videoOverlay} pointerEvents="none">
                  <Ionicons name="play-circle" size={28} color={colors.onPrimary} />
                </View>
              </>
            ) : (
              <Image source={{ uri: item.url }} style={styles.image} resizeMode="cover" />
            )}
          </View>
        );

        if (!onMediaPress) {
          return <View key={`${item.url}-${index}`}>{tile}</View>;
        }

        return (
          <Pressable
            key={`${item.url}-${index}`}
            onPress={() => onMediaPress(index)}
            accessibilityRole="button"
            accessibilityLabel={`Xem media ${index + 1}`}
          >
            {tile}
          </Pressable>
        );
      })}
    </View>
  );
}

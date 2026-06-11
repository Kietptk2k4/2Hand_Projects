import { Image, Pressable, StyleSheet, View } from "react-native";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";
import { colors } from "../../../shared/theme/colors";

export function CommentMediaDisplay({ media = [], onMediaPress }) {
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
              <View style={styles.videoPlaceholder} />
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

const styles = StyleSheet.create({
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
  videoPlaceholder: {
    flex: 1,
    backgroundColor: colors.onSurface,
  },
});

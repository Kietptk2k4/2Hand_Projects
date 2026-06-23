import { Pressable, View } from "react-native";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { getPostMediaUrl } from "../utils/postMediaType";
import { PostMediaItem } from "./PostMediaItem";

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
  };
}

export function CommentMediaDisplay({ media = [], onMediaPress }) {
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
            <PostMediaItem item={item} variant="thumbnail" style={styles.image} playIconSize={28} />
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

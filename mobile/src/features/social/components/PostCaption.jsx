import { useMemo, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { colors } from "../../../shared/theme/colors";

const HASHTAG_REGEX = /#([\w\u00C0-\u024F\u1E00-\u1EFF]+)/g;

function buildCaptionParts(text, extraHashtags = []) {
  if (!text && extraHashtags.length === 0) return [];

  const tagSet = new Set(
    (extraHashtags || []).map((tag) => (tag.startsWith("#") ? tag.slice(1) : tag).toLowerCase())
  );

  let body = text || "";
  if (!body && tagSet.size > 0) {
    body = [...tagSet].map((t) => `#${t}`).join(" ");
  }

  const parts = [];
  let lastIndex = 0;
  let match;

  const regex = new RegExp(HASHTAG_REGEX.source, HASHTAG_REGEX.flags);
  while ((match = regex.exec(body)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ type: "text", value: body.slice(lastIndex, match.index) });
    }
    parts.push({ type: "tag", value: match[1] });
    tagSet.delete(match[1].toLowerCase());
    lastIndex = regex.lastIndex;
  }

  if (lastIndex < body.length) {
    parts.push({ type: "text", value: body.slice(lastIndex) });
  }

  tagSet.forEach((tag) => {
    parts.push({ type: "tag", value: tag });
  });

  return parts;
}

export function PostCaption({ caption, hashtags = [], onCaptionPress, onHashtagPress }) {
  const [expanded, setExpanded] = useState(false);
  const isLong = useMemo(() => (caption || "").length > 180, [caption]);
  const parts = useMemo(
    () => buildCaptionParts(caption, hashtags),
    [caption, hashtags]
  );

  if (!caption && (!hashtags || hashtags.length === 0)) {
    return null;
  }

  const body = (
    <View>
      <Text style={styles.caption} numberOfLines={!expanded && isLong ? 3 : undefined}>
        {parts.map((part, index) => {
          if (part.type === "text") {
            return <Text key={`t-${index}`}>{part.value}</Text>;
          }
          return (
            <Text
              key={`h-${index}-${part.value}`}
              style={styles.hashtag}
              onPress={(event) => {
                event?.stopPropagation?.();
                onHashtagPress?.(part.value);
              }}
            >
              #{part.value}
            </Text>
          );
        })}
      </Text>
      {isLong ? (
        <Pressable
          onPress={(event) => {
            event?.stopPropagation?.();
            setExpanded((prev) => !prev);
          }}
          hitSlop={8}
        >
          <Text style={styles.expand}>{expanded ? "Thu gọn" : "Xem thêm"}</Text>
        </Pressable>
      ) : null}
    </View>
  );

  if (onCaptionPress) {
    return (
      <Pressable onPress={onCaptionPress} accessibilityRole="button">
        {body}
      </Pressable>
    );
  }

  return body;
}

const styles = StyleSheet.create({
  caption: {
    fontSize: 15,
    lineHeight: 22,
    color: colors.onSurface,
  },
  hashtag: {
    color: colors.primary,
    fontWeight: "500",
  },
  expand: {
    marginTop: 4,
    fontSize: 14,
    fontWeight: "600",
    color: colors.primary,
  },
});

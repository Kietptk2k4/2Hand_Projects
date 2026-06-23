import { Ionicons } from "@expo/vector-icons";
import { useVideoPlayer, VideoView } from "expo-video";
import { Image, Platform, StyleSheet, View } from "react-native";
import { logImageError, logImageLoad } from "../../../shared/utils/debugMediaLog";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";
import { PostVideoPlayer } from "./PostVideoPlayer";

function PostMediaThumbnailInner({ uri, style, iconSize = 36 }) {
  const player = useVideoPlayer(uri, (instance) => {
    instance.loop = false;
    instance.muted = true;
    instance.pause();
  });

  return (
    <View style={[styles.thumbnailWrap, style]}>
      <VideoView
        player={player}
        style={StyleSheet.absoluteFill}
        contentFit="cover"
        nativeControls={false}
        surfaceType={Platform.OS === "android" ? "textureView" : undefined}
      />
      <View style={styles.thumbnailOverlay} pointerEvents="none">
        <Ionicons name="play-circle" size={iconSize} color="#FFFFFF" />
      </View>
    </View>
  );
}

export function PostMediaThumbnail({ item, style, iconSize = 36, fallbackUri }) {
  const src = item?.url || item?.mediaUrl ? getPostMediaUrl(item) : fallbackUri;
  if (!src) return null;

  const isVideo = item ? isPostVideoMedia(item) : false;

  if (isVideo) {
    return <PostMediaThumbnailInner key={src} uri={src} style={style} iconSize={iconSize} />;
  }

  return (
    <Image
      source={{ uri: src }}
      style={[styles.image, style]}
      resizeMode="cover"
      onLoad={() => logImageLoad(src)}
      onError={(event) => logImageError(src, event)}
    />
  );
}

export function PostMediaItem({
  item,
  style,
  variant = "inline",
  contentFit = "cover",
  nativeControls,
  playbackId = null,
  playIconSize = 36,
}) {
  const src = getPostMediaUrl(item);
  if (!src) return null;

  const isVideo = isPostVideoMedia(item);
  const showControls = nativeControls ?? (variant === "inline" && isVideo);

  if (variant === "thumbnail" || (variant === "grid" && isVideo)) {
    return <PostMediaThumbnail item={item} style={style} iconSize={playIconSize} />;
  }

  if (isVideo) {
    return (
      <PostVideoPlayer
        uri={src}
        style={style}
        contentFit={contentFit}
        nativeControls={showControls}
        playbackId={playbackId}
      />
    );
  }

  return (
    <Image
      source={{ uri: src }}
      style={[styles.image, style]}
      resizeMode={contentFit === "contain" ? "contain" : "cover"}
      onLoad={() => logImageLoad(src)}
      onError={(event) => logImageError(src, event)}
    />
  );
}

const styles = StyleSheet.create({
  image: {
    width: "100%",
    height: "100%",
  },
  thumbnailWrap: {
    width: "100%",
    height: "100%",
    overflow: "hidden",
    backgroundColor: "#000",
  },
  thumbnailOverlay: {
    ...StyleSheet.absoluteFillObject,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "rgba(0,0,0,0.25)",
  },
});

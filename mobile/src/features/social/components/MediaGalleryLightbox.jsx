import { useEffect } from "react";
import { Image, Modal, Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useVideoPlayback } from "../context/VideoPlaybackContext";
import { getPostMediaUrl, isPostVideoMedia } from "../utils/postMediaType";
import { buildPlaybackId, VIDEO_PLAYBACK_SURFACES } from "../utils/videoPlaybackId";
import { PostVideoPlayer } from "./PostVideoPlayer";

export function MediaGalleryLightbox({
  media = [],
  postId,
  ownerId,
  initialIndex = 0,
  onClose,
}) {
  const insets = useSafeAreaInsets();
  const playbackOwnerId = ownerId || postId;
  const { pauseAll } = useVideoPlayback();

  const items = (media || [])
    .filter((item) => item?.url || item?.mediaUrl)
    .map((item) => ({ ...item, url: getPostMediaUrl(item) }));

  const safeIndex = Math.min(Math.max(initialIndex, 0), Math.max(items.length - 1, 0));
  const current = items[safeIndex];
  const isVideo = current ? isPostVideoMedia(current) : false;
  const playbackId = buildPlaybackId(
    playbackOwnerId,
    safeIndex,
    VIDEO_PLAYBACK_SURFACES.GALLERY
  );

  useEffect(() => {
    return () => {
      pauseAll();
    };
  }, [pauseAll]);

  const handleClose = () => {
    pauseAll();
    onClose?.();
  };

  if (!current) return null;

  return (
    <Modal visible transparent animationType="fade" onRequestClose={handleClose}>
      <View style={styles.backdrop}>
        <Pressable style={StyleSheet.absoluteFill} onPress={handleClose} />
        <View style={styles.content} pointerEvents="box-none">
          {isVideo ? (
            <PostVideoPlayer
              uri={current.url}
              style={styles.media}
              contentFit="contain"
              nativeControls
              autoPlay
              playbackId={playbackId}
            />
          ) : (
            <Image source={{ uri: current.url }} style={styles.media} resizeMode="contain" />
          )}
        </View>
        <Pressable
          style={[styles.closeButton, { top: Math.max(insets.top, 16) + 8 }]}
          onPress={handleClose}
          accessibilityRole="button"
          accessibilityLabel="Dong gallery"
        >
          <Ionicons name="close" size={28} color="#FFFFFF" />
        </Pressable>
        {items.length > 1 ? (
          <Text style={[styles.counter, { bottom: Math.max(insets.bottom, 16) + 8 }]}>
            {safeIndex + 1} / {items.length}
          </Text>
        ) : null}
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.92)",
    alignItems: "center",
    justifyContent: "center",
  },
  content: {
    width: "100%",
    height: "80%",
    alignItems: "center",
    justifyContent: "center",
  },
  media: {
    width: "100%",
    height: "100%",
  },
  closeButton: {
    position: "absolute",
    right: 16,
    width: 44,
    height: 44,
    alignItems: "center",
    justifyContent: "center",
  },
  counter: {
    position: "absolute",
    alignSelf: "center",
    fontSize: 14,
    color: "#FFFFFF",
  },
});

import { useVideoPlayer, VideoView } from "expo-video";
import { StyleSheet } from "react-native";

export function PostVideoPlayer({
  uri,
  style,
  contentFit = "cover",
  nativeControls = true,
}) {
  const player = useVideoPlayer(uri, (instance) => {
    instance.loop = false;
  });

  if (!uri) return null;

  return (
    <VideoView
      style={[styles.video, style]}
      player={player}
      nativeControls={nativeControls}
      contentFit={contentFit}
      allowsFullscreen={nativeControls}
    />
  );
}

const styles = StyleSheet.create({
  video: {
    width: "100%",
    height: "100%",
  },
});

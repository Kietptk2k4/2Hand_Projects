import { useEffect } from "react";
import { Platform, StyleSheet } from "react-native";
import { useVideoPlayer, VideoView } from "expo-video";
import { logVideoUri } from "../../../shared/utils/debugMediaLog";
import { useOptionalVideoPlayback } from "../context/VideoPlaybackContext";

export function PostVideoPlayer({
  uri,
  style,
  contentFit = "cover",
  nativeControls = true,
  autoPlay = false,
  playbackId = null,
}) {
  const playback = useOptionalVideoPlayback();
  const player = useVideoPlayer(uri, (instance) => {
    instance.loop = false;
    if (autoPlay) {
      instance.play();
    }
  });

  useEffect(() => {
    logVideoUri(uri);
  }, [uri]);

  useEffect(() => {
    if (!playbackId || !playback || !player) return undefined;
    return playback.registerPlayer(playbackId, player);
  }, [playback, playbackId, player]);

  useEffect(() => {
    if (!playbackId || !playback || !player) return;
    if (playback.activePlaybackId && playback.activePlaybackId !== playbackId && player.playing) {
      player.pause();
    }
  }, [playback?.activePlaybackId, playbackId, player, playback]);

  useEffect(() => {
    if (!playbackId || !playback || !player) return undefined;

    const playingSub = player.addListener("playingChange", ({ isPlaying }) => {
      if (isPlaying) {
        playback.claimPlayback(playbackId);
        return;
      }
      if (playback.isActivePlayer(playbackId)) {
        playback.releasePlayback(playbackId);
      }
    });

    const endedSub = player.addListener("playToEnd", () => {
      playback.releasePlayback(playbackId);
    });

    return () => {
      playingSub.remove();
      endedSub.remove();
    };
  }, [playback, playbackId, player]);

  useEffect(() => {
    if (!autoPlay || !playbackId || !playback || !player) return undefined;

    playback.claimPlayback(playbackId);
    player.play();

    return () => {
      playback.releasePlayback(playbackId);
    };
  }, [autoPlay, playback, playbackId, player, uri]);

  if (!uri) return null;

  return (
    <VideoView
      key={uri}
      style={[styles.video, style]}
      player={player}
      nativeControls={nativeControls}
      contentFit={contentFit}
      allowsFullscreen={nativeControls}
      surfaceType={Platform.OS === "android" ? "textureView" : undefined}
    />
  );
}

const styles = StyleSheet.create({
  video: {
    width: "100%",
    height: "100%",
  },
});

import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState,
} from "react";

const VideoPlaybackContext = createContext(null);

export function VideoPlaybackProvider({ children }) {
  const [activePlaybackId, setActivePlaybackId] = useState(null);
  const playersRef = useRef(new Map());

  const registerPlayer = useCallback((playbackId, player) => {
    if (!playbackId || !player) {
      return () => {};
    }

    playersRef.current.set(playbackId, player);

    return () => {
      playersRef.current.delete(playbackId);
      setActivePlaybackId((current) => (current === playbackId ? null : current));
    };
  }, []);

  const claimPlayback = useCallback((playbackId) => {
    if (!playbackId) return;

    playersRef.current.forEach((player, id) => {
      if (id !== playbackId && player.playing) {
        player.pause();
      }
    });

    setActivePlaybackId(playbackId);
  }, []);

  const releasePlayback = useCallback((playbackId) => {
    setActivePlaybackId((current) => (current === playbackId ? null : current));
  }, []);

  const pauseAll = useCallback(() => {
    playersRef.current.forEach((player) => {
      if (player.playing) {
        player.pause();
      }
    });
    setActivePlaybackId(null);
  }, []);

  const isActivePlayer = useCallback(
    (playbackId) => activePlaybackId === playbackId,
    [activePlaybackId]
  );

  const value = useMemo(
    () => ({
      activePlaybackId,
      registerPlayer,
      claimPlayback,
      releasePlayback,
      pauseAll,
      isActivePlayer,
    }),
    [
      activePlaybackId,
      registerPlayer,
      claimPlayback,
      releasePlayback,
      pauseAll,
      isActivePlayer,
    ]
  );

  return (
    <VideoPlaybackContext.Provider value={value}>{children}</VideoPlaybackContext.Provider>
  );
}

export function useVideoPlayback() {
  const context = useContext(VideoPlaybackContext);
  if (!context) {
    throw new Error("useVideoPlayback must be used inside VideoPlaybackProvider");
  }
  return context;
}

export function useOptionalVideoPlayback() {
  return useContext(VideoPlaybackContext);
}
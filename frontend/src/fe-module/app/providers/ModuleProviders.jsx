import { RouterProvider } from "react-router-dom";
import { router } from "../router/router";
import { AuthSessionProvider } from "../../features/auth/hooks/useAuthSession.jsx";
import { SocialWriteBlockProvider } from "../../features/social/context/SocialWriteBlockContext";
import { VideoPlaybackProvider } from "../../features/social/context/VideoPlaybackContext";

export function ModuleProviders() {
  return (
    <AuthSessionProvider>
      <VideoPlaybackProvider>
        <SocialWriteBlockProvider>
          <RouterProvider router={router} />
        </SocialWriteBlockProvider>
      </VideoPlaybackProvider>
    </AuthSessionProvider>
  );
}


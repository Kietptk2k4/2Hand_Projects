import { RouterProvider } from "react-router-dom";
import { router } from "../router/router";
import { AppearanceProvider } from "../../features/auth/context/AppearanceContext";
import { AuthSessionProvider } from "../../features/auth/hooks/useAuthSession.jsx";
import { CartBadgeProvider } from "../../features/commerce/context/CartBadgeContext";
import { SocialWriteBlockProvider } from "../../features/social/context/SocialWriteBlockContext";
import { VideoPlaybackProvider } from "../../features/social/context/VideoPlaybackContext";

export function ModuleProviders() {
  return (
    <AuthSessionProvider>
      <AppearanceProvider>
      <CartBadgeProvider>
        <VideoPlaybackProvider>
          <SocialWriteBlockProvider>
            <RouterProvider router={router} />
          </SocialWriteBlockProvider>
        </VideoPlaybackProvider>
      </CartBadgeProvider>
      </AppearanceProvider>
    </AuthSessionProvider>
  );
}


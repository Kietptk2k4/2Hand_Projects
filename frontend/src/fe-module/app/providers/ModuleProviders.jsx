import { RouterProvider } from "react-router-dom";
import { router } from "../router/router";
import { AuthSessionProvider } from "../../features/auth/hooks/useAuthSession.jsx";
import { SocialWriteBlockProvider } from "../../features/social/context/SocialWriteBlockContext";

export function ModuleProviders() {
  return (
    <AuthSessionProvider>
      <SocialWriteBlockProvider>
        <RouterProvider router={router} />
      </SocialWriteBlockProvider>
    </AuthSessionProvider>
  );
}


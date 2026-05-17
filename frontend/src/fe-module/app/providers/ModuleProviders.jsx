import { RouterProvider } from "react-router-dom";
import { router } from "../router/router";
import { AuthSessionProvider } from "../../features/auth/hooks/useAuthSession.jsx";

export function ModuleProviders() {
  return (
    <AuthSessionProvider>
      <RouterProvider router={router} />
    </AuthSessionProvider>
  );
}


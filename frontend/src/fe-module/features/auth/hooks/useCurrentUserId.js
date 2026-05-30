import { useMemo } from "react";
import { resolveCurrentUserId } from "../utils/resolveCurrentUserId";
import { useAuthSession } from "./useAuthSession.jsx";

export function useCurrentUserId() {
  const { user } = useAuthSession();
  return useMemo(() => resolveCurrentUserId(user), [user]);
}

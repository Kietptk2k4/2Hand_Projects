import { useEffect, useState } from "react";
import { getAccessToken } from "../../../services/auth/tokenStorage";
import { resolveUserIdFromAccessToken } from "../utils/decodeAccessToken";

export function useCurrentUserId() {
  const [userId, setUserId] = useState(null);

  useEffect(() => {
    let active = true;

    (async () => {
      const token = await getAccessToken();
      if (!active) return;
      setUserId(resolveUserIdFromAccessToken(token));
    })();

    return () => {
      active = false;
    };
  }, []);

  return userId;
}

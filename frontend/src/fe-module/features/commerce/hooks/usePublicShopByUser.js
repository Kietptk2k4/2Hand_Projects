import { useCallback, useEffect, useState } from "react";
import { fetchPublicShopByUser } from "../api/publicShopByUserApi";
import { mapPublicShopByUserResponse } from "../utils/reviewParticipantMapper";

export function usePublicShopByUser(userId, { enabled = true } = {}) {
  const [shop, setShop] = useState(null);
  const [status, setStatus] = useState("idle");

  const load = useCallback(async () => {
    if (!enabled || !userId) {
      setShop(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");

    try {
      const raw = await fetchPublicShopByUser(userId);
      setShop(mapPublicShopByUserResponse(raw));
      setStatus("ready");
    } catch {
      setShop(mapPublicShopByUserResponse(null));
      setStatus("ready");
    }
  }, [enabled, userId]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    shop,
    isLoading: status === "loading",
    refetch: load,
  };
}

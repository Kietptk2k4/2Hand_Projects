import { useQuery } from "@tanstack/react-query";
import { fetchPublicShopByUser } from "../api/publicShopByUserApi";
import { mapPublicShopByUserResponse } from "../utils/mapPublicShopByUser";

export function usePublicShopByUser(userId, { enabled = true } = {}) {
  const query = useQuery({
    queryKey: ["commerce", "public-shop-by-user", userId],
    enabled: Boolean(enabled && userId),
    queryFn: async () => {
      try {
        const raw = await fetchPublicShopByUser(userId);
        return mapPublicShopByUserResponse(raw);
      } catch {
        return mapPublicShopByUserResponse(null);
      }
    },
  });

  return {
    shop: query.data ?? mapPublicShopByUserResponse(null),
    isLoading: query.isLoading,
    refetch: query.refetch,
  };
}

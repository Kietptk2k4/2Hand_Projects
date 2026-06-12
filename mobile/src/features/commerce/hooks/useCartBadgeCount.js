import { useQuery } from "@tanstack/react-query";

import { cartKeys } from "../api/cartKeys";

import { getCartBadgeCountFromMapped } from "../utils/cartDisplay";

import { fetchCartWithValidation } from "./useCart";



export function useCartBadgeCount() {

  const query = useQuery({

    queryKey: cartKeys.detail(),

    queryFn: fetchCartWithValidation,

    staleTime: 30_000,

    select: (data) => getCartBadgeCountFromMapped(data?.cart),

  });



  return query.data ?? 0;

}


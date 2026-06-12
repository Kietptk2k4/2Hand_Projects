import { useCallback, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  fetchCart,
  removeCartItem,
  updateCartItemQuantity,
  validateCartItems,
} from "../api/cartApi";
import { cartKeys } from "../api/cartKeys";
import { mapCartResponse, mapValidateCartItemsResponse } from "../utils/cartMapper";
import { applyValidationToCart } from "../utils/cartValidationMerge";
import { isCartItemInvalid } from "../utils/cartDisplay";

export async function fetchCartWithValidation() {
  const raw = await fetchCart();
  let mapped = mapCartResponse(raw);

  if (!mapped?.items?.length) {
    return { cart: mapped, validation: null };
  }

  const cartItemIds = mapped.items.map((item) => item.cartItemId);
  const validationRaw = await validateCartItems(cartItemIds);
  const validation = mapValidateCartItemsResponse(validationRaw);
  mapped = applyValidationToCart(mapped, validation);

  return { cart: mapped, validation };
}

export function useCart() {
  const queryClient = useQueryClient();
  const [mutationError, setMutationError] = useState("");
  const [mutatingItemId, setMutatingItemId] = useState(null);

  const query = useQuery({
    queryKey: cartKeys.detail(),
    queryFn: fetchCartWithValidation,
  });

  const cart = query.data?.cart ?? null;
  const validation = query.data?.validation ?? null;

  const invalidateCart = useCallback(async () => {
    await queryClient.invalidateQueries({ queryKey: cartKeys.detail() });
  }, [queryClient]);

  const updateQuantityMutation = useMutation({
    mutationFn: async ({ cartItemId, quantity }) => {
      await updateCartItemQuantity(cartItemId, quantity);
      return fetchCartWithValidation();
    },
    onMutate: ({ cartItemId }) => {
      setMutatingItemId(cartItemId);
      setMutationError("");
    },
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data);
    },
    onError: async (error) => {
      setMutationError(error?.message || "Không cập nhật được số lượng.");
      await invalidateCart();
    },
    onSettled: () => {
      setMutatingItemId(null);
    },
  });

  const removeItemMutation = useMutation({
    mutationFn: async (cartItemId) => {
      await removeCartItem(cartItemId);
      return fetchCartWithValidation();
    },
    onMutate: (cartItemId) => {
      setMutatingItemId(cartItemId);
      setMutationError("");
    },
    onSuccess: (data) => {
      queryClient.setQueryData(cartKeys.detail(), data);
    },
    onError: async (error) => {
      setMutationError(error?.message || "Không xóa được sản phẩm.");
      await invalidateCart();
    },
    onSettled: () => {
      setMutatingItemId(null);
    },
  });

  const updateQuantity = useCallback(
    async (cartItemId, nextQuantity) => {
      const item = cart?.items?.find((entry) => entry.cartItemId === cartItemId);
      if (!item || isCartItemInvalid(item)) return;

      const quantity = Number(nextQuantity);
      if (!Number.isInteger(quantity) || quantity <= 0) return;
      if (quantity > item.availableQuantity) return;

      await updateQuantityMutation.mutateAsync({ cartItemId, quantity });
    },
    [cart?.items, updateQuantityMutation]
  );

  const removeItem = useCallback(
    async (cartItemId) => {
      await removeItemMutation.mutateAsync(cartItemId);
    },
    [removeItemMutation]
  );

  const revalidate = useCallback(
    async (cartItemIds) => {
      if (!cart?.items?.length) return null;

      try {
        const raw = await validateCartItems(cartItemIds);
        const result = mapValidateCartItemsResponse(raw);
        if (result) {
          queryClient.setQueryData(cartKeys.detail(), (prev) => {
            if (!prev?.cart) return prev;
            return {
              ...prev,
              cart: applyValidationToCart(prev.cart, result),
              validation: result,
            };
          });
        }
        return result;
      } catch {
        return null;
      }
    },
    [cart?.items?.length, queryClient]
  );

  const isMutating = updateQuantityMutation.isPending || removeItemMutation.isPending;
  const errorMessage =
    mutationError || (query.isError ? query.error?.message || "Không tải được giỏ hàng. Vui lòng thử lại." : "");

  return {
    cart,
    validation,
    errorMessage,
    isLoading: query.isLoading,
    isEmpty: query.isSuccess && (cart?.items?.length ?? 0) === 0,
    isMutating,
    mutatingItemId,
    updateQuantity,
    removeItem,
    retry: query.refetch,
    revalidate,
  };
}

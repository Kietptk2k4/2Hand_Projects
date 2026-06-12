import { useMutation, useQueryClient } from "@tanstack/react-query";
import { deleteUserAddress } from "../api/addressApi";
import { addressKeys } from "../api/addressKeys";
import { mapAddressApiError } from "../constants/addressConstants";
import { mapDeleteAddressResponse } from "../utils/addressMapper";

export function useDeleteAddress() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async (addressId) => {
      const raw = await deleteUserAddress(addressId);
      return mapDeleteAddressResponse(raw);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: addressKeys.list() });
    },
  });

  return {
    deleteAddress: mutation.mutateAsync,
    isDeleting: mutation.isPending,
    error: mutation.error ? mapAddressApiError(mutation.error) : "",
    reset: mutation.reset,
  };
}
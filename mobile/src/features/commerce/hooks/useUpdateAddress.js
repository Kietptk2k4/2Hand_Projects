import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateUserAddress } from "../api/addressApi";
import { addressKeys } from "../api/addressKeys";
import { mapAddressApiError } from "../constants/addressConstants";
import { toUpdateAddressPayload } from "../utils/addressMapper";

export function useUpdateAddress() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async ({ addressId, form }) => {
      await updateUserAddress(addressId, toUpdateAddressPayload(form));
      return addressId;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: addressKeys.list() });
    },
  });

  return {
    updateAddress: mutation.mutateAsync,
    isUpdating: mutation.isPending,
    error: mutation.error ? mapAddressApiError(mutation.error) : "",
    reset: mutation.reset,
  };
}
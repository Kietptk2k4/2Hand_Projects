import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createUserAddress } from "../api/addressApi";
import { addressKeys } from "../api/addressKeys";
import { mapAddressApiError } from "../constants/addressConstants";
import { mapCreateAddressResponse, toCreateAddressPayload } from "../utils/addressMapper";

export function useCreateAddress() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async (form) => {
      const raw = await createUserAddress(toCreateAddressPayload(form));
      return mapCreateAddressResponse(raw);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: addressKeys.list() });
    },
  });

  return {
    createAddress: mutation.mutateAsync,
    isCreating: mutation.isPending,
    error: mutation.error ? mapAddressApiError(mutation.error) : "",
    reset: mutation.reset,
  };
}
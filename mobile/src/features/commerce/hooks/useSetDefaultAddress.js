import { useMutation, useQueryClient } from "@tanstack/react-query";
import { setDefaultUserAddress } from "../api/addressApi";
import { addressKeys } from "../api/addressKeys";
import { mapAddressApiError } from "../constants/addressConstants";

export function useSetDefaultAddress() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async (addressId) => {
      await setDefaultUserAddress(addressId);
      return addressId;
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: addressKeys.list() });
    },
  });

  return {
    setDefaultAddress: mutation.mutateAsync,
    isSettingDefault: mutation.isPending,
    error: mutation.error ? mapAddressApiError(mutation.error) : "",
    reset: mutation.reset,
  };
}
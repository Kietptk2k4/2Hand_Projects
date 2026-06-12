import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback, useState } from "react";
import { fetchUserAddresses } from "../api/addressApi";
import { addressKeys } from "../api/addressKeys";
import { mapAddressApiError } from "../constants/addressConstants";
import { mapAddressesResponse } from "../utils/addressMapper";
import { prefetchGhnAddressLabelsForAddresses } from "../utils/prefetchGhnAddressLabels";

async function fetchAddressesWithLabels() {
  const raw = await fetchUserAddresses();
  const list = mapAddressesResponse(raw);
  await prefetchGhnAddressLabelsForAddresses(list);
  return list;
}

export function useAddresses({ enabled = true } = {}) {
  const queryClient = useQueryClient();
  const [labelVersion, setLabelVersion] = useState(0);

  const query = useQuery({
    queryKey: addressKeys.list(),
    queryFn: async () => {
      const list = await fetchAddressesWithLabels();
      setLabelVersion((version) => version + 1);
      return list;
    },
    enabled,
  });

  const invalidate = useCallback(async () => {
    await queryClient.invalidateQueries({ queryKey: addressKeys.list() });
  }, [queryClient]);

  const addresses = query.data ?? [];
  const errorMessage = query.isError
    ? mapAddressApiError(query.error) || "Không tải được danh sách địa chỉ."
    : "";

  return {
    addresses,
    labelVersion,
    isLoading: query.isLoading,
    isFetching: query.isFetching,
    errorMessage,
    isEmpty: query.isSuccess && addresses.length === 0,
    retry: query.refetch,
    invalidate,
  };
}
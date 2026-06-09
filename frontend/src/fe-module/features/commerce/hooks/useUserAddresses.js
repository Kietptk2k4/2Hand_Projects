import { useCallback, useEffect, useState } from "react";
import {
  createUserAddress,
  deleteUserAddress,
  fetchUserAddresses,
  setDefaultUserAddress,
  updateUserAddress,
} from "../api/userAddressApi";
import { ADDRESS_TOAST_MESSAGES, mapAddressApiError } from "../constants/addressConstants";
import {
  mapAddressesResponse,
  mapCreateAddressResponse,
  mapDeleteAddressResponse,
  sortAddressesClient,
  toCreateAddressPayload,
  toUpdateAddressPayload,
} from "../utils/addressMapper";
import { prefetchGhnAddressLabelsForAddresses } from "../utils/prefetchGhnAddressLabels";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useUserAddresses() {
  const { showSessionExpired } = useAuthSession();
  const [addresses, setAddresses] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [mutatingAddressId, setMutatingAddressId] = useState(null);
  const [addressLabelVersion, setAddressLabelVersion] = useState(0);

  const load = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const raw = await fetchUserAddresses();
      const list = mapAddressesResponse(raw);
      setAddresses(sortAddressesClient(list));
      prefetchGhnAddressLabelsForAddresses(list).then(() => {
        setAddressLabelVersion((version) => version + 1);
      });
      return list;
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return [];
      }
      setErrorMessage(mapAddressApiError(error));
      return [];
    } finally {
      setIsLoading(false);
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const handleMutationError = useCallback(
    (error) => {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        throw error;
      }
      throw { ...error, message: mapAddressApiError(error) };
    },
    [showSessionExpired],
  );

  const createAddress = useCallback(
    async (form) => {
      setIsCreating(true);
      try {
        const raw = await createUserAddress(toCreateAddressPayload(form));
        mapCreateAddressResponse(raw);
        await load();
        return ADDRESS_TOAST_MESSAGES.createSuccess;
      } catch (error) {
        handleMutationError(error);
        return null;
      } finally {
        setIsCreating(false);
      }
    },
    [handleMutationError, load],
  );

  const updateAddress = useCallback(
    async (addressId, form) => {
      setIsUpdating(true);
      setMutatingAddressId(addressId);
      try {
        await updateUserAddress(addressId, toUpdateAddressPayload(form));
        await load();
        return ADDRESS_TOAST_MESSAGES.updateSuccess;
      } catch (error) {
        handleMutationError(error);
        return null;
      } finally {
        setIsUpdating(false);
        setMutatingAddressId(null);
      }
    },
    [handleMutationError, load],
  );

  const deleteAddress = useCallback(
    async (addressId) => {
      setIsDeleting(true);
      setMutatingAddressId(addressId);
      try {
        const raw = await deleteUserAddress(addressId);
        mapDeleteAddressResponse(raw);
        await load();
        return ADDRESS_TOAST_MESSAGES.deleteSuccess;
      } catch (error) {
        handleMutationError(error);
        return null;
      } finally {
        setIsDeleting(false);
        setMutatingAddressId(null);
      }
    },
    [handleMutationError, load],
  );

  const setDefaultAddress = useCallback(
    async (addressId) => {
      setMutatingAddressId(addressId);
      try {
        await setDefaultUserAddress(addressId);
        await load();
        return ADDRESS_TOAST_MESSAGES.setDefaultSuccess;
      } catch (error) {
        handleMutationError(error);
        return null;
      } finally {
        setMutatingAddressId(null);
      }
    },
    [handleMutationError, load],
  );

  return {
    addresses,
    addressLabelVersion,
    isLoading,
    errorMessage,
    isCreating,
    isUpdating,
    isDeleting,
    mutatingAddressId,
    isMutating: isCreating || isUpdating || isDeleting || Boolean(mutatingAddressId),
    load,
    retry: load,
    createAddress,
    updateAddress,
    deleteAddress,
    setDefaultAddress,
  };
}

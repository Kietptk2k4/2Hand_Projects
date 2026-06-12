import { useQuery } from "@tanstack/react-query";
import { getMyProfile } from "../../api/authApi";
import { accountKeys } from "../../constants/accountKeys";
import { handleAccountQueryError } from "../utils/handleAccountQueryError";

export function useAccountProfile({ enabled = true } = {}) {
  const query = useQuery({
    queryKey: accountKeys.me(),
    enabled,
    queryFn: async () => {
      try {
        return await getMyProfile();
      } catch (error) {
        await handleAccountQueryError(error);
        throw error;
      }
    },
  });

  const errorCode = query.error?.code ?? null;
  const isNotFound = errorCode === 404;
  const isEmpty = query.isSuccess && (!query.data || !query.data.user);

  return {
    profile: query.data ?? null,
    user: query.data?.user ?? null,
    userProfile: query.data?.profile ?? null,
    settings: query.data?.settings ?? null,
    isLoading: query.isLoading,
    isError: query.isError,
    isNotFound,
    isEmpty,
    errorMessage: isNotFound
      ? "Thông tin tài khoản chưa sẵn sàng."
      : query.error?.message || "Không tải được thông tin tài khoản.",
    retry: query.refetch,
  };
}
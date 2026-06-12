import { useMutation } from "@tanstack/react-query";
import { useCallback, useState } from "react";
import { softDeleteMyAccount } from "../../api/authApi";
import { clearAuthSession } from "../../utils/clearAuthSession";
import { handleAccountQueryError } from "../utils/handleAccountQueryError";

const PASSWORD_REQUIRED_MESSAGE = "Vui lòng nhập mật khẩu hiện tại.";
const WRONG_PASSWORD_MESSAGE = "Mật khẩu không chính xác.";
const GENERIC_ERROR_MESSAGE = "Có lỗi xảy ra. Vui lòng thử lại.";
const ALREADY_DELETED_MESSAGE = "Tài khoản đã được xóa trước đó.";

function resolvePasswordError(error) {
  const passwordError = error?.errors?.find((item) => item.field === "password");
  if (!passwordError) return null;

  if (passwordError.reason === "INVALID_CREDENTIAL") {
    return WRONG_PASSWORD_MESSAGE;
  }

  return passwordError.reason || WRONG_PASSWORD_MESSAGE;
}

export function useDeleteAccount({ onSuccess, onError } = {}) {
  const [password, setPassword] = useState("");
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [fieldError, setFieldError] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);

  const mutation = useMutation({
    mutationFn: () => softDeleteMyAccount({ password }),
    onSuccess: async () => {
      setIsModalOpen(false);
      onSuccess?.();
      await clearAuthSession();
    },
    onError: async (error) => {
      const handled = await handleAccountQueryError(error);
      if (handled) {
        setIsModalOpen(false);
        return;
      }

      if (error?.code === 409) {
        setIsModalOpen(false);
        await clearAuthSession();
        onError?.(ALREADY_DELETED_MESSAGE);
        return;
      }

      const passwordMessage = resolvePasswordError(error);
      const message = passwordMessage || error?.message || GENERIC_ERROR_MESSAGE;
      setFieldError(message);
      onError?.(message);
      setIsModalOpen(false);
    },
  });

  const updatePassword = useCallback((value) => {
    setPassword(value);
    setFieldError("");
  }, []);

  const openConfirmModal = useCallback(() => {
    if (!password.trim()) {
      setFieldError(PASSWORD_REQUIRED_MESSAGE);
      return;
    }
    setFieldError("");
    setIsModalOpen(true);
  }, [password]);

  const closeConfirmModal = useCallback(() => {
    if (mutation.isPending) return;
    setIsModalOpen(false);
  }, [mutation.isPending]);

  const confirmDelete = useCallback(() => {
    if (mutation.isPending) return;
    setFieldError("");
    mutation.mutate();
  }, [mutation]);

  const togglePasswordVisibility = useCallback(() => {
    setIsPasswordVisible((current) => !current);
  }, []);

  return {
    password,
    isPasswordVisible,
    fieldError,
    isModalOpen,
    isSubmitting: mutation.isPending,
    updatePassword,
    openConfirmModal,
    closeConfirmModal,
    confirmDelete,
    togglePasswordVisibility,
  };
}
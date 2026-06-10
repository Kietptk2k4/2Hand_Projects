import { useCallback, useEffect, useState } from "react";
import {
  cancelSellerPayoutRequest,
  createSellerPayoutAccount,
  createSellerPayoutRequest,
  fetchSellerPayoutAccounts,
  fetchSellerPayoutRequests,
} from "../api/sellerPayoutApi";
import {
  mapSellerPayoutAccountsResponse,
  mapSellerPayoutRequest,
  mapSellerPayoutRequestsResponse,
} from "../utils/sellerPayoutMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

const MIN_PAYOUT_AMOUNT = 100_000;

export function useSellerPayout({ onSuccess } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [accounts, setAccounts] = useState([]);
  const [requests, setRequests] = useState([]);
  const [status, setStatus] = useState("idle");
  const [actionStatus, setActionStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    try {
      const [accountsRaw, requestsRaw] = await Promise.all([
        fetchSellerPayoutAccounts(),
        fetchSellerPayoutRequests({ page: 1, limit: 10 }),
      ]);
      setAccounts(mapSellerPayoutAccountsResponse(accountsRaw));
      setRequests(mapSellerPayoutRequestsResponse(requestsRaw).items);
      setStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải được dữ liệu rút tiền.");
      setStatus("error");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const saveAccount = useCallback(
    async (form) => {
      setActionStatus("loading");
      setErrorMessage("");
      try {
        const payload = {
          bank_name: form.bankName.trim(),
          bank_account_name: form.bankAccountName.trim(),
          bank_account_number: form.bankAccountNumber.trim(),
          is_default: Boolean(form.isDefault),
        };
        const created = await createSellerPayoutAccount(payload);
        const mapped = mapSellerPayoutAccountsResponse({ accounts: [created] })[0];
        if (mapped) {
          setAccounts((prev) => {
            const base = payload.is_default ? prev.map((item) => ({ ...item, isDefault: false })) : prev;
            return [...base, mapped];
          });
        } else {
          await load();
        }
        onSuccess?.("Đã lưu tài khoản ngân hàng.");
        setActionStatus("idle");
      } catch (error) {
        setErrorMessage(error?.message || "Không lưu được tài khoản.");
        setActionStatus("error");
      }
    },
    [load, onSuccess],
  );

  const submitPayoutRequest = useCallback(
    async ({ payoutAccountId, amount }) => {
      const parsedAmount = Number(amount);
      if (!Number.isFinite(parsedAmount) || parsedAmount < MIN_PAYOUT_AMOUNT) {
        setErrorMessage(`Số tiền rút tối thiểu là ${MIN_PAYOUT_AMOUNT.toLocaleString("vi-VN")} đ.`);
        return;
      }
      setActionStatus("loading");
      setErrorMessage("");
      try {
        const created = await createSellerPayoutRequest({
          payout_account_id: payoutAccountId,
          amount: parsedAmount,
        });
        const mapped = mapSellerPayoutRequest(created);
        if (mapped) {
          setRequests((prev) => [mapped, ...prev]);
        } else {
          await load();
        }
        onSuccess?.("Đã gửi yêu cầu rút tiền.");
        setActionStatus("idle");
      } catch (error) {
        setErrorMessage(error?.message || "Không tạo được yêu cầu rút tiền.");
        setActionStatus("error");
      }
    },
    [load, onSuccess],
  );

  const cancelRequest = useCallback(
    async (payoutRequestId) => {
      setActionStatus("loading");
      setErrorMessage("");
      try {
        const updated = await cancelSellerPayoutRequest(payoutRequestId);
        const mapped = mapSellerPayoutRequest(updated);
        setRequests((prev) =>
          prev.map((item) => (item.id === payoutRequestId ? mapped || { ...item, status: "CANCELLED" } : item)),
        );
        onSuccess?.("Đã hủy yêu cầu rút tiền.");
        setActionStatus("idle");
      } catch (error) {
        setErrorMessage(error?.message || "Không hủy được yêu cầu.");
        setActionStatus("error");
      }
    },
    [onSuccess],
  );

  return {
    accounts,
    requests,
    status,
    actionStatus,
    errorMessage,
    minPayoutAmount: MIN_PAYOUT_AMOUNT,
    isLoading: status === "loading",
    isSubmitting: actionStatus === "loading",
    reload: load,
    saveAccount,
    submitPayoutRequest,
    cancelRequest,
  };
}

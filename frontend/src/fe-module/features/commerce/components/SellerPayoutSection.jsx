import { useMemo, useState } from "react";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { useSellerPayout } from "../hooks/useSellerPayout";

const STATUS_LABELS = {
  REQUESTED: "Chờ duyệt",
  APPROVED: "Đã duyệt",
  PAID: "Đã chuyển",
  REJECTED: "Từ chối",
  CANCELLED: "Đã hủy",
};

export function SellerPayoutSection({
  availableBalance = 0,
  pendingPayoutAmount = 0,
  onNotify,
  onFinanceChange,
}) {
  const payout = useSellerPayout({ onSuccess: onNotify, onFinanceChange });
  const [accountForm, setAccountForm] = useState({
    bankName: "",
    bankAccountName: "",
    bankAccountNumber: "",
    isDefault: true,
  });
  const [selectedAccountId, setSelectedAccountId] = useState("");
  const [amount, setAmount] = useState("");

  const defaultAccountId = useMemo(() => {
    const defaultAccount = payout.accounts.find((item) => item.isDefault);
    return defaultAccount?.id || payout.accounts[0]?.id || "";
  }, [payout.accounts]);

  const activeAccountId = selectedAccountId || defaultAccountId;

  return (
    <section className="mb-8 rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <div className="mb-6 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-title-lg font-semibold text-on-surface">Rút tiền</h2>
          <p className="mt-1 text-body-md text-on-surface-variant">
            Tối thiểu {formatVndPrice(payout.minPayoutAmount)}. Số dư khả dụng:{" "}
            <span className="font-medium text-primary">{formatVndPrice(availableBalance)}</span>
            {pendingPayoutAmount > 0 ? (
              <>
                {" "}
                · Đang chờ rút:{" "}
                <span className="font-medium text-on-surface">
                  {formatVndPrice(pendingPayoutAmount)}
                </span>
              </>
            ) : null}
          </p>
        </div>
        <button
          type="button"
          onClick={() => {
            payout.reload();
            onFinanceChange?.();
          }}
          disabled={payout.isLoading}
          className="rounded-lg border border-outline-variant px-3 py-2 text-label-md text-on-surface hover:bg-surface-container-high disabled:opacity-60"
        >
          Làm mới
        </button>
      </div>

      {payout.errorMessage ? (
        <div className="mb-4 rounded-lg border border-error/30 bg-error-container/20 px-3 py-2 text-body-sm text-on-error-container">
          {payout.errorMessage}
        </div>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-2">
        <div>
          <h3 className="mb-3 text-title-md font-medium text-on-surface">Tài khoản ngân hàng</h3>
          <form
            className="space-y-3"
            onSubmit={(event) => {
              event.preventDefault();
              payout.saveAccount(accountForm);
            }}
          >
            <input
              className="w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-body-md"
              placeholder="Tên ngân hàng"
              value={accountForm.bankName}
              onChange={(event) => setAccountForm((prev) => ({ ...prev, bankName: event.target.value }))}
              required
            />
            <input
              className="w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-body-md"
              placeholder="Tên chủ tài khoản"
              value={accountForm.bankAccountName}
              onChange={(event) => setAccountForm((prev) => ({ ...prev, bankAccountName: event.target.value }))}
              required
            />
            <input
              className="w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-body-md"
              placeholder="Số tài khoản"
              value={accountForm.bankAccountNumber}
              onChange={(event) => setAccountForm((prev) => ({ ...prev, bankAccountNumber: event.target.value }))}
              required
            />
            <label className="flex items-center gap-2 text-body-sm text-on-surface-variant">
              <input
                type="checkbox"
                checked={accountForm.isDefault}
                onChange={(event) => setAccountForm((prev) => ({ ...prev, isDefault: event.target.checked }))}
              />
              Đặt làm tài khoản mặc định
            </label>
            <button
              type="submit"
              disabled={payout.isSubmitting}
              className="rounded-lg bg-primary px-4 py-2 text-label-md text-on-primary disabled:opacity-60"
            >
              Thêm tài khoản
            </button>
          </form>

          {payout.accounts.length ? (
            <ul className="mt-4 space-y-2 text-body-sm">
              {payout.accounts.map((account) => (
                <li
                  key={account.id}
                  className="rounded-lg border border-outline-variant/70 px-3 py-2 text-on-surface"
                >
                  <p className="font-medium">
                    {account.bankName} · {account.bankAccountNumber}
                    {account.isDefault ? (
                      <span className="ml-2 rounded-full bg-primary-container px-2 py-0.5 text-label-sm text-on-primary-container">
                        Mặc định
                      </span>
                    ) : null}
                  </p>
                  <p className="text-on-surface-variant">{account.bankAccountName}</p>
                </li>
              ))}
            </ul>
          ) : null}
        </div>

        <div>
          <h3 className="mb-3 text-title-md font-medium text-on-surface">Yêu cầu rút tiền</h3>
          <form
            className="space-y-3"
            onSubmit={(event) => {
              event.preventDefault();
              payout.submitPayoutRequest({ payoutAccountId: activeAccountId, amount });
              setAmount("");
            }}
          >
            <select
              className="w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-body-md"
              value={activeAccountId}
              onChange={(event) => setSelectedAccountId(event.target.value)}
              required
              disabled={!payout.accounts.length}
            >
              {!payout.accounts.length ? <option value="">Chưa có tài khoản</option> : null}
              {payout.accounts.map((account) => (
                <option key={account.id} value={account.id}>
                  {account.bankName} - {account.bankAccountNumber}
                </option>
              ))}
            </select>
            <input
              type="number"
              min={payout.minPayoutAmount}
              step="1000"
              className="w-full rounded-lg border border-outline-variant bg-surface px-3 py-2 text-body-md"
              placeholder="Số tiền rút (VND)"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
              required
              disabled={!payout.accounts.length}
            />
            <button
              type="submit"
              disabled={payout.isSubmitting || !payout.accounts.length}
              className="rounded-lg bg-primary px-4 py-2 text-label-md text-on-primary disabled:opacity-60"
            >
              Gửi yêu cầu rút tiền
            </button>
          </form>

          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full text-left text-body-sm">
              <thead>
                <tr className="border-b border-outline-variant text-on-surface-variant">
                  <th className="px-2 py-2">Thời gian</th>
                  <th className="px-2 py-2">Số tiền</th>
                  <th className="px-2 py-2">Trạng thái</th>
                  <th className="px-2 py-2" />
                </tr>
              </thead>
              <tbody>
                {payout.isLoading ? (
                  <tr>
                    <td colSpan={4} className="px-2 py-4 text-on-surface-variant">
                      Đang tải...
                    </td>
                  </tr>
                ) : payout.requests.length ? (
                  payout.requests.map((request) => (
                    <tr key={request.id} className="border-b border-outline-variant/50">
                      <td className="px-2 py-2">
                        {request.requestedAt
                          ? new Date(request.requestedAt).toLocaleString("vi-VN")
                          : "—"}
                      </td>
                      <td className="px-2 py-2 font-medium">{formatVndPrice(request.amount)}</td>
                      <td className="px-2 py-2">{STATUS_LABELS[request.status] || request.status}</td>
                      <td className="px-2 py-2">
                        {request.status === "REQUESTED" ? (
                          <button
                            type="button"
                            onClick={() => payout.cancelRequest(request.id)}
                            className="text-label-sm text-error hover:underline"
                          >
                            Hủy
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={4} className="px-2 py-4 text-on-surface-variant">
                      Chưa có yêu cầu rút tiền.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  );
}

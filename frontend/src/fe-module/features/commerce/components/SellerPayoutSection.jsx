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

const STATUS_BADGES = {
  REQUESTED: "bg-amber-50 text-amber-700 border border-amber-200",
  APPROVED: "bg-emerald-50 text-emerald-700 border border-emerald-200",
  PAID: "bg-blue-50 text-blue-700 border border-blue-200",
  REJECTED: "bg-rose-50 text-rose-700 border border-rose-200",
  CANCELLED: "bg-slate-100 text-slate-600 border border-slate-200",
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
    <section className="mb-8 rounded-2xl border border-outline-variant bg-surface-container-lowest p-6 shadow-xs">
      {/* Header */}
      <div className="mb-6 flex flex-wrap items-start justify-between gap-3 border-b border-outline-variant/60 pb-4">
        <div>
          <h2 className="text-lg font-black text-on-surface sm:text-xl flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-2xl">account_balance_wallet</span>
            VÍ & YÊU CẦU RÚT TIỀN
          </h2>
          <p className="mt-1 text-xs font-semibold text-on-surface-variant">
            Rút tối thiểu {formatVndPrice(payout.minPayoutAmount)}. Số dư khả dụng:{" "}
            <span className="font-bold text-emerald-600">{formatVndPrice(availableBalance)}</span>
            {pendingPayoutAmount > 0 ? (
              <>
                {" "}
                · Đang chờ rút:{" "}
                <span className="font-bold text-amber-600">
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
          className="inline-flex items-center gap-1.5 rounded-xl border border-outline-variant bg-surface-container-lowest px-4 py-2 text-xs font-bold text-on-surface hover:bg-surface-container-low shadow-xs cursor-pointer disabled:opacity-60"
        >
          <span className="material-symbols-outlined text-sm">refresh</span>
          Làm mới
        </button>
      </div>

      {payout.errorMessage ? (
        <div className="mb-6 rounded-xl border border-error/30 bg-error-container/30 px-4 py-3 text-xs font-semibold text-on-error-container">
          {payout.errorMessage}
        </div>
      ) : null}

      <div className="grid gap-8 lg:grid-cols-2">
        {/* Left Column: Bank Account Registration */}
        <div className="rounded-xl border border-outline-variant/60 bg-surface-container-low/40 p-5">
          <h3 className="mb-4 text-xs font-bold uppercase tracking-wider text-on-surface flex items-center gap-1.5">
            <span className="material-symbols-outlined text-sm text-primary">account_balance</span>
            Tài khoản ngân hàng
          </h3>
          <form
            className="space-y-3"
            onSubmit={(event) => {
              event.preventDefault();
              payout.saveAccount(accountForm);
            }}
          >
            <div>
              <input
                className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-2.5 px-3.5 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm"
                placeholder="Tên ngân hàng (ví dụ: Vietcombank, MBBank)"
                value={accountForm.bankName}
                onChange={(event) => setAccountForm((prev) => ({ ...prev, bankName: event.target.value }))}
                required
              />
            </div>
            <div>
              <input
                className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-2.5 px-3.5 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm"
                placeholder="Tên chủ tài khoản"
                value={accountForm.bankAccountName}
                onChange={(event) => setAccountForm((prev) => ({ ...prev, bankAccountName: event.target.value }))}
                required
              />
            </div>
            <div>
              <input
                className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-2.5 px-3.5 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm"
                placeholder="Số tài khoản"
                value={accountForm.bankAccountNumber}
                onChange={(event) => setAccountForm((prev) => ({ ...prev, bankAccountNumber: event.target.value }))}
                required
              />
            </div>
            <label className="flex items-center gap-2 text-xs font-semibold text-on-surface-variant cursor-pointer">
              <input
                type="checkbox"
                checked={accountForm.isDefault}
                onChange={(event) => setAccountForm((prev) => ({ ...prev, isDefault: event.target.checked }))}
                className="h-4 w-4 rounded border-outline-variant text-primary focus:ring-primary"
              />
              Đặt làm tài khoản mặc định
            </label>
            <button
              type="submit"
              disabled={payout.isSubmitting}
              className="mt-2 rounded-xl bg-primary px-5 py-2.5 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] disabled:opacity-60 cursor-pointer"
            >
              Thêm tài khoản
            </button>
          </form>

          {payout.accounts.length ? (
            <div className="mt-5 space-y-2 border-t border-outline-variant/40 pt-4">
              <p className="text-[11px] font-bold uppercase tracking-wider text-on-surface-variant">Tài khoản đã lưu:</p>
              {payout.accounts.map((account) => (
                <div
                  key={account.id}
                  className="rounded-xl border border-outline-variant/80 bg-surface-container-lowest p-3 shadow-xs"
                >
                  <div className="flex items-center justify-between">
                    <p className="text-xs font-bold text-on-surface">
                      {account.bankName} · {account.bankAccountNumber}
                    </p>
                    {account.isDefault ? (
                      <span className="rounded-md bg-emerald-50 px-2 py-0.5 text-[10px] font-bold text-emerald-700 border border-emerald-200">
                        Mặc định
                      </span>
                    ) : null}
                  </div>
                  <p className="mt-0.5 text-[11px] font-semibold text-on-surface-variant">{account.bankAccountName}</p>
                </div>
              ))}
            </div>
          ) : null}
        </div>

        {/* Right Column: Withdrawal Request & History */}
        <div className="rounded-xl border border-outline-variant/60 bg-surface-container-low/40 p-5">
          <h3 className="mb-4 text-xs font-bold uppercase tracking-wider text-on-surface flex items-center gap-1.5">
            <span className="material-symbols-outlined text-sm text-emerald-600">payments</span>
            Yêu cầu rút tiền
          </h3>
          <form
            className="space-y-3"
            onSubmit={(event) => {
              event.preventDefault();
              payout.submitPayoutRequest({ payoutAccountId: activeAccountId, amount });
              setAmount("");
            }}
          >
            <div>
              <select
                className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-2.5 px-3.5 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm"
                value={activeAccountId}
                onChange={(event) => setSelectedAccountId(event.target.value)}
                required
                disabled={!payout.accounts.length}
              >
                {!payout.accounts.length ? <option value="">Chưa có tài khoản ngân hàng</option> : null}
                {payout.accounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.bankName} - {account.bankAccountNumber} ({account.bankAccountName})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <input
                type="number"
                min={payout.minPayoutAmount}
                step="1000"
                className="w-full rounded-2xl border border-outline-variant bg-surface-container-lowest py-2.5 px-3.5 text-xs text-on-surface shadow-xs transition-all focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20 sm:text-sm"
                placeholder="Số tiền rút (VND)"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                required
                disabled={!payout.accounts.length}
              />
            </div>
            <button
              type="submit"
              disabled={payout.isSubmitting || !payout.accounts.length}
              className="mt-2 rounded-xl bg-emerald-600 px-5 py-2.5 text-xs font-bold text-white shadow-xs transition-all hover:bg-emerald-700 disabled:opacity-60 cursor-pointer"
            >
              Gửi yêu cầu rút tiền
            </button>
          </form>

          {/* History Table */}
          <div className="mt-5 border-t border-outline-variant/40 pt-4">
            <p className="mb-2 text-[11px] font-bold uppercase tracking-wider text-on-surface-variant">Lịch sử rút tiền:</p>
            <div className="overflow-x-auto rounded-xl border border-outline-variant/80 bg-surface-container-lowest shadow-xs">
              <table className="min-w-full text-left text-xs">
                <thead className="border-b border-outline-variant/60 bg-surface-container-low text-[10px] font-bold uppercase tracking-wider text-on-surface-variant">
                  <tr>
                    <th className="px-3 py-2">Thời gian</th>
                    <th className="px-3 py-2">Số tiền</th>
                    <th className="px-3 py-2">Trạng thái</th>
                    <th className="px-3 py-2 text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-outline-variant/40">
                  {payout.isLoading ? (
                    <tr>
                      <td colSpan={4} className="px-3 py-4 text-center text-slate-400">
                        Đang tải...
                      </td>
                    </tr>
                  ) : payout.requests.length ? (
                    payout.requests.map((request) => (
                      <tr key={request.id}>
                        <td className="px-3 py-2 text-on-surface-variant">
                          {request.requestedAt
                            ? new Date(request.requestedAt).toLocaleString("vi-VN")
                            : "—"}
                        </td>
                        <td className="px-3 py-2 font-bold text-on-surface">{formatVndPrice(request.amount)}</td>
                        <td className="px-3 py-2">
                          <span className={`inline-block rounded-md px-2 py-0.5 text-[10px] font-bold ${STATUS_BADGES[request.status] || "bg-slate-100 text-slate-600"}`}>
                            {STATUS_LABELS[request.status] || request.status}
                          </span>
                        </td>
                        <td className="px-3 py-2 text-right">
                          {request.status === "REQUESTED" ? (
                            <button
                              type="button"
                              onClick={() => payout.cancelRequest(request.id)}
                              className="text-xs font-bold text-rose-600 hover:underline cursor-pointer"
                            >
                              Hủy
                            </button>
                          ) : null}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={4} className="px-3 py-4 text-center text-slate-400">
                        Chưa có yêu cầu rút tiền.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

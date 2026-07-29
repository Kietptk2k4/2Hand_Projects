import { useCallback, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { CommerceShell } from "../components/CommerceShell";
import { SellerPayoutSection } from "../components/SellerPayoutSection";
import { SellerRevenueBucketCards } from "../components/SellerRevenueBucketCards";
import { SellerRevenueTrendChart } from "../components/SellerRevenueTrendChart";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { useSellerFinanceAnalytics } from "../hooks/useSellerFinanceAnalytics";

const GRANULARITY_OPTIONS = [
  { value: "DAY", label: "Theo ngày" },
  { value: "WEEK", label: "Theo tuần" },
  { value: "MONTH", label: "Theo tháng" },
];

export function CommerceSellerAnalyticsPage() {
  const [granularity, setGranularity] = useState("DAY");
  const [toastMessage, setToastMessage] = useState("");
  const { summary, trend, ledger, isLoading, errorMessage, retry } = useSellerFinanceAnalytics({
    granularity,
  });

  const showComingSoon = useCallback((message) => {
    setToastMessage(message || "Tính năng đang được phát triển.");
  }, []);

  const dismissToast = useCallback(() => setToastMessage(""), []);

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mx-auto w-full max-w-[1280px]">
        {/* Header */}
        <header className="mb-6 flex flex-wrap items-start justify-between gap-4 border-b border-outline-variant/60 pb-4">
          <div>
            <h1 className="text-xl font-black text-on-surface sm:text-2xl">
              THỐNG KÊ DOANH THU & VÍ SELLER
            </h1>
            <p className="mt-1 text-xs font-semibold text-on-surface-variant">
              Theo dõi doanh thu theo trạng thái đơn hàng (chỉ tính giá sản phẩm, chưa gồm phí ship)
            </p>
          </div>
          <button
            type="button"
            onClick={retry}
            disabled={isLoading}
            className="inline-flex items-center gap-1.5 rounded-xl border border-outline-variant bg-surface-container-lowest px-5 py-2.5 text-xs font-bold text-on-surface shadow-xs transition-all hover:bg-surface-container-low disabled:opacity-60 cursor-pointer"
          >
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              refresh
            </span>
            Làm mới
          </button>
        </header>

        {/* Error Notification */}
        {errorMessage ? (
          <div className="mb-6 rounded-2xl border border-error/30 bg-error-container/40 p-4 text-xs font-semibold text-on-error-container shadow-xs">
            {errorMessage}
          </div>
        ) : null}

        {/* 3 Main Finance Metric Cards */}
        <section className="mb-6 grid gap-4 md:grid-cols-3">
          {/* Card 1: Gross Revenue */}
          <div className="flex items-center justify-between rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md">
            <div>
              <p className="text-xs font-bold text-on-surface-variant">Tổng doanh thu (Gross)</p>
              <p className="mt-2 text-2xl font-black text-on-surface">
                {isLoading ? "—" : formatVndPrice(summary?.totalGross ?? 0)}
              </p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl border bg-blue-50 text-blue-600 border-blue-200">
              <span className="material-symbols-outlined text-xl" aria-hidden="true">
                payments
              </span>
            </div>
          </div>

          {/* Card 2: Available Balance */}
          <div className="flex items-center justify-between rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md">
            <div>
              <p className="text-xs font-bold text-on-surface-variant">Số dư khả dụng (sau phí 10%)</p>
              <p className="mt-2 text-2xl font-black text-emerald-600">
                {isLoading ? "—" : formatVndPrice(summary?.balance?.availableBalance ?? 0)}
              </p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl border bg-emerald-50 text-emerald-600 border-emerald-200">
              <span className="material-symbols-outlined text-xl" aria-hidden="true">
                account_balance_wallet
              </span>
            </div>
          </div>

          {/* Card 3: Platform Fee */}
          <div className="flex items-center justify-between rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs transition-all hover:-translate-y-0.5 hover:shadow-md">
            <div>
              <p className="text-xs font-bold text-on-surface-variant">Phí sàn đã ghi nhận (10%)</p>
              <p className="mt-2 text-2xl font-black text-purple-600">
                {isLoading ? "—" : formatVndPrice(summary?.balance?.totalPlatformFee ?? 0)}
              </p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl border bg-purple-50 text-purple-600 border-purple-200">
              <span className="material-symbols-outlined text-xl" aria-hidden="true">
                receipt_long
              </span>
            </div>
          </div>
        </section>

        {/* Status Bucket Cards */}
        <section className="mb-8">
          <SellerRevenueBucketCards summary={summary} isLoading={isLoading} />
        </section>

        {/* Wallet & Payout Section */}
        <div id="payout">
          <SellerPayoutSection
            availableBalance={summary?.balance?.availableBalance ?? 0}
            pendingPayoutAmount={summary?.balance?.pendingPayoutAmount ?? 0}
            onNotify={setToastMessage}
            onFinanceChange={retry}
          />
        </div>

        {/* Revenue Trend Chart */}
        <section className="mb-8 rounded-2xl border border-outline-variant bg-surface-container-lowest p-6 shadow-xs">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3 border-b border-outline-variant/60 pb-3">
            <h2 className="text-lg font-black text-on-surface">Biểu đồ doanh thu</h2>
            <div className="flex flex-wrap gap-2">
              {GRANULARITY_OPTIONS.map((option) => {
                const active = granularity === option.value;
                return (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => setGranularity(option.value)}
                    className={[
                      "rounded-xl px-4 py-1.5 text-xs font-bold transition-all cursor-pointer",
                      active
                        ? "bg-primary text-on-primary shadow-xs"
                        : "border border-outline-variant bg-surface-container-lowest text-on-surface-variant hover:bg-surface-container-high",
                    ].join(" ")}
                  >
                    {option.label}
                  </button>
                );
              })}
            </div>
          </div>
          <SellerRevenueTrendChart trend={trend} isLoading={isLoading} />
        </section>

        {/* Ledger Table */}
        <section className="mb-8">
          <h2 className="mb-4 text-lg font-black text-on-surface">Sổ cái ghi nhận</h2>
          <div className="overflow-hidden rounded-2xl border border-outline-variant bg-surface-container-lowest shadow-xs">
            <div className="overflow-x-auto">
              <table className="min-w-full text-left text-xs">
                <thead className="border-b border-outline-variant/60 bg-surface-container-low text-[11px] font-bold uppercase tracking-wider text-on-surface-variant">
                  <tr>
                    <th className="px-4 py-3.5">Thời gian</th>
                    <th className="px-4 py-3.5">Doanh thu Gross</th>
                    <th className="px-4 py-3.5">Phí sàn (10%)</th>
                    <th className="px-4 py-3.5 font-bold">Thực nhận (Net)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-outline-variant/40">
                  {isLoading ? (
                    <tr>
                      <td colSpan={4} className="px-4 py-8 text-center text-slate-400 font-semibold">
                        Đang tải bút toán...
                      </td>
                    </tr>
                  ) : ledger?.items?.length ? (
                    ledger.items.map((entry) => (
                      <tr key={entry.id} className="transition-colors hover:bg-surface-container-low/50">
                        <td className="px-4 py-3.5 text-on-surface font-medium">
                          {entry.createdAt
                            ? new Date(entry.createdAt).toLocaleString("vi-VN")
                            : "—"}
                        </td>
                        <td className="px-4 py-3.5 font-semibold text-on-surface">
                          {formatVndPrice(entry.grossAmount)}
                        </td>
                        <td className="px-4 py-3.5 text-purple-600 font-semibold">
                          {formatVndPrice(entry.platformFeeAmount)}
                        </td>
                        <td className="px-4 py-3.5 font-black text-emerald-600">
                          {formatVndPrice(entry.netAmount)}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={4} className="px-4 py-8 text-center text-slate-400 font-semibold">
                        Chưa có bút toán ghi nhận.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </section>

        {/* E-Commerce Trust Badges Footer */}
        <CommerceFooterTrustSection />
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}

import { useCallback, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { CommerceShell } from "../components/CommerceShell";
import { SellerPayoutSection } from "../components/SellerPayoutSection";
import { SellerRevenueBucketCards } from "../components/SellerRevenueBucketCards";
import { SellerRevenueTrendChart } from "../components/SellerRevenueTrendChart";
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
        <header className="mb-6 flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-headline-lg-mobile font-bold text-on-surface md:text-headline-lg">
              Thống kê doanh thu
            </h1>
            <p className="mt-1 text-body-md text-on-surface-variant">
              Theo dõi doanh thu theo trạng thái đơn hàng. Chỉ tính giá hàng, không gồm phí vận chuyển.
            </p>
          </div>
          <button
            type="button"
            onClick={retry}
            disabled={isLoading}
            className="inline-flex items-center gap-2 rounded-lg border border-outline-variant px-4 py-2 text-label-md text-on-surface transition-colors hover:bg-surface-container-high disabled:opacity-60"
          >
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              refresh
            </span>
            Làm mới
          </button>
        </header>

        {errorMessage ? (
          <div className="mb-6 rounded-xl border border-error/30 bg-error-container/30 px-4 py-3 text-body-md text-on-error-container">
            {errorMessage}
          </div>
        ) : null}

        <section className="mb-6 grid gap-4 md:grid-cols-3">
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
            <p className="text-label-md text-on-surface-variant">Tổng doanh thu (gross)</p>
            <p className="mt-2 text-headline-md font-bold text-on-surface">
              {isLoading ? "—" : formatVndPrice(summary?.totalGross ?? 0)}
            </p>
          </div>
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
            <p className="text-label-md text-on-surface-variant">Số dư khả dụng (sau phí 10%)</p>
            <p className="mt-2 text-headline-md font-bold text-primary">
              {isLoading ? "—" : formatVndPrice(summary?.balance?.availableBalance ?? 0)}
            </p>
          </div>
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
            <p className="text-label-md text-on-surface-variant">Phí sàn đã ghi nhận</p>
            <p className="mt-2 text-headline-md font-bold text-on-surface">
              {isLoading ? "—" : formatVndPrice(summary?.balance?.totalPlatformFee ?? 0)}
            </p>
          </div>
        </section>

        <section className="mb-8">
          <SellerRevenueBucketCards summary={summary} isLoading={isLoading} />
        </section>

        <SellerPayoutSection
          availableBalance={summary?.balance?.availableBalance ?? 0}
          onNotify={setToastMessage}
        />

        <section className="mb-8">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-title-lg font-semibold text-on-surface">Biểu đồ doanh thu</h2>
            <div className="flex flex-wrap gap-2">
              {GRANULARITY_OPTIONS.map((option) => {
                const active = granularity === option.value;
                return (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => setGranularity(option.value)}
                    className={[
                      "rounded-full px-4 py-2 text-label-md transition-colors",
                      active
                        ? "bg-primary text-on-primary"
                        : "border border-outline-variant text-on-surface-variant hover:bg-surface-container-high",
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

        <section>
          <h2 className="mb-4 text-title-lg font-semibold text-on-surface">Sổ cái ghi nhận</h2>
          <div className="overflow-x-auto rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
            <table className="min-w-full text-left text-body-sm">
              <thead className="border-b border-outline-variant bg-surface-container-low">
                <tr>
                  <th className="px-4 py-3 font-medium text-on-surface-variant">Thời gian</th>
                  <th className="px-4 py-3 font-medium text-on-surface-variant">Gross</th>
                  <th className="px-4 py-3 font-medium text-on-surface-variant">Phí sàn</th>
                  <th className="px-4 py-3 font-medium text-on-surface-variant">Net</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td colSpan={4} className="px-4 py-8 text-center text-on-surface-variant">
                      Đang tải...
                    </td>
                  </tr>
                ) : ledger?.items?.length ? (
                  ledger.items.map((entry) => (
                    <tr key={entry.id} className="border-b border-outline-variant/60 last:border-0">
                      <td className="px-4 py-3 text-on-surface">
                        {entry.createdAt
                          ? new Date(entry.createdAt).toLocaleString("vi-VN")
                          : "—"}
                      </td>
                      <td className="px-4 py-3">{formatVndPrice(entry.grossAmount)}</td>
                      <td className="px-4 py-3 text-on-surface-variant">
                        {formatVndPrice(entry.platformFeeAmount)}
                      </td>
                      <td className="px-4 py-3 font-medium text-on-surface">
                        {formatVndPrice(entry.netAmount)}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={4} className="px-4 py-8 text-center text-on-surface-variant">
                      Chưa có bút toán ghi nhận.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}

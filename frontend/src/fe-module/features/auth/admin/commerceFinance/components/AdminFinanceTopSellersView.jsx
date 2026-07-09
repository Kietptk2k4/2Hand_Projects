import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { TopSellersTable } from "./TopSellersTable.jsx";
import { CommerceFinanceEmptyState } from "./ui/CommerceFinanceEmptyState.jsx";
import { CommerceFinanceListSkeleton } from "./ui/CommerceFinanceListSkeleton.jsx";
import { CommerceFinanceRetryPanel } from "./ui/CommerceFinanceRetryPanel.jsx";

export function AdminFinanceTopSellersView({
  title,
  subtitle,
  status,
  errorMessage,
  sellers,
  onRetry,
  onSellerSelect,
}) {
  if (status === "error") {
    return (
      <div className="max-w-full min-w-0 space-y-4">
        <AdminPageHeader title={title} subtitle={subtitle} />
        <CommerceFinanceRetryPanel message={errorMessage} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className="max-w-full min-w-0 space-y-4">
      <AdminPageHeader title={title} subtitle={subtitle} />

      {status === "loading" ? <CommerceFinanceListSkeleton rows={5} /> : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="md" className="max-w-full min-w-0">
          {sellers?.length ? (
            <TopSellersTable sellers={sellers} onSellerSelect={onSellerSelect} />
          ) : (
            <CommerceFinanceEmptyState message="Chưa có dữ liệu seller." />
          )}
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}

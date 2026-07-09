import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { SellerRevenueBucketCards } from "../../../../commerce/components/SellerRevenueBucketCards.jsx";
import { CommerceFinanceListSkeleton } from "./ui/CommerceFinanceListSkeleton.jsx";
import { CommerceFinanceRetryPanel } from "./ui/CommerceFinanceRetryPanel.jsx";

export function AdminFinanceCodPipelineView({
  title,
  subtitle,
  status,
  errorMessage,
  pipeline,
  onRetry,
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

      {status === "loading" ? (
        <CommerceFinanceListSkeleton rows={3} />
      ) : (
        <AdminSurfaceCard padding="md" className="max-w-full min-w-0">
          <SellerRevenueBucketCards summary={pipeline} isLoading={false} />
        </AdminSurfaceCard>
      )}
    </div>
  );
}

import {
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../../components/ui";
import {
  ORDER_SUPPORT_PAYMENT_SUBTITLE,
  ORDER_SUPPORT_PAYMENT_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { PaymentSupportDetailPanel } from "../PaymentSupportDetailPanel.jsx";
import { PaymentSupportFilterBar } from "../PaymentSupportFilterBar.jsx";
import { PaymentSupportTable } from "../PaymentSupportTable.jsx";
import { SupportForbiddenState } from "../SupportForbiddenState.jsx";
import { SupportListSkeleton } from "../ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "../ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "../SupportUnavailableState.jsx";

export function PaymentSupportDetailTabView({
  canReadPayment,
  listStatus,
  listErrorMessage,
  listResult,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onClearFilters,
  onRetryList,
  paymentId,
  orderId,
  currentPage,
  totalPages,
  onPageChange,
  onPaymentSelect,
  onNavigate,
  formatDateTime,
  formatVndPrice,
}) {
  const summary =
    listStatus === "ready"
      ? `${listResult?.total_elements ?? 0} thanh toán · Trang ${listResult?.page ?? currentPage}/${totalPages} · Sắp xếp: mới nhất trước`
      : "";

  return (
    <div className="w-full min-w-0 space-y-4">
      <AdminPageHeader title={ORDER_SUPPORT_PAYMENT_TITLE} subtitle={ORDER_SUPPORT_PAYMENT_SUBTITLE} />

      {!canReadPayment ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền PAYMENT_SUPPORT_READ." />
      ) : null}

      <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
        <PaymentSupportFilterBar
          draftFilters={draftFilters}
          onDraftFiltersChange={onDraftFiltersChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </AdminSurfaceCard>

      {listStatus === "loading" ? <SupportListSkeleton /> : null}
      {listStatus === "forbidden" ? <SupportForbiddenState message={listErrorMessage} /> : null}
      {listStatus === "unavailable" ? <SupportUnavailableState message={listErrorMessage} /> : null}
      {listStatus === "error" ? (
        <SupportRetryPanel message={listErrorMessage} onRetry={onRetryList} />
      ) : null}

      {listStatus === "ready" ? (
        <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
          <AdminPagination
            currentPage={currentPage}
            totalPages={totalPages}
            summary={summary}
            onPrevious={() => onPageChange(currentPage - 1)}
            onNext={() => onPageChange(currentPage + 1)}
            className="mb-4"
          />
          <PaymentSupportTable
            payments={listResult?.payments ?? []}
            selectedPaymentId={paymentId}
            onPaymentSelect={onPaymentSelect}
            formatDateTime={formatDateTime}
            formatVndPrice={formatVndPrice}
          />
        </AdminSurfaceCard>
      ) : null}

      {paymentId ? (
        <div>
          <h3 className="mb-3 text-base font-semibold text-admin-text">Chi tiết thanh toán đang xem</h3>
          <PaymentSupportDetailPanel paymentId={paymentId} orderId={orderId} onNavigate={onNavigate} />
        </div>
      ) : null}
    </div>
  );
}

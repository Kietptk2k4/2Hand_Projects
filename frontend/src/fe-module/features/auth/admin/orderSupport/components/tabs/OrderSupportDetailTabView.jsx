import {
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../../components/ui";
import { ORDER_LIST_SORT_OPTIONS } from "../../constants/orderListConstants.js";
import {
  ORDER_SUPPORT_ORDER_SUBTITLE,
  ORDER_SUPPORT_ORDER_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { OrderSupportFilterBar } from "../OrderSupportFilterBar.jsx";
import { OrderSupportTable } from "../OrderSupportTable.jsx";
import { OrderSupportDetailPanel } from "../OrderSupportDetailPanel.jsx";
import { SupportForbiddenState } from "../SupportForbiddenState.jsx";
import { SupportListSkeleton } from "../ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "../ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "../SupportUnavailableState.jsx";

function sortColumnLabel(sortField) {
  const option = ORDER_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Tạo gần nhất";
}

export function OrderSupportDetailTabView({
  canReadOrder,
  listStatus,
  listErrorMessage,
  listResult,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onClearFilters,
  onRetryList,
  orderId,
  currentPage,
  totalPages,
  activeSort,
  onPageChange,
  onOrderSelect,
  onNavigate,
  formatDateTime,
  formatVndPrice,
}) {
  const summary =
    listStatus === "ready"
      ? `${listResult?.total_elements ?? 0} đơn hàng · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${listResult?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="w-full min-w-0 space-y-4">
      <AdminPageHeader title={ORDER_SUPPORT_ORDER_TITLE} subtitle={ORDER_SUPPORT_ORDER_SUBTITLE} />

      {!canReadOrder ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền ORDER_SUPPORT_READ." />
      ) : null}

      <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
        <OrderSupportFilterBar
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
          <OrderSupportTable
            orders={listResult?.orders ?? []}
            selectedOrderId={orderId}
            onOrderSelect={onOrderSelect}
            formatDateTime={formatDateTime}
            formatVndPrice={formatVndPrice}
          />
        </AdminSurfaceCard>
      ) : null}

      {orderId ? (
        <div>
          <h3 className="mb-3 text-base font-semibold text-admin-text">Chi tiết đơn hàng đang xem</h3>
          <OrderSupportDetailPanel orderId={orderId} onNavigate={onNavigate} />
        </div>
      ) : null}
    </div>
  );
}

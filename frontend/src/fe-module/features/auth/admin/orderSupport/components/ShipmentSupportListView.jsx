import { AdminPagination, AdminSurfaceCard } from "../../components/ui";
import { SHIPMENT_LIST_SORT_OPTIONS } from "../constants/shipmentListConstants.js";
import { ShipmentSupportFilterBar } from "./ShipmentSupportFilterBar.jsx";
import { ShipmentSupportTable } from "./ShipmentSupportTable.jsx";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportListSkeleton } from "./ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "./ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";

function sortColumnLabel(sortField) {
  const option = SHIPMENT_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Cập nhật gần nhất";
}

export function ShipmentSupportListView({
  canReadShipment,
  status,
  errorMessage,
  result,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onClearFilters,
  onRetry,
  selectedShipmentId,
  onShipmentSelect,
  currentPage,
  totalPages,
  activeSort,
  onPageChange,
  formatDateTime,
}) {
  const summary =
    status === "ready"
      ? `${result?.total_elements ?? 0} vận đơn · Sắp xếp: ${sortColumnLabel(activeSort)} · Trang ${result?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="space-y-4">
      {!canReadShipment ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền SHIPMENT_SUPPORT_READ." />
      ) : null}

      <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
        <ShipmentSupportFilterBar
          draftFilters={draftFilters}
          onDraftFiltersChange={onDraftFiltersChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </AdminSurfaceCard>

      {status === "loading" ? <SupportListSkeleton /> : null}
      {status === "forbidden" ? <SupportForbiddenState message={errorMessage} /> : null}
      {status === "unavailable" ? <SupportUnavailableState message={errorMessage} /> : null}
      {status === "error" ? <SupportRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
          <AdminPagination
            currentPage={currentPage}
            totalPages={totalPages}
            summary={summary}
            onPrevious={() => onPageChange(currentPage - 1)}
            onNext={() => onPageChange(currentPage + 1)}
            className="mb-4"
          />
          <ShipmentSupportTable
            shipments={result?.shipments ?? []}
            selectedShipmentId={selectedShipmentId}
            onShipmentSelect={onShipmentSelect}
            formatDateTime={formatDateTime}
          />
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}

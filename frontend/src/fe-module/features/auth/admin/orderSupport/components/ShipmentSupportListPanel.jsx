import { useCallback, useEffect, useState } from "react";
import { listShipmentSupport } from "../api/orderSupportApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AccountCard, AccountSkeleton } from "../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { ORDER_SUPPORT_PERMISSIONS } from "../constants/orderSupportPermissions.js";
import {
  SHIPMENT_LIST_CARRIER_OPTIONS,
  SHIPMENT_LIST_PAGE_SIZE,
  SHIPMENT_LIST_SORT_OPTIONS,
  SHIPMENT_LIST_STATUS_OPTIONS,
} from "../constants/shipmentListConstants.js";
import { useOrderSupportPermissions } from "../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../utils/orderSupportTabErrors.js";
import { SupportForbiddenState } from "./SupportForbiddenState.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";
import { SupportUnavailableState } from "./SupportUnavailableState.jsx";

function sortColumnLabel(sortField) {
  const option = SHIPMENT_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Cập nhật gần nhất";
}

export function ShipmentSupportListPanel({
  shipmentListFilters,
  onFiltersChange,
  selectedShipmentId,
  onShipmentSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canReadShipment } = useOrderSupportPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const [draftFilters, setDraftFilters] = useState({
    status: shipmentListFilters.status || "",
    carrier: shipmentListFilters.carrier || "",
    sort: shipmentListFilters.sort || "updated_at",
  });

  useEffect(() => {
    setDraftFilters({
      status: shipmentListFilters.status || "",
      carrier: shipmentListFilters.carrier || "",
      sort: shipmentListFilters.sort || "updated_at",
    });
  }, [shipmentListFilters]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await listShipmentSupport({
        status: shipmentListFilters.status || undefined,
        carrier: shipmentListFilters.carrier || undefined,
        sort: shipmentListFilters.sort || "updated_at",
        page: Number(shipmentListFilters.page) || 1,
        size: Number(shipmentListFilters.size) || SHIPMENT_LIST_PAGE_SIZE,
      });
      setResult(data);
      setStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_SHIPMENT,
        actionLabel: "xem danh sách vận đơn",
        fallbackMessage: "Không tải được danh sách vận đơn.",
      });
    }
  }, [shipmentListFilters, showSessionExpired]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: SHIPMENT_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      carrier: "",
      sort: "updated_at",
      page: 1,
      size: SHIPMENT_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const currentPage = Number(shipmentListFilters.page) || 1;
  const totalPages = result?.total_pages || 1;
  const activeSort = shipmentListFilters.sort || "updated_at";

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...shipmentListFilters,
      page: nextPage,
      size: SHIPMENT_LIST_PAGE_SIZE,
    });
  };

  return (
    <div className="space-y-4">
      {!canReadShipment ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền SHIPMENT_SUPPORT_READ." />
      ) : null}

      <AccountCard>
        <form onSubmit={handleApplyFilters} className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Trạng thái</label>
            <select
              value={draftFilters.status}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, status: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {SHIPMENT_LIST_STATUS_OPTIONS.map((option) => (
                <option key={option.value || "all"} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Carrier</label>
            <select
              value={draftFilters.carrier}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, carrier: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {SHIPMENT_LIST_CARRIER_OPTIONS.map((option) => (
                <option key={option.value || "all"} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Sắp xếp theo</label>
            <select
              value={draftFilters.sort}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, sort: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {SHIPMENT_LIST_SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="flex items-end gap-2">
            <button
              type="submit"
              className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
            >
              Áp dụng
            </button>
            <button
              type="button"
              onClick={handleClearFilters}
              className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
            >
              Xóa lọc
            </button>
          </div>
        </form>
      </AccountCard>

      {status === "loading" ? <AccountSkeleton /> : null}
      {status === "forbidden" ? <SupportForbiddenState message={errorMessage} /> : null}
      {status === "unavailable" ? <SupportUnavailableState message={errorMessage} /> : null}

      {status === "error" ? (
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={fetchList}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thử lại
          </button>
        </AccountCard>
      ) : null}

      {status === "ready" ? (
        <AccountCard>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm text-on-surface-variant">
              {result.total_elements ?? 0} vận đơn · Sắp xếp: {sortColumnLabel(activeSort)} · Trang{" "}
              {result.page}/{totalPages}
            </p>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={currentPage <= 1}
                onClick={() => handlePageChange(currentPage - 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Trước
              </button>
              <button
                type="button"
                disabled={currentPage >= totalPages}
                onClick={() => handlePageChange(currentPage + 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Sau
              </button>
            </div>
          </div>

          {result.shipments?.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[960px] text-left text-sm">
                <thead>
                  <tr className="border-b border-outline-variant text-on-surface-variant">
                    <th className="py-2 pr-3 font-medium">Shipment ID</th>
                    <th className="py-2 pr-3 font-medium">Order ID</th>
                    <th className="py-2 pr-3 font-medium">Carrier</th>
                    <th className="py-2 pr-3 font-medium">Trạng thái</th>
                    <th className="py-2 pr-3 font-medium">Tracking</th>
                    <th className="py-2 pr-3 font-medium">Gửi hàng</th>
                    <th className="py-2 pr-3 font-medium">Tạo lúc</th>
                    <th className="py-2 font-medium">Cập nhật</th>
                  </tr>
                </thead>
                <tbody>
                  {result.shipments.map((row) => {
                    const isSelected = selectedShipmentId === row.shipment_id;
                    return (
                      <tr
                        key={row.shipment_id}
                        className={`cursor-pointer border-b border-outline-variant/60 align-top hover:bg-surface-container-low ${
                          isSelected ? "bg-primary/5" : ""
                        }`}
                        onClick={() => onShipmentSelect?.(row.shipment_id)}
                      >
                        <td className="py-3 pr-3 font-mono text-xs">{row.shipment_id}</td>
                        <td className="py-3 pr-3 font-mono text-xs">{row.order_id}</td>
                        <td className="py-3 pr-3">{row.carrier}</td>
                        <td className="py-3 pr-3">
                          <SupportStatusBadge status={row.internal_status} />
                        </td>
                        <td className="py-3 pr-3 font-mono text-xs">{row.tracking_number || "—"}</td>
                        <td className="py-3 pr-3">{formatDateTime(row.shipped_at)}</td>
                        <td className="py-3 pr-3">{formatDateTime(row.created_at)}</td>
                        <td className="py-3">{formatDateTime(row.updated_at)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm text-on-surface-variant">Không có vận đơn phù hợp bộ lọc.</p>
          )}
        </AccountCard>
      ) : null}
    </div>
  );
}

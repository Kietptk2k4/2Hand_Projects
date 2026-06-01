import { useEffect, useState } from "react";
import { CARRIERS, SHIPMENT_TYPES } from "../constants/sellerShipmentConstants";
import { formatShortOrderId } from "../utils/formatOrderDate";
import { useCreateShipment } from "../hooks/useCreateShipment";

export function CreateShipmentModal({
  open,
  onClose,
  onSuccess,
  prefillOrderId,
  prefillOrderItemIds,
}) {
  const [step, setStep] = useState(1);

  const {
    orderOptions,
    orderId,
    setOrderId,
    itemsForSelectedOrder,
    selectedItemIds,
    toggleItem,
    carrier,
    setCarrier,
    shipmentType,
    setShipmentType,
    weightGram,
    setWeightGram,
    trackingNumber,
    setTrackingNumber,
    showTrackingField,
    isLoadingOrders,
    loadError,
    isSubmitting,
    submitError,
    submit,
    resetForm,
  } = useCreateShipment({ open, prefillOrderId, prefillOrderItemIds });

  useEffect(() => {
    if (!open) {
      setStep(1);
      resetForm();
    }
  }, [open, resetForm]);

  if (!open) return null;

  const handleClose = () => {
    if (isSubmitting) return;
    onClose?.();
  };

  const handleSubmit = async () => {
    const result = await submit();
    if (result?.shipmentId) {
      onSuccess?.(result);
    }
  };

  const canGoStep2 = Boolean(orderId) && selectedItemIds.size > 0;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-inverse-surface/40 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="create-shipment-title"
    >
      <div className="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-lg">
        <div className="border-b border-outline-variant px-6 py-4">
          <h2 id="create-shipment-title" className="text-headline-sm font-semibold text-on-surface">
            Tạo vận đơn
          </h2>
          <p className="mt-1 text-body-sm text-on-surface-variant">
            Bước {step}/2 — {step === 1 ? "Chọn đơn & mục hàng" : "Cấu hình vận chuyển"}
          </p>
        </div>

        <div className="flex-1 overflow-y-auto px-6 py-4">
          {isLoadingOrders ? (
            <p className="text-body-sm text-on-surface-variant">Đang tải đơn chuẩn bị...</p>
          ) : null}

          {loadError ? <p className="text-sm text-error">{loadError}</p> : null}

          {!isLoadingOrders && !loadError && step === 1 ? (
            <div className="space-y-4">
              {orderOptions.length === 0 ? (
                <p className="text-body-sm text-on-surface-variant">
                  Không có mục đơn PROCESSING chưa có vận đơn.
                </p>
              ) : (
                <>
                  <label className="block">
                    <span className="text-label-sm font-medium text-on-surface">Đơn hàng</span>
                    <select
                      value={orderId}
                      onChange={(e) => setOrderId(e.target.value)}
                      className="mt-1 w-full rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-body-sm"
                    >
                      <option value="">— Chọn đơn —</option>
                      {orderOptions.map((id) => (
                        <option key={id} value={id}>
                          {formatShortOrderId(id)}
                        </option>
                      ))}
                    </select>
                  </label>

                  {itemsForSelectedOrder.length > 0 ? (
                    <ul className="space-y-2 rounded-lg border border-outline-variant/60 p-3">
                      {itemsForSelectedOrder.map((item) => (
                        <li key={item.orderItemId} className="flex items-start gap-2">
                          <input
                            type="checkbox"
                            checked={selectedItemIds.has(item.orderItemId)}
                            onChange={() => toggleItem(item.orderItemId)}
                            className="mt-1 h-4 w-4 rounded border-outline-variant text-primary"
                          />
                          <div>
                            <p className="text-body-sm text-on-surface">
                              {item.productNameSnapshot}
                            </p>
                            <p className="text-label-sm text-on-surface-variant">
                              SL: {item.quantity}
                            </p>
                          </div>
                        </li>
                      ))}
                    </ul>
                  ) : orderId ? (
                    <p className="text-body-sm text-on-surface-variant">
                      Đơn này không còn mục eligible.
                    </p>
                  ) : null}
                </>
              )}
            </div>
          ) : null}

          {step === 2 ? (
            <div className="space-y-4">
              <label className="block">
                <span className="text-label-sm font-medium text-on-surface">Hãng vận chuyển</span>
                <select
                  value={carrier}
                  onChange={(e) => setCarrier(e.target.value)}
                  className="mt-1 w-full rounded-lg border border-outline-variant px-3 py-2 text-body-sm"
                >
                  {CARRIERS.map((c) => (
                    <option key={c.value} value={c.value}>
                      {c.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="block">
                <span className="text-label-sm font-medium text-on-surface">Loại vận chuyển</span>
                <select
                  value={shipmentType}
                  onChange={(e) => setShipmentType(e.target.value)}
                  className="mt-1 w-full rounded-lg border border-outline-variant px-3 py-2 text-body-sm"
                >
                  {SHIPMENT_TYPES.map((t) => (
                    <option key={t.value} value={t.value}>
                      {t.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="block">
                <span className="text-label-sm font-medium text-on-surface">
                  Khối lượng (gram) — tùy chọn
                </span>
                <input
                  type="number"
                  min="1"
                  value={weightGram}
                  onChange={(e) => setWeightGram(e.target.value)}
                  className="mt-1 w-full rounded-lg border border-outline-variant px-3 py-2 text-body-sm"
                  placeholder="VD: 800"
                />
              </label>

              {showTrackingField ? (
                <label className="block">
                  <span className="text-label-sm font-medium text-on-surface">
                    Mã vận đơn — tùy chọn
                  </span>
                  <input
                    type="text"
                    value={trackingNumber}
                    onChange={(e) => setTrackingNumber(e.target.value)}
                    className="mt-1 w-full rounded-lg border border-outline-variant px-3 py-2 text-body-sm"
                    placeholder="Có thể cập nhật sau"
                  />
                </label>
              ) : null}
            </div>
          ) : null}

          {submitError ? <p className="mt-3 text-sm text-error">{submitError}</p> : null}
        </div>

        <div className="flex justify-end gap-3 border-t border-outline-variant px-6 py-4">
          <button
            type="button"
            onClick={handleClose}
            disabled={isSubmitting}
            className="rounded-lg px-4 py-2 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Hủy
          </button>
          {step === 2 ? (
            <button
              type="button"
              onClick={() => setStep(1)}
              disabled={isSubmitting}
              className="rounded-lg border border-outline-variant px-4 py-2 text-label-md disabled:opacity-50"
            >
              Quay lại
            </button>
          ) : null}
          {step === 1 ? (
            <button
              type="button"
              disabled={!canGoStep2}
              onClick={() => setStep(2)}
              className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary disabled:opacity-50"
            >
              Tiếp
            </button>
          ) : (
            <button
              type="button"
              onClick={handleSubmit}
              disabled={isSubmitting}
              className="rounded-lg bg-primary px-4 py-2 text-label-md font-medium text-on-primary disabled:opacity-50"
            >
              {isSubmitting ? "Đang tạo..." : "Tạo vận đơn"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

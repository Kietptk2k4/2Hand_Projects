import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { ReviewAuthorLink } from "../components/ReviewAuthorLink";
import { CommerceShell } from "../components/CommerceShell";
import { OrderDetailShippingAddress } from "../components/OrderDetailShippingAddress";
import { SellerShipmentUpdateConfirmDialog } from "../components/SellerShipmentUpdateConfirmDialog";
import {
  MANUAL_NEXT_ACTIONS,
  SHIPMENT_STATUS_BADGE_CLASS,
  SHIPMENT_STATUS_LABELS,
  canCancelSellerShipment,
} from "../constants/sellerShipmentConstants";
import { useSellerShipmentDetail } from "../hooks/useSellerShipmentDetail";
import { formatShortOrderId } from "../utils/formatOrderDate";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { APP_ROUTES } from "../../../shared/constants/routes";

const TERMINAL = ["DELIVERED", "FAILED", "CANCELLED", "RETURNED"];

export function CommerceSellerShipmentDetailPage() {
  const { shipmentId } = useParams();
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const [trackingDraft, setTrackingDraft] = useState("");
  const [pendingAction, setPendingAction] = useState(null);
  const [pendingTrackingSave, setPendingTrackingSave] = useState(false);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);

  const {
    detail,
    isLoading,
    isUpdating,
    isCancelling,
    errorMessage,
    updateError,
    cancelError,
    isNotFound,
    retry,
    patchShipment,
    cancelShipment,
    clearUpdateError,
    clearCancelError,
  } = useSellerShipmentDetail(shipmentId);

  const isGhn = detail?.carrier === "GHN";
  const isManual = detail?.carrier === "MANUAL" || detail?.carrier === "SELF_DELIVERY";
  const canEdit = isManual && detail && !TERMINAL.includes(detail.status);
  const canCancel = detail && canCancelSellerShipment(detail);

  const nextAction = detail ? MANUAL_NEXT_ACTIONS[detail.status] : null;
  const nextActions = Array.isArray(nextAction) ? nextAction : nextAction ? [nextAction] : [];

  const handleConfirmCancel = useCallback(async () => {
    const result = await cancelShipment();
    if (result) {
      setShowCancelConfirm(false);
      setToastMessage("Đã hủy vận đơn. Các mục đơn đã được trả về trạng thái xử lý.");
    }
  }, [cancelShipment]);

  const handleConfirmStatus = useCallback(async () => {
    if (!pendingAction) return;
    const result = await patchShipment({ status: pendingAction.status });
    if (result) {
      setPendingAction(null);
      setToastMessage("Cập nhật trạng thái thành công.");
    }
  }, [patchShipment, pendingAction]);

  const handleSaveTracking = useCallback(async () => {
    setPendingTrackingSave(true);
    const result = await patchShipment({ trackingNumber: trackingDraft });
    setPendingTrackingSave(false);
    if (result) {
      setToastMessage("Đã lưu mã vận đơn.");
    }
  }, [patchShipment, trackingDraft]);

  useEffect(() => {
    if (detail?.trackingNumber != null) {
      setTrackingDraft(detail.trackingNumber);
    }
  }, [detail?.shipmentId, detail?.trackingNumber]);

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[960px]">
        <div className="mb-6">
          <button
            type="button"
            onClick={() => navigate(APP_ROUTES.commerceSellerShipments)}
            className="mb-3 inline-flex items-center gap-1 text-label-md text-primary hover:underline"
          >
            <span className="material-symbols-outlined text-[18px]">arrow_back</span>
            Quản lý vận chuyển
          </button>
          <h1 className="text-headline-md font-semibold text-on-surface">Chi tiết vận đơn</h1>
          {detail ? (
            <p className="mt-1 font-mono text-body-sm text-on-surface-variant">{detail.shipmentId}</p>
          ) : null}
        </div>

        {isLoading ? (
          <div className="h-64 animate-pulse rounded-xl bg-surface-container-low" />
        ) : null}

        {isNotFound ? (
          <div className="rounded-xl border border-outline-variant p-8 text-center">
            <p className="text-body-md text-on-surface-variant">Không tìm thấy vận đơn.</p>
            <Link
              to={APP_ROUTES.commerceSellerShipments}
              className="mt-4 inline-block text-primary hover:underline"
            >
              Quay lại danh sách
            </Link>
          </div>
        ) : null}

        {!isLoading && errorMessage && !isNotFound ? (
          <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
            <p className="text-body-md text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-lg bg-primary px-4 py-2 text-label-md text-on-primary"
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {!isLoading && detail ? (
          <>
            {isGhn ? (
              <div className="mb-4 rounded-lg border border-sky-200 bg-sky-50 px-4 py-3 text-body-sm text-sky-900">
                Trạng thái cập nhật tự động qua GHN — không chỉnh tay trên hệ thống.
              </div>
            ) : null}

            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6">
              <div className="flex flex-wrap items-center gap-2">
                <span
                  className={[
                    "rounded-full px-3 py-1 text-label-sm font-medium",
                    SHIPMENT_STATUS_BADGE_CLASS[detail.status] || "bg-surface-container-high",
                  ].join(" ")}
                >
                  {SHIPMENT_STATUS_LABELS[detail.status] || detail.status}
                </span>
                <span className="text-label-sm text-on-surface-variant">
                  Đơn {formatShortOrderId(detail.orderId)}
                </span>
              </div>

              <div className="mt-4">
                <p className="mb-2 text-label-sm text-on-surface-variant">Người mua</p>
                <ReviewAuthorLink
                  buyerId={detail.buyerId}
                  displayName={detail.buyerDisplayName}
                  avatarUrl={detail.buyerAvatarUrl}
                />
              </div>

              <dl className="mt-6 grid gap-4 sm:grid-cols-2">
                <div>
                  <dt className="text-label-sm text-on-surface-variant">Hãng VC</dt>
                  <dd className="text-body-md font-medium">{detail.carrier}</dd>
                </div>
                <div>
                  <dt className="text-label-sm text-on-surface-variant">Loại</dt>
                  <dd className="text-body-md font-medium">{detail.shipmentType}</dd>
                </div>
                <div>
                  <dt className="text-label-sm text-on-surface-variant">Mã vận đơn</dt>
                  <dd className="font-mono text-body-md">{detail.trackingNumber || "—"}</dd>
                </div>
                {detail.ghnOrderCode ? (
                  <div>
                    <dt className="text-label-sm text-on-surface-variant">Mã GHN</dt>
                    <dd className="font-mono text-body-md">{detail.ghnOrderCode}</dd>
                  </div>
                ) : null}
                <div>
                  <dt className="text-label-sm text-on-surface-variant">Phí ship</dt>
                  <dd className="text-body-md">{formatVndPrice(detail.shippingFee)}</dd>
                </div>
                <div>
                  <dt className="text-label-sm text-on-surface-variant">COD</dt>
                  <dd className="text-body-md">{formatVndPrice(detail.codAmount)}</dd>
                </div>
                <div>
                  <dt className="text-label-sm text-on-surface-variant">Khối lượng</dt>
                  <dd className="text-body-md">{detail.weightGram ? `${detail.weightGram} g` : "—"}</dd>
                </div>
                <div>
                  <dt className="text-label-sm text-on-surface-variant">ETA</dt>
                  <dd className="text-body-md">{detail.estimatedDeliveryDate || "—"}</dd>
                </div>
              </dl>
            </div>

            {detail.shippingAddress ? (
              <section className="mt-6">
                <h2 className="mb-3 text-headline-sm font-semibold text-on-surface">
                  Địa chỉ giao hàng
                </h2>
                <OrderDetailShippingAddress address={detail.shippingAddress} />
              </section>
            ) : null}

            <section className="mt-6">
              <h2 className="mb-3 text-headline-sm font-semibold text-on-surface">
                Mục trong vận đơn
              </h2>
              <ul className="divide-y divide-outline-variant/60 rounded-xl border border-outline-variant">
                {detail.orderItems.map((row) => (
                  <li
                    key={row.orderItemId}
                    className="flex items-center justify-between px-4 py-3 text-body-sm"
                  >
                    <span className="text-on-surface">{row.productNameSnapshot}</span>
                    <span className="text-on-surface-variant">
                      ×{row.quantity} · {row.status}
                    </span>
                  </li>
                ))}
              </ul>
            </section>

            {canCancel ? (
              <section className="mt-6 rounded-xl border border-error/30 bg-error-container/20 p-6">
                <h2 className="text-headline-sm font-semibold text-on-surface">Hủy vận đơn</h2>
                <p className="mt-2 text-body-sm text-on-surface-variant">
                  {isGhn
                    ? "Hủy đơn GHN trên hệ thống và gọi API hủy bên GHN. Người mua sẽ được thông báo."
                    : "Hủy vận đơn thủ công. Các mục đơn sẽ quay lại trạng thái xử lý để tạo vận đơn mới."}
                </p>
                {cancelError ? <p className="mt-2 text-sm text-error">{cancelError}</p> : null}
                <button
                  type="button"
                  disabled={isCancelling || isUpdating}
                  onClick={() => {
                    clearCancelError();
                    setShowCancelConfirm(true);
                  }}
                  className="mt-4 rounded-lg border border-error px-4 py-2 text-label-md font-medium text-error disabled:opacity-50"
                >
                  Hủy vận đơn
                </button>
              </section>
            ) : null}

            {canEdit ? (
              <section className="mt-6 rounded-xl border border-secondary/30 bg-surface-container-low p-6">
                <h2 className="text-headline-sm font-semibold text-on-surface">Cập nhật vận đơn</h2>

                <label className="mt-4 block">
                  <span className="text-label-sm font-medium text-on-surface">Mã vận đơn</span>
                  <input
                    type="text"
                    value={trackingDraft}
                    onChange={(e) => setTrackingDraft(e.target.value)}
                    className="mt-1 w-full max-w-md rounded-lg border border-outline-variant px-3 py-2 text-body-sm"
                  />
                </label>
                <button
                  type="button"
                  disabled={isUpdating || pendingTrackingSave}
                  onClick={handleSaveTracking}
                  className="mt-2 rounded-lg border border-outline-variant px-4 py-2 text-label-md disabled:opacity-50"
                >
                  Lưu mã vận đơn
                </button>

                {updateError ? <p className="mt-2 text-sm text-error">{updateError}</p> : null}

                <div className="mt-4 flex flex-wrap gap-2">
                  {nextActions.map((action) => (
                    <button
                      key={action.status}
                      type="button"
                      disabled={isUpdating}
                      onClick={() => {
                        clearUpdateError();
                        setPendingAction(action);
                      }}
                      className={[
                        "rounded-lg px-4 py-2 text-label-md font-medium disabled:opacity-50",
                        action.status === "FAILED"
                          ? "border border-error text-error"
                          : "bg-primary text-on-primary",
                      ].join(" ")}
                    >
                      {action.label}
                    </button>
                  ))}
                </div>
              </section>
            ) : null}
          </>
        ) : null}
      </div>

      <SellerShipmentUpdateConfirmDialog
        open={showCancelConfirm}
        title="Xác nhận hủy vận đơn"
        description="Bạn có chắc muốn hủy vận đơn này? Hành động này không thể hoàn tác."
        confirmLabel="Hủy vận đơn"
        isProcessing={isCancelling}
        errorMessage={cancelError}
        onCancel={() => {
          if (!isCancelling) setShowCancelConfirm(false);
        }}
        onConfirm={handleConfirmCancel}
      />

      <SellerShipmentUpdateConfirmDialog
        open={Boolean(pendingAction)}
        title="Xác nhận cập nhật"
        description={
          pendingAction ? `Chuyển sang "${pendingAction.label}"?` : ""
        }
        isProcessing={isUpdating}
        errorMessage={updateError}
        onCancel={() => {
          if (!isUpdating) setPendingAction(null);
        }}
        onConfirm={handleConfirmStatus}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}

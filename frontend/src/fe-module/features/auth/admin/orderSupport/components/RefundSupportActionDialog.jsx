import { useState } from "react";
import { AdminFilterButton } from "../../components/ui";
import { SystemOperationsModalShell } from "../../systemOperations/components/ui/SystemOperationsModalShell.jsx";

export function RefundSupportActionDialog({
  open,
  action,
  pending,
  itemLabel,
  onClose,
  onConfirm,
}) {
  const [adminNote, setAdminNote] = useState("");

  const isConfirm = action === "confirm";
  const title = isConfirm ? "Xác nhận đã hoàn tiền" : "Từ chối yêu cầu hoàn tiền";
  const body = isConfirm
    ? "Xác nhận bạn đã hoàn tiền cho khách trên VNPay. Hành động này sẽ hủy đơn và giải phóng tồn kho."
    : "Từ chối yêu cầu hoàn tiền. Đơn hàng sẽ tiếp tục ở trạng thái đang xử lý.";

  const handleClose = () => {
    setAdminNote("");
    onClose?.();
  };

  const handleConfirm = () => {
    onConfirm?.(adminNote.trim());
    setAdminNote("");
  };

  return (
    <SystemOperationsModalShell
      open={open}
      title={title}
      onClose={handleClose}
      maxWidthClass="max-w-md"
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={pending} onClick={handleClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={pending}
            className={
              isConfirm
                ? ""
                : "border-admin-danger/30 bg-admin-danger text-white hover:bg-admin-danger/90"
            }
            onClick={handleConfirm}
          >
            {isConfirm ? "Xác nhận đã hoàn tiền" : "Từ chối"}
          </AdminFilterButton>
        </>
      }
    >
      <p className="text-sm text-admin-text-secondary">{body}</p>
      {itemLabel ? <p className="mt-3 text-sm font-medium text-admin-text">{itemLabel}</p> : null}
      {!isConfirm ? (
        <label className="mt-4 block">
          <span className="text-sm font-medium text-admin-text">Ghi chú (tuỳ chọn)</span>
          <textarea
            value={adminNote}
            onChange={(event) => setAdminNote(event.target.value)}
            rows={3}
            className="mt-2 w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text"
            placeholder="Lý do từ chối..."
          />
        </label>
      ) : null}
    </SystemOperationsModalShell>
  );
}

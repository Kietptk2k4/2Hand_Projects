import { useEffect, useState } from "react";
import { AdminFilterButton } from "../../components/ui";
import { SystemOperationsModalShell } from "../../systemOperations/components/ui/SystemOperationsModalShell.jsx";
import { formatVndPrice } from "../../../../social/utils/formatPrice";

const ACTION_COPY = {
  approve: {
    title: "Duyệt yêu cầu rút tiền",
    subtitle: "Xác nhận duyệt trước khi chuyển sang bước ghi nhận chuyển khoản.",
    confirmLabel: "Duyệt",
    field: null,
  },
  reject: {
    title: "Từ chối yêu cầu",
    subtitle: "Ghi rõ lý do để seller và audit có thể tra cứu.",
    confirmLabel: "Từ chối",
    field: "note",
    placeholder: "Lý do từ chối (bắt buộc)",
  },
  "mark-paid": {
    title: "Ghi nhận chuyển khoản",
    subtitle: "Nhập mã tham chiếu giao dịch ngân hàng.",
    confirmLabel: "Ghi nhận",
    field: "ref",
    placeholder: "Mã tham chiếu chuyển khoản",
  },
};

export function PayoutActionDialog({ request, pending, onConfirm, onClose }) {
  const [value, setValue] = useState("");
  const action = request?.action;
  const copy = ACTION_COPY[action];
  const item = request?.item;

  useEffect(() => {
    if (request) setValue("");
  }, [request]);

  if (!request || !copy) return null;

  const requiresInput = copy.field === "note" || copy.field === "ref";
  const trimmed = value.trim();
  const canSubmit = !requiresInput || trimmed.length > 0;

  const handleConfirm = () => {
    if (!canSubmit || pending) return;
    if (copy.field === "note") onConfirm?.({ adminNote: trimmed });
    else if (copy.field === "ref") onConfirm?.({ bankTransferRef: trimmed });
    else onConfirm?.({});
  };

  return (
    <SystemOperationsModalShell
      open={Boolean(request)}
      title={copy.title}
      subtitle={copy.subtitle}
      onClose={onClose}
      maxWidthClass="max-w-md"
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={pending} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={pending || !canSubmit}
            className={
              action === "reject"
                ? "border-admin-danger/30 bg-admin-danger text-white hover:bg-admin-danger/90"
                : ""
            }
            onClick={handleConfirm}
          >
            {copy.confirmLabel}
          </AdminFilterButton>
        </>
      }
    >
      {item ? (
        <div className="mb-4 rounded-lg border border-admin-border bg-admin-surface-muted px-3 py-3 text-sm">
          <p className="font-medium tabular-nums text-admin-text">{formatVndPrice(item.amount)}</p>
          <p className="mt-1 text-admin-text-secondary">
            {item.bankName} · {item.bankAccountName}
          </p>
          <p className="mt-0.5 font-mono text-xs text-admin-text-muted">{item.bankAccountNumber}</p>
        </div>
      ) : null}

      {copy.field === "note" ? (
        <label className="block">
          <span className="mb-1.5 block text-sm font-medium text-admin-text">Lý do từ chối</span>
          <textarea
            rows={4}
            value={value}
            onChange={(event) => setValue(event.target.value)}
            placeholder={copy.placeholder}
            className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text placeholder:text-admin-text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          />
        </label>
      ) : null}

      {copy.field === "ref" ? (
        <label className="block">
          <span className="mb-1.5 block text-sm font-medium text-admin-text">Mã tham chiếu</span>
          <input
            type="text"
            value={value}
            onChange={(event) => setValue(event.target.value)}
            placeholder={copy.placeholder}
            className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text placeholder:text-admin-text-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          />
        </label>
      ) : null}

      {action === "approve" ? (
        <p className="text-sm text-admin-text-secondary">
          Sau khi duyệt, yêu cầu chuyển sang trạng thái chờ chuyển khoản.
        </p>
      ) : null}
    </SystemOperationsModalShell>
  );
}

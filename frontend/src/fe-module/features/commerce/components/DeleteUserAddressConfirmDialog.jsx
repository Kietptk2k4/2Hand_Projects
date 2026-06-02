import { formatAddressHeader } from "../utils/formatAddressLine";

export function DeleteUserAddressConfirmDialog({
  open,
  address,
  isSubmitting,
  onCancel,
  onConfirm,
}) {
  if (!open || !address) return null;

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-on-surface/40 p-4 backdrop-blur-sm"
      role="presentation"
      onClick={onCancel}
    >
      <div
        className="w-full max-w-md rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-lg"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="delete-address-title"
        onClick={(event) => event.stopPropagation()}
      >
        <h2 id="delete-address-title" className="text-headline-sm font-semibold text-on-surface">
          Xóa địa chỉ?
        </h2>
        <p className="mt-2 text-body-sm text-on-surface-variant">
          Bạn có chắc muốn xóa địa chỉ của{" "}
          <span className="font-medium text-on-surface">{formatAddressHeader(address)}</span>?
        </p>
        {address.isDefault ? (
          <p className="mt-3 rounded-lg border border-primary/30 bg-primary/5 p-3 text-body-sm text-on-surface">
            Đây là địa chỉ mặc định. Hệ thống sẽ tự chọn địa chỉ khác làm mặc định nếu còn địa chỉ
            trong sổ.
          </p>
        ) : null}
        <p className="mt-2 text-body-sm text-on-surface-variant">
          Thay đổi này không ảnh hưởng địa chỉ đã lưu trong đơn hàng đã đặt.
        </p>

        <div className="mt-6 flex gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isSubmitting}
            className="flex-1 rounded-lg border border-outline-variant py-2.5 text-sm font-medium text-on-surface hover:bg-surface-container-low"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isSubmitting}
            className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-error py-2.5 text-sm font-medium text-on-error hover:opacity-90 disabled:opacity-50"
          >
            {isSubmitting ? (
              <span className="h-5 w-5 animate-spin rounded-full border-2 border-on-error border-t-transparent" />
            ) : (
              "Xóa"
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

import { GENERIC_CANCEL } from "../constants/systemOperationsUiStrings.js";

const COPY = {
  publish: {
    title: "Publish thông báo",
    body: "Thông báo sẽ được gửi tới người dùng theo cấu hình publish.",
    confirm: "Publish",
  },
  pin: { title: "Pin thông báo", body: "Thông báo sẽ được ghim trên banner hệ thống.", confirm: "Pin" },
  unpin: { title: "Bỏ pin", body: "Thông báo sẽ không còn được ghim.", confirm: "Bỏ pin" },
  cancel: { title: "Hủy thông báo", body: "Hành động này không thể hoàn tác.", confirm: "Hủy thông báo" },
};

export function AnnouncementActionConfirmDialog({ request, pending, onConfirm, onClose }) {
  if (!request) return null;
  const copy = COPY[request.type] || COPY.publish;

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center p-4">
      <button type="button" aria-label="Đóng" className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative z-10 w-full max-w-md rounded-xl border border-outline-variant bg-surface p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-on-surface">{copy.title}</h3>
        <p className="mt-2 text-sm text-on-surface-variant">{copy.body}</p>
        <p className="mt-3 text-sm font-medium text-on-surface">{request.item?.title}</p>
        <div className="mt-6 flex justify-end gap-2">
          <button type="button" onClick={onClose} className="rounded-lg border border-outline-variant px-4 py-2 text-sm">
            {GENERIC_CANCEL}
          </button>
          <button
            type="button"
            disabled={pending}
            onClick={() => onConfirm?.(request)}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
          >
            {copy.confirm}
          </button>
        </div>
      </div>
    </div>
  );
}
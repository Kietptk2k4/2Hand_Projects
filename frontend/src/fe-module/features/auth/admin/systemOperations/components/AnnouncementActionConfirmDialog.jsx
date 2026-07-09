import { AnnouncementActionConfirmDialogView } from "./AnnouncementActionConfirmDialogView.jsx";

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
  const copy = request ? COPY[request.type] || COPY.publish : null;
  const confirmDanger = request?.type === "cancel";

  return (
    <AnnouncementActionConfirmDialogView
      open={Boolean(request)}
      title={copy?.title}
      body={copy?.body}
      confirmLabel={copy?.confirm}
      confirmDanger={confirmDanger}
      itemTitle={request?.item?.title}
      pending={pending}
      onClose={onClose}
      onConfirm={() => onConfirm?.(request)}
    />
  );
}

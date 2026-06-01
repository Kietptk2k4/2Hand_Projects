export function SellerShipmentListEmptyState({ variant }) {
  const message =
    variant === "search"
      ? "Không tìm thấy vận đơn phù hợp."
      : variant === "filter"
        ? "Không có vận đơn ở trạng thái này."
        : "Chưa có vận đơn nào. Tạo vận đơn từ đơn đang chuẩn bị.";

  return (
    <div className="rounded-xl border border-dashed border-outline-variant bg-surface-container-low p-10 text-center">
      <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
        local_shipping
      </span>
      <p className="text-body-md text-on-surface-variant">{message}</p>
    </div>
  );
}

export function SellerOrderListEmptyState({ variant = "none" }) {
  const title =
    variant === "search"
      ? "Không có kết quả phù hợp"
      : variant === "filter"
        ? "Không có đơn ở trạng thái này"
        : "Chưa có đơn hàng nào";

  const description =
    variant === "search"
      ? "Thử đổi từ khóa tìm kiếm trên trang hiện tại."
      : variant === "filter"
        ? "Thử chọn tab hoặc bộ lọc vận chuyển khác."
        : "Khi có đơn mua từ shop của bạn, các mục sẽ hiển thị tại đây.";

  return (
    <div className="flex flex-col items-center justify-center rounded-xl border border-outline-variant bg-surface-container-lowest px-6 py-16 text-center">
      <span
        className="material-symbols-outlined mb-4 text-[48px] text-on-surface-variant"
        aria-hidden="true"
      >
        shopping_bag
      </span>
      <h2 className="text-headline-sm font-semibold text-on-surface">{title}</h2>
      <p className="mt-2 max-w-md text-body-sm text-on-surface-variant">{description}</p>
    </div>
  );
}

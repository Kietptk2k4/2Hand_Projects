import { AdminPagination } from "../../auth/admin/components/ui";

export function AdminProductRemovalPagination({
  page,
  totalPages,
  rangeStart,
  rangeEnd,
  totalItems,
  onPrev,
  onNext,
  onGoToPage,
  disabled,
}) {
  if (totalItems === 0) return null;

  return (
    <AdminPagination
      currentPage={page}
      totalPages={totalPages}
      summary={`Hiển thị ${rangeStart}–${rangeEnd} của ${totalItems} mục`}
      onPrevious={onPrev}
      onNext={onNext}
      onGoToPage={onGoToPage}
      disabled={disabled}
      showPageNumbers
      className="border-t border-admin-border-subtle pt-4"
    />
  );
}

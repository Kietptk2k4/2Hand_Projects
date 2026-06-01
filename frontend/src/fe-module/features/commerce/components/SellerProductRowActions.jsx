export function SellerProductRowActions({ product, disabled, onAction, onComingSoon }) {
  const status = product.status;

  const actions = [];

  if (status === "DRAFT") {
    actions.push({ key: "publish", label: "Đăng bán" });
    actions.push({ key: "archive", label: "Lưu trữ" });
  } else if (status === "ACTIVE" || status === "OUT_OF_STOCK") {
    actions.push({ key: "pause", label: "Tạm dừng" });
    actions.push({ key: "archive", label: "Lưu trữ" });
  } else if (status === "PAUSED") {
    actions.push({ key: "publish", label: "Mở bán lại" });
    actions.push({ key: "archive", label: "Lưu trữ" });
  }

  if (actions.length === 0) {
    return <span className="text-body-sm text-on-surface-variant">—</span>;
  }

  return (
    <div className="flex flex-wrap justify-end gap-1">
      <button
        type="button"
        disabled={disabled}
        onClick={() => onComingSoon?.()}
        className="rounded p-1 text-on-surface-variant hover:bg-surface-container-low"
        title="Chỉnh sửa (sắp có)"
        aria-label="Chỉnh sửa"
      >
        <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
          edit
        </span>
      </button>
      {actions.map((action) => (
        <button
          key={action.key}
          type="button"
          disabled={disabled}
          onClick={() => onAction(action.key, product)}
          className="rounded-lg px-2 py-1 text-label-sm font-medium text-primary hover:bg-surface-container-low disabled:opacity-50"
        >
          {action.label}
        </button>
      ))}
    </div>
  );
}

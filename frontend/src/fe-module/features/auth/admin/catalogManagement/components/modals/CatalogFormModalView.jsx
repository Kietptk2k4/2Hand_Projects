import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../../components/ui";

export function CatalogModalShell({
  open,
  title,
  onClose,
  children,
  footer,
}) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex min-h-dvh items-end justify-center bg-admin-text/40 p-0 backdrop-blur-sm sm:items-center sm:p-4"
      role="dialog"
      aria-modal="true"
      onClick={(event) => {
        if (event.target === event.currentTarget) onClose?.();
      }}
    >
      <div className="flex max-h-[90dvh] w-full max-w-lg flex-col overflow-hidden rounded-t-xl border border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:mx-4 sm:rounded-xl">
        <div className="border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <h2 className="text-lg font-semibold text-admin-text">{title}</h2>
        </div>
        <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">{children}</div>
        {footer ? (
          <div className="flex flex-col-reverse gap-2 border-t border-admin-border bg-admin-surface-muted px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
            {footer}
          </div>
        ) : null}
      </div>
    </div>
  );
}

export function CatalogFormModalView({
  open,
  title,
  name,
  slug,
  parentId,
  showParentField,
  parentOptions,
  error,
  isSubmitting,
  submitLabel,
  onNameChange,
  onSlugChange,
  onParentIdChange,
  onClose,
  onSubmit,
}) {
  return (
    <CatalogModalShell
      open={open}
      title={title}
      onClose={onClose}
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="submit"
            form="catalog-form"
            variant="primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Đang lưu…" : submitLabel}
          </AdminFilterButton>
        </>
      }
    >
      <form id="catalog-form" className="space-y-4" onSubmit={onSubmit}>
        <AdminFilterField label="Tên" htmlFor="catalog-name">
          <AdminFilterInput
            id="catalog-name"
            value={name}
            onChange={(e) => onNameChange(e.target.value)}
            required
          />
        </AdminFilterField>
        <AdminFilterField label="Slug (tùy chọn)" htmlFor="catalog-slug">
          <AdminFilterInput
            id="catalog-slug"
            value={slug}
            onChange={(e) => onSlugChange(e.target.value)}
            placeholder="tự động tạo nếu để trống"
          />
        </AdminFilterField>
        {showParentField ? (
          <AdminFilterField label="Danh mục cha" htmlFor="catalog-parent">
            <AdminFilterSelect
              id="catalog-parent"
              value={parentId}
              onChange={(e) => onParentIdChange(e.target.value)}
            >
              <option value="">— Không có (gốc) —</option>
              {parentOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {"—".repeat(option.level)} {option.name}
                </option>
              ))}
            </AdminFilterSelect>
          </AdminFilterField>
        ) : null}
        {error ? <p className="text-sm text-admin-danger">{error}</p> : null}
      </form>
    </CatalogModalShell>
  );
}

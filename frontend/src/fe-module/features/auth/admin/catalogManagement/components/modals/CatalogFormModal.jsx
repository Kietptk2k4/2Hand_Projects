import { useEffect, useState } from "react";
import { AccountCard, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";

export function CatalogFormModal({
  open,
  title,
  initialValues,
  parentOptions = [],
  showParentField = false,
  submitLabel,
  onClose,
  onSubmit,
}) {
  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [parentId, setParentId] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!open) return;
    setName(initialValues?.name || "");
    setSlug(initialValues?.slug || "");
    setParentId(initialValues?.parentId || "");
    setError("");
    setIsSubmitting(false);
  }, [open, initialValues]);

  if (!open) return null;

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!name.trim()) {
      setError("Vui lòng nhập tên.");
      return;
    }
    setIsSubmitting(true);
    setError("");
    try {
      const payload = { name: name.trim() };
      if (slug.trim()) payload.slug = slug.trim();
      if (showParentField) {
        payload.parent_id = parentId || null;
      }
      await onSubmit?.(payload);
      onClose?.();
    } catch (submitError) {
      setError(submitError?.message || "Không thể lưu thay đổi.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <AccountCard className="w-full max-w-lg p-6">
        <TabPanelHeader title={title} />
        <form className="mt-4 space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="mb-1 block text-sm font-medium text-on-surface" htmlFor="catalog-name">
              Tên
            </label>
            <input
              id="catalog-name"
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-on-surface" htmlFor="catalog-slug">
              Slug (tùy chọn)
            </label>
            <input
              id="catalog-slug"
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
              value={slug}
              onChange={(e) => setSlug(e.target.value)}
              placeholder="tu-dong-tao-neu-de-trong"
            />
          </div>
          {showParentField ? (
            <div>
              <label className="mb-1 block text-sm font-medium text-on-surface" htmlFor="catalog-parent">
                Danh mục cha
              </label>
              <select
                id="catalog-parent"
                className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
                value={parentId}
                onChange={(e) => setParentId(e.target.value)}
              >
                <option value="">— Không có (gốc) —</option>
                {parentOptions.map((option) => (
                  <option key={option.id} value={option.id}>
                    {"—".repeat(option.level)} {option.name}
                  </option>
                ))}
              </select>
            </div>
          ) : null}
          {error ? <p className="text-sm text-error">{error}</p> : null}
          <div className="flex justify-end gap-2">
            <button
              type="button"
              className="rounded-lg border border-outline-variant px-4 py-2 text-sm"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Hủy
            </button>
            <button
              type="submit"
              className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary disabled:opacity-50"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Đang lưu..." : submitLabel}
            </button>
          </div>
        </form>
      </AccountCard>
    </div>
  );
}

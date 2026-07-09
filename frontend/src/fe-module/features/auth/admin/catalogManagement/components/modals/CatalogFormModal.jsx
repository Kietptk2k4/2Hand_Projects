import { useEffect, useState } from "react";
import { CatalogFormModalView } from "./CatalogFormModalView.jsx";

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
    <CatalogFormModalView
      open={open}
      title={title}
      name={name}
      slug={slug}
      parentId={parentId}
      showParentField={showParentField}
      parentOptions={parentOptions}
      error={error}
      isSubmitting={isSubmitting}
      submitLabel={submitLabel}
      onNameChange={setName}
      onSlugChange={setSlug}
      onParentIdChange={setParentId}
      onClose={onClose}
      onSubmit={handleSubmit}
    />
  );
}

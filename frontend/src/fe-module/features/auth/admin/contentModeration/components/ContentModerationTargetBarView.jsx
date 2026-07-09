import { AdminFilterButton, AdminFilterInput, AdminSurfaceCard } from "../../components/ui";

export function ContentModerationTargetBarView({
  label,
  placeholder,
  hint,
  idLabel,
  inputValue,
  validationError,
  currentId,
  onInputChange,
  onSubmit,
  onClear,
}) {
  return (
    <AdminSurfaceCard padding="lg" className="mb-6 max-w-full min-w-0">
      <form onSubmit={onSubmit}>
        <label
          htmlFor="content-moderation-target-input"
          className="mb-1.5 block text-xs font-medium text-admin-text-secondary"
        >
          {label}
        </label>
        <div className="flex flex-col gap-2 sm:flex-row">
          <AdminFilterInput
            id="content-moderation-target-input"
            type="text"
            value={inputValue}
            onChange={onInputChange}
            placeholder={placeholder}
            className="font-mono"
            autoComplete="off"
            spellCheck={false}
          />
          <div className="flex shrink-0 flex-col gap-2 sm:flex-row">
            <AdminFilterButton type="submit" variant="primary">
              Chọn
            </AdminFilterButton>
            {currentId ? (
              <AdminFilterButton type="button" variant="secondary" onClick={onClear}>
                Xóa
              </AdminFilterButton>
            ) : null}
          </div>
        </div>
        {validationError ? (
          <p className="mt-2 text-xs text-admin-danger">{validationError}</p>
        ) : (
          <p className="mt-2 text-xs text-admin-text-muted">{hint}</p>
        )}
        {currentId ? (
          <p className="mt-2 break-all font-mono text-xs text-admin-text-secondary">
            {idLabel}: {currentId}
          </p>
        ) : null}
      </form>
    </AdminSurfaceCard>
  );
}

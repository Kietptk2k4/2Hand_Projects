import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminSurfaceCard,
} from "../../components/ui";

export function AdminSupportTargetBarView({
  label,
  placeholder,
  hint,
  paramKey,
  inputValue,
  validationError,
  currentId,
  onInputChange,
  onSubmit,
  onClear,
}) {
  return (
    <AdminSurfaceCard padding="lg" className="mb-4">
      <form onSubmit={onSubmit}>
        <AdminFilterField label={label} htmlFor="support-target-input">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-start">
            <AdminFilterInput
              id="support-target-input"
              type="text"
              value={inputValue}
              onChange={(event) => onInputChange(event.target.value)}
              placeholder={placeholder}
              autoComplete="off"
              className="text-base sm:text-sm"
            />
            <div className="flex w-full shrink-0 flex-col gap-2 sm:w-auto sm:flex-row">
              <AdminFilterButton type="submit" variant="primary" className="w-full sm:w-auto">
                Tra cứu
              </AdminFilterButton>
              {currentId ? (
                <AdminFilterButton
                  type="button"
                  variant="secondary"
                  className="w-full sm:w-auto"
                  onClick={onClear}
                >
                  Xóa
                </AdminFilterButton>
              ) : null}
            </div>
          </div>
        </AdminFilterField>

        {validationError ? (
          <p className="mt-2 text-xs text-admin-danger">{validationError}</p>
        ) : (
          <p className="mt-2 text-xs text-admin-text-muted">{hint}</p>
        )}

        {currentId ? (
          <p className="mt-2 break-all text-xs text-admin-text-muted">
            {paramKey}: {currentId}
          </p>
        ) : null}
      </form>
    </AdminSurfaceCard>
  );
}

export function RbacSelectedUserSummary({
  selectedUserId,
  selectedUser,
  fieldError,
  emptyHint = "Chọn một người dùng từ danh sách phía trên.",
}) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-wide text-admin-text-muted">
        Người dùng đã chọn
      </p>
      {selectedUserId ? (
        <div className="mt-2 text-sm text-admin-text">
          <p className="font-medium break-all">{selectedUser?.email || selectedUserId}</p>
          {selectedUser?.display_name ? (
            <p className="text-admin-text-secondary">{selectedUser.display_name}</p>
          ) : null}
        </div>
      ) : (
        <p className="mt-2 text-sm text-admin-text-muted">{emptyHint}</p>
      )}
      {fieldError ? <p className="mt-1 text-sm text-admin-danger">{fieldError}</p> : null}
    </div>
  );
}

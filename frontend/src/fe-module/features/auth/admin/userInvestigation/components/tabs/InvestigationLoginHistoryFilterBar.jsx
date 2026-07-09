import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../../components/ui";

export function InvestigationLoginHistoryFilterBar({
  successDraft,
  fromDraft,
  toDraft,
  onSuccessChange,
  onFromChange,
  onToChange,
  onApply,
  onClear,
}) {
  return (
    <AdminFilterBar
      className="sm:grid-cols-2 lg:grid-cols-3"
      actions={
        <>
          <AdminFilterButton type="button" variant="primary" onClick={onApply}>
            Áp dụng
          </AdminFilterButton>
          <AdminFilterButton type="button" variant="secondary" onClick={onClear}>
            Xóa lọc
          </AdminFilterButton>
        </>
      }
    >
      <AdminFilterField label="Kết quả" htmlFor="history-success">
        <AdminFilterSelect
          id="history-success"
          value={successDraft}
          onChange={(e) => onSuccessChange(e.target.value)}
        >
          <option value="">Tất cả</option>
          <option value="true">Thành công</option>
          <option value="false">Thất bại</option>
        </AdminFilterSelect>
      </AdminFilterField>
      <AdminFilterField label="Từ ngày" htmlFor="history-from">
        <AdminFilterInput
          id="history-from"
          type="datetime-local"
          className="text-base"
          value={fromDraft}
          onChange={(e) => onFromChange(e.target.value)}
        />
      </AdminFilterField>
      <AdminFilterField label="Đến ngày" htmlFor="history-to">
        <AdminFilterInput
          id="history-to"
          type="datetime-local"
          className="text-base"
          value={toDraft}
          onChange={(e) => onToChange(e.target.value)}
        />
      </AdminFilterField>
    </AdminFilterBar>
  );
}

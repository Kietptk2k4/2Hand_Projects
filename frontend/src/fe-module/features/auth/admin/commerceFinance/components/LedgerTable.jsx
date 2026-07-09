import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";

function LedgerMobileCard({ entry }) {
  return (
    <AdminMobileCard ariaLabel={`Sổ cái ${entry.entryType}`}>
      <div className="flex items-start justify-between gap-2">
        <p className="font-medium text-admin-text">{entry.entryType}</p>
        <p className="shrink-0 font-medium tabular-nums text-admin-text" title={formatVndPrice(entry.netAmount)}>
          {formatVndPrice(entry.netAmount)}
        </p>
      </div>
      <p className="mt-2 text-xs text-admin-text-muted">
        {entry.createdAt ? formatDateTime(entry.createdAt) : "—"}
      </p>
    </AdminMobileCard>
  );
}

export function LedgerTable({ items }) {
  if (!items?.length) {
    return <p className="text-sm text-admin-text-muted">Chưa có bút toán sổ cái.</p>;
  }

  return (
    <>
      <AdminMobileCardList className="md:hidden">
        {items.map((entry) => (
          <LedgerMobileCard key={entry.id} entry={entry} />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="480px" ariaLabel="Sổ cái seller">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Thời gian</AdminDataTableCell>
            <AdminDataTableCell header>Loại</AdminDataTableCell>
            <AdminDataTableCell header>Net</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((entry) => (
            <AdminDataTableRow key={entry.id}>
              <AdminDataTableCell className="py-3 text-admin-text-secondary">
                {entry.createdAt ? formatDateTime(entry.createdAt) : "—"}
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3">{entry.entryType}</AdminDataTableCell>
              <AdminDataTableCell className="py-3 tabular-nums font-medium" title={formatVndPrice(entry.netAmount)}>
                {formatVndPrice(entry.netAmount)}
              </AdminDataTableCell>
            </AdminDataTableRow>
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}

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
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import { truncateSellerId } from "../utils/topSellersHelpers.js";

function formatEntryStatus(status) {
  if (!status) return "—";
  return status;
}

function LedgerMobileCard({ entry }) {
  return (
    <AdminMobileCard ariaLabel={`Sổ cái ${entry.entryType}`}>
      <div className="flex items-start justify-between gap-2">
        <p className="font-medium text-admin-text">{entry.entryType}</p>
        <p
          className="shrink-0 font-medium tabular-nums text-admin-text"
          title={formatVndPrice(entry.netAmount)}
        >
          {formatVndPrice(entry.netAmount)}
        </p>
      </div>
      <dl className="mt-3 grid grid-cols-2 gap-2 text-xs">
        <div>
          <dt className="text-admin-text-muted">Gross</dt>
          <dd className="mt-0.5 tabular-nums text-admin-text">
            {formatVndPrice(entry.grossAmount)}
          </dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Phí</dt>
          <dd className="mt-0.5 tabular-nums text-admin-text">
            {formatVndPrice(entry.platformFeeAmount)}
          </dd>
        </div>
        <div className="col-span-2">
          <dt className="text-admin-text-muted">Order item</dt>
          <dd
            className="mt-0.5 font-mono text-admin-text-secondary"
            title={entry.orderItemId}
          >
            {truncateSellerId(entry.orderItemId, 8, 4)}
          </dd>
        </div>
      </dl>
      <p className="mt-2 text-xs text-admin-text-muted">
        {entry.createdAt ? formatDateTime(entry.createdAt) : "—"} ·{" "}
        {formatEntryStatus(entry.status)}
      </p>
    </AdminMobileCard>
  );
}

export function LedgerTable({
  items,
  pagination,
  onPageChange,
  isLoading,
}) {
  const page = pagination?.page || 1;
  const totalPages =
    pagination?.totalPages ||
    Math.max(1, Math.ceil((pagination?.totalItems || 0) / (pagination?.limit || 20)));

  const summary =
    pagination?.totalItems != null
      ? `Trang ${page}/${totalPages} · ${pagination.totalItems} bút toán`
      : null;

  return (
    <AdminSurfaceCard padding="none" className="max-w-full min-w-0 overflow-hidden">
      <div className="border-b border-admin-border px-4 py-3 sm:px-5">
        <h2 className="text-base font-semibold text-admin-text">Sổ cái</h2>
        <p className="mt-0.5 text-sm text-admin-text-secondary">
          Bút toán ghi nhận và payout (CREDIT / DEBIT).
        </p>
      </div>

      {isLoading ? (
        <div className="space-y-3 p-4 sm:p-5">
          {Array.from({ length: 5 }, (_, index) => (
            <div key={index} className="h-10 animate-pulse rounded bg-admin-surface-muted" />
          ))}
        </div>
      ) : null}

      {!isLoading && !items?.length ? (
        <p className="px-4 py-10 text-center text-sm text-admin-text-muted sm:px-5">
          Chưa có bút toán sổ cái.
        </p>
      ) : null}

      {!isLoading && items?.length ? (
        <>
          <AdminMobileCardList className="p-4 md:hidden">
            {items.map((entry) => (
              <LedgerMobileCard key={entry.id} entry={entry} />
            ))}
          </AdminMobileCardList>

          <div className="hidden md:block">
            <AdminDataTable minWidth="880px" ariaLabel="Sổ cái seller">
              <AdminDataTableHead>
                <AdminDataTableRow>
                  <AdminDataTableCell header>Thời gian</AdminDataTableCell>
                  <AdminDataTableCell header>Loại</AdminDataTableCell>
                  <AdminDataTableCell header>Gross</AdminDataTableCell>
                  <AdminDataTableCell header>Phí</AdminDataTableCell>
                  <AdminDataTableCell header>Net</AdminDataTableCell>
                  <AdminDataTableCell header>Order item</AdminDataTableCell>
                  <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
                </AdminDataTableRow>
              </AdminDataTableHead>
              <AdminDataTableBody>
                {items.map((entry) => (
                  <AdminDataTableRow key={entry.id}>
                    <AdminDataTableCell className="py-3 text-admin-text-secondary">
                      {entry.createdAt ? formatDateTime(entry.createdAt) : "—"}
                    </AdminDataTableCell>
                    <AdminDataTableCell className="py-3">{entry.entryType}</AdminDataTableCell>
                    <AdminDataTableCell
                      className="py-3 tabular-nums"
                      title={formatVndPrice(entry.grossAmount)}
                    >
                      {formatVndPrice(entry.grossAmount)}
                    </AdminDataTableCell>
                    <AdminDataTableCell
                      className="py-3 tabular-nums"
                      title={formatVndPrice(entry.platformFeeAmount)}
                    >
                      {formatVndPrice(entry.platformFeeAmount)}
                    </AdminDataTableCell>
                    <AdminDataTableCell
                      className="py-3 tabular-nums font-medium"
                      title={formatVndPrice(entry.netAmount)}
                    >
                      {formatVndPrice(entry.netAmount)}
                    </AdminDataTableCell>
                    <AdminDataTableCell className="py-3">
                      <span
                        className="font-mono text-xs text-admin-text-secondary"
                        title={entry.orderItemId}
                      >
                        {entry.orderItemId
                          ? truncateSellerId(entry.orderItemId, 8, 4)
                          : "—"}
                      </span>
                    </AdminDataTableCell>
                    <AdminDataTableCell className="py-3 text-admin-text-secondary">
                      {formatEntryStatus(entry.status)}
                    </AdminDataTableCell>
                  </AdminDataTableRow>
                ))}
              </AdminDataTableBody>
            </AdminDataTable>
          </div>
        </>
      ) : null}

      {onPageChange && totalPages > 1 ? (
        <div className="border-t border-admin-border px-4 py-3 sm:px-5">
          <AdminPagination
            currentPage={page}
            totalPages={totalPages}
            summary={summary}
            disabled={isLoading}
            onPrevious={() => onPageChange(page - 1)}
            onNext={() => onPageChange(page + 1)}
          />
        </div>
      ) : null}
    </AdminSurfaceCard>
  );
}

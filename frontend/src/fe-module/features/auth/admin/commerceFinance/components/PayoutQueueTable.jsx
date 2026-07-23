import { useCallback, useState } from "react";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { truncateSellerId } from "../utils/topSellersHelpers.js";
import { PayoutStatusBadge } from "./ui/PayoutStatusBadge.jsx";

function CopyIconButton({ value, label, className = "" }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(async () => {
    if (!value) return;
    try {
      await navigator.clipboard.writeText(String(value));
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  }, [value]);

  if (!value) return null;

  return (
    <button
      type="button"
      onClick={handleCopy}
      aria-label={label}
      title={label}
      className={[
        "inline-flex h-8 w-8 items-center justify-center rounded-md border border-admin-border text-admin-text-secondary transition-colors hover:bg-admin-surface-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft",
        className,
      ].join(" ")}
    >
      <span className="material-symbols-outlined text-sm" aria-hidden="true">
        {copied ? "check" : "content_copy"}
      </span>
    </button>
  );
}

function SellerCell({ sellerId, onSellerDetail }) {
  return (
    <div className="flex min-w-0 items-center gap-2">
      <button
        type="button"
        onClick={() => onSellerDetail?.(sellerId)}
        className="min-w-0 text-left font-mono text-xs text-admin-accent hover:text-admin-accent-strong focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
        title={sellerId}
      >
        {truncateSellerId(sellerId, 10, 6)}
      </button>
      <CopyIconButton value={sellerId} label="Copy seller ID" />
    </div>
  );
}

function BankCell({ item }) {
  const accountLine = [item.bankAccountName, item.bankAccountNumber].filter(Boolean).join(" · ");
  return (
    <div className="flex min-w-0 items-start justify-between gap-2">
      <div className="min-w-0">
        <p className="text-sm text-admin-text">{item.bankName || "—"}</p>
        {accountLine ? (
          <p className="mt-0.5 break-all text-xs text-admin-text-secondary">{accountLine}</p>
        ) : null}
      </div>
      <CopyIconButton value={item.bankAccountNumber} label="Copy số tài khoản" />
    </div>
  );
}

function ActionButtons({ item, actionId, onAction, onOpenDetail }) {
  return (
    <div className="flex flex-wrap justify-end gap-2">
      <AdminFilterButton
        type="button"
        variant="secondary"
        className="min-h-11 px-2.5 py-1 text-xs"
        onClick={() => onOpenDetail?.(item)}
      >
        Chi tiết
      </AdminFilterButton>
      {item.status === "REQUESTED" ? (
        <>
          <AdminFilterButton
            type="button"
            variant="primary"
            className="min-h-11 px-2.5 py-1 text-xs"
            disabled={actionId === item.id}
            onClick={() => onAction?.(item, "approve")}
          >
            Duyệt
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11 border-admin-danger/30 px-2.5 py-1 text-xs text-admin-danger hover:bg-admin-danger-soft"
            disabled={actionId === item.id}
            onClick={() => onAction?.(item, "reject")}
          >
            Từ chối
          </AdminFilterButton>
        </>
      ) : null}
      {item.status === "APPROVED" ? (
        <AdminFilterButton
          type="button"
          variant="primary"
          className="min-h-11 px-2.5 py-1 text-xs"
          disabled={actionId === item.id}
          onClick={() => onAction?.(item, "mark-paid")}
        >
          Đã chuyển
        </AdminFilterButton>
      ) : null}
    </div>
  );
}

function PayoutQueueMobileCard({ item, actionId, onAction, onOpenDetail, onSellerDetail }) {
  return (
    <AdminMobileCard ariaLabel={`Yêu cầu rút tiền ${item.sellerId}`}>
      <div className="flex items-start justify-between gap-2">
        <SellerCell sellerId={item.sellerId} onSellerDetail={onSellerDetail} />
        <PayoutStatusBadge status={item.status} />
      </div>
      <p
        className="mt-2 text-lg font-semibold tabular-nums text-admin-text"
        title={formatVndPrice(item.amount)}
      >
        {formatVndPrice(item.amount)}
      </p>
      <p className="mt-1 text-xs text-admin-text-muted">
        {item.requestedAt ? formatDateTime(item.requestedAt) : "—"}
      </p>
      <div className="mt-2">
        <BankCell item={item} />
      </div>
      <div className="mt-3 flex flex-wrap justify-end gap-2 border-t border-admin-border-subtle pt-3">
        <ActionButtons
          item={item}
          actionId={actionId}
          onAction={onAction}
          onOpenDetail={onOpenDetail}
        />
      </div>
    </AdminMobileCard>
  );
}

export function PayoutQueueTable({
  items,
  actionId,
  onAction,
  onOpenDetail,
  onSellerDetail,
}) {
  if (!items?.length) {
    return null;
  }

  return (
    <>
      <AdminMobileCardList className="p-4 md:hidden">
        {items.map((item) => (
          <PayoutQueueMobileCard
            key={item.id}
            item={item}
            actionId={actionId}
            onAction={onAction}
            onOpenDetail={onOpenDetail}
            onSellerDetail={onSellerDetail}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="960px" ariaLabel="Hàng đợi rút tiền">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Thời gian</AdminDataTableCell>
            <AdminDataTableCell header>Seller</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Số tiền
            </AdminDataTableCell>
            <AdminDataTableCell header className="hidden md:table-cell">
              Tài khoản
            </AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Thao tác
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => (
            <AdminDataTableRow
              key={item.id}
              className="transition-colors hover:bg-admin-surface-muted/40"
            >
              <AdminDataTableCell className="py-3 text-admin-text-secondary">
                {item.requestedAt ? formatDateTime(item.requestedAt) : "—"}
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3">
                <SellerCell sellerId={item.sellerId} onSellerDetail={onSellerDetail} />
              </AdminDataTableCell>
              <AdminDataTableCell
                className="py-3 text-right font-semibold tabular-nums"
                title={formatVndPrice(item.amount)}
              >
                {formatVndPrice(item.amount)}
              </AdminDataTableCell>
              <AdminDataTableCell className="hidden py-3 md:table-cell">
                <BankCell item={item} />
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3">
                <PayoutStatusBadge status={item.status} />
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3">
                <ActionButtons
                  item={item}
                  actionId={actionId}
                  onAction={onAction}
                  onOpenDetail={onOpenDetail}
                />
              </AdminDataTableCell>
            </AdminDataTableRow>
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}

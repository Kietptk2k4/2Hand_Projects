import { formatVndPrice } from "../../../../social/utils/formatPrice";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
  AdminSurfaceCard,
} from "../../components/ui";
import {
  sellerFeeRatePercent,
  truncateSellerId,
} from "../utils/topSellersHelpers.js";

function formatFeeRate(seller) {
  const rate = sellerFeeRatePercent(seller);
  return `${(Math.round(rate * 10) / 10).toFixed(1)}%`;
}

function TopSellerMobileCard({ seller, rank, onSellerSelect }) {
  return (
    <AdminMobileCard
      ariaLabel={`Hạng ${rank}: ${seller.shopName}`}
      onClick={() => onSellerSelect?.(seller.sellerId, seller.shopName)}
      className="cursor-pointer transition-colors hover:border-admin-accent-border"
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="text-[11px] font-medium uppercase tracking-[0.06em] text-admin-text-muted">
            #{rank}
          </p>
          <p className="mt-1 font-medium text-admin-text">{seller.shopName || "—"}</p>
          <p
            className="mt-1 font-mono text-xs text-admin-text-muted"
            title={seller.sellerId}
          >
            {truncateSellerId(seller.sellerId)}
          </p>
        </div>
        <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
          chevron_right
        </span>
      </div>
      <dl className="mt-3 grid grid-cols-2 gap-2 text-xs">
        <div>
          <dt className="text-admin-text-muted">Gross</dt>
          <dd className="mt-0.5 font-medium tabular-nums text-admin-text">
            {formatVndPrice(seller.recognizedGross)}
          </dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Phí sàn</dt>
          <dd className="mt-0.5 font-medium tabular-nums text-admin-text">
            {formatVndPrice(seller.platformFee)}
          </dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Fee %</dt>
          <dd className="mt-0.5 tabular-nums text-admin-text">{formatFeeRate(seller)}</dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Đơn</dt>
          <dd className="mt-0.5 tabular-nums text-admin-text">{seller.completedItemCount}</dd>
        </div>
      </dl>
    </AdminMobileCard>
  );
}

export function TopSellersTable({ sellers, onSellerSelect }) {
  if (!sellers?.length) {
    return null;
  }

  return (
    <AdminSurfaceCard padding="none" className="max-w-full min-w-0 overflow-hidden">
      <div className="border-b border-admin-border px-4 py-3 sm:px-5">
        <h2 className="text-base font-semibold text-admin-text">Bảng xếp hạng</h2>
        <p className="mt-0.5 text-sm text-admin-text-secondary">
          Sắp xếp theo GMV đã ghi nhận (BE). Bấm hàng để mở chi tiết seller.
        </p>
      </div>

      <AdminMobileCardList className="p-4 md:hidden">
        {sellers.map((seller, index) => (
          <TopSellerMobileCard
            key={seller.sellerId}
            seller={seller}
            rank={index + 1}
            onSellerSelect={onSellerSelect}
          />
        ))}
      </AdminMobileCardList>

      <div className="hidden md:block">
        <AdminDataTable minWidth="820px" ariaLabel="Top sellers theo doanh thu">
          <AdminDataTableHead>
            <AdminDataTableRow>
              <AdminDataTableCell header className="w-14">
                #
              </AdminDataTableCell>
              <AdminDataTableCell header>Shop</AdminDataTableCell>
              <AdminDataTableCell header>Gross</AdminDataTableCell>
              <AdminDataTableCell header>Phí sàn</AdminDataTableCell>
              <AdminDataTableCell header>Fee %</AdminDataTableCell>
              <AdminDataTableCell header>Đơn</AdminDataTableCell>
              <AdminDataTableCell header className="w-12">
                <span className="sr-only">Chi tiết</span>
              </AdminDataTableCell>
            </AdminDataTableRow>
          </AdminDataTableHead>
          <AdminDataTableBody>
            {sellers.map((seller, index) => (
              <AdminDataTableRow
                key={seller.sellerId}
                onClick={() => onSellerSelect?.(seller.sellerId, seller.shopName)}
              >
                <AdminDataTableCell className="py-3 tabular-nums text-admin-text-muted">
                  {index + 1}
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3">
                  <p className="font-medium text-admin-text">{seller.shopName || "—"}</p>
                  <p
                    className="mt-0.5 font-mono text-xs text-admin-text-muted"
                    title={seller.sellerId}
                  >
                    {truncateSellerId(seller.sellerId)}
                  </p>
                </AdminDataTableCell>
                <AdminDataTableCell
                  className="py-3 tabular-nums"
                  title={formatVndPrice(seller.recognizedGross)}
                >
                  {formatVndPrice(seller.recognizedGross)}
                </AdminDataTableCell>
                <AdminDataTableCell
                  className="py-3 tabular-nums"
                  title={formatVndPrice(seller.platformFee)}
                >
                  {formatVndPrice(seller.platformFee)}
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3 tabular-nums">
                  {formatFeeRate(seller)}
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3 tabular-nums">
                  {seller.completedItemCount}
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3 text-admin-text-muted">
                  <span className="material-symbols-outlined text-lg" aria-hidden="true">
                    chevron_right
                  </span>
                </AdminDataTableCell>
              </AdminDataTableRow>
            ))}
          </AdminDataTableBody>
        </AdminDataTable>
      </div>
    </AdminSurfaceCard>
  );
}

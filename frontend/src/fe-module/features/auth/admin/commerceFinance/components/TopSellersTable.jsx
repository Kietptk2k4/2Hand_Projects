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

function TopSellerMobileCard({ seller, onSellerSelect }) {
  return (
    <AdminMobileCard ariaLabel={`Seller ${seller.shopName}`}>
      <p className="font-medium text-admin-text">{seller.shopName}</p>
      <button
        type="button"
        onClick={() => onSellerSelect?.(seller.sellerId)}
        className="mt-2 break-all text-left font-mono text-xs text-admin-accent hover:underline"
        title={seller.sellerId}
      >
        {seller.sellerId}
      </button>
      <dl className="mt-3 grid grid-cols-2 gap-2 text-xs">
        <div>
          <dt className="text-admin-text-muted">Gross</dt>
          <dd className="mt-0.5 font-medium tabular-nums text-admin-text" title={formatVndPrice(seller.recognizedGross)}>
            {formatVndPrice(seller.recognizedGross)}
          </dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Phí sàn</dt>
          <dd className="mt-0.5 font-medium tabular-nums text-admin-text" title={formatVndPrice(seller.platformFee)}>
            {formatVndPrice(seller.platformFee)}
          </dd>
        </div>
        <div className="col-span-2">
          <dt className="text-admin-text-muted">Đơn hoàn tất</dt>
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
    <>
      <AdminMobileCardList className="p-4 md:hidden">
        {sellers.map((seller) => (
          <TopSellerMobileCard key={seller.sellerId} seller={seller} onSellerSelect={onSellerSelect} />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="720px" ariaLabel="Top sellers theo doanh thu">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Shop</AdminDataTableCell>
            <AdminDataTableCell header>Seller ID</AdminDataTableCell>
            <AdminDataTableCell header>Gross</AdminDataTableCell>
            <AdminDataTableCell header>Phí sàn</AdminDataTableCell>
            <AdminDataTableCell header>Đơn</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {sellers.map((seller) => (
            <AdminDataTableRow key={seller.sellerId}>
              <AdminDataTableCell className="py-3 font-medium">{seller.shopName}</AdminDataTableCell>
              <AdminDataTableCell className="py-3">
                <AdminFilterButton
                  type="button"
                  variant="secondary"
                  className="h-auto min-h-0 break-all border-0 bg-transparent px-0 py-1 font-mono text-xs text-admin-accent hover:bg-transparent hover:text-admin-accent-strong hover:underline"
                  onClick={() => onSellerSelect?.(seller.sellerId)}
                  title={seller.sellerId}
                >
                  {seller.sellerId}
                </AdminFilterButton>
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3 tabular-nums" title={formatVndPrice(seller.recognizedGross)}>
                {formatVndPrice(seller.recognizedGross)}
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3 tabular-nums" title={formatVndPrice(seller.platformFee)}>
                {formatVndPrice(seller.platformFee)}
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3 tabular-nums">{seller.completedItemCount}</AdminDataTableCell>
            </AdminDataTableRow>
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}

import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function OrderSupportTable({
  orders,
  selectedOrderId,
  onOrderSelect,
  formatDateTime,
  formatVndPrice,
}) {
  if (!orders?.length) {
    return (
      <p className="py-6 text-center text-sm text-admin-text-secondary">
        Không có đơn hàng phù hợp bộ lọc.
      </p>
    );
  }

  return (
    <>
      <AdminMobileCardList>
        {orders.map((order) => {
          const isSelected = order.order_id === selectedOrderId;
          return (
            <AdminMobileCard
              key={order.order_id}
              isSelected={isSelected}
              onClick={() => onOrderSelect?.(order.order_id)}
              ariaLabel={`Chọn đơn hàng ${order.order_id}`}
            >
              <p className="font-mono text-xs text-admin-text-muted">{order.order_id}</p>
              <p className="mt-1 text-sm font-medium text-admin-text">
                {formatVndPrice(order.final_amount)}
              </p>
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <SupportStatusBadge status={order.order_status} />
                <SupportStatusBadge status={order.payment_status} />
              </div>
              <p className="mt-2 text-xs text-admin-text-secondary">
                {order.payment_method} · {formatDateTime(order.created_at)}
              </p>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="900px" ariaLabel="Danh sách đơn hàng hỗ trợ">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Tạo lúc</AdminDataTableCell>
            <AdminDataTableCell header>Order ID</AdminDataTableCell>
            <AdminDataTableCell header>Buyer ID</AdminDataTableCell>
            <AdminDataTableCell header>Phương thức</AdminDataTableCell>
            <AdminDataTableCell header>Số tiền</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Thanh toán</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {orders.map((order) => {
            const isSelected = order.order_id === selectedOrderId;
            return (
              <AdminDataTableRow
                key={order.order_id}
                isSelected={isSelected}
                onClick={() => onOrderSelect?.(order.order_id)}
              >
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  {formatDateTime(order.created_at)}
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs">{order.order_id}</AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs">{order.buyer_id}</AdminDataTableCell>
                <AdminDataTableCell>{order.payment_method}</AdminDataTableCell>
                <AdminDataTableCell>{formatVndPrice(order.final_amount)}</AdminDataTableCell>
                <AdminDataTableCell>
                  <SupportStatusBadge status={order.order_status} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <SupportStatusBadge status={order.payment_status} />
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}

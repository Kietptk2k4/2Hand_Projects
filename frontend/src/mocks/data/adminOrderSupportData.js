export const MOCK_ORDER_ID = "a1111111-1111-4111-8111-111111111101";
export const MOCK_PAYMENT_ID = "a2222222-2222-4222-8222-222222222202";
export const MOCK_SHIPMENT_ID = "a3333333-3333-4333-8333-333333333303";

const MOCK_ORDERS_LIST = [
  {
    order_id: MOCK_ORDER_ID,
    buyer_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    order_status: "PROCESSING",
    payment_status: "PAID",
    payment_method: "PAYOS",
    final_amount: 350000,
    created_at: "2026-05-19T10:00:00Z",
    updated_at: "2026-05-20T08:00:00Z",
  },
  {
    order_id: "b1111111-1111-4111-8111-111111111102",
    buyer_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    order_status: "AWAITING_PAYMENT",
    payment_status: "PENDING",
    payment_method: "COD",
    final_amount: 420000,
    created_at: "2026-06-09T14:30:00Z",
    updated_at: "2026-06-09T14:30:00Z",
  },
  {
    order_id: "c2222222-2222-4222-8222-222222222203",
    buyer_id: "c9c9cf87-3bc3-4b12-9f27-fe1f6faf06e2",
    order_status: "COMPLETED",
    payment_status: "PAID",
    payment_method: "VNPAY",
    final_amount: 510000,
    created_at: "2026-06-15T09:00:00Z",
    updated_at: "2026-06-16T11:00:00Z",
  },
];

function parseDateParam(value) {
  if (!value) return null;
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

export function getMockOrdersForSupport({
  q,
  status,
  payment_status,
  payment_method,
  from,
  to,
  sort = "created_at",
  page = 1,
  size = 20,
} = {}) {
  let items = [...MOCK_ORDERS_LIST];

  if (q) {
    const needle = q.toLowerCase();
    items = items.filter(
      (order) =>
        order.order_id.toLowerCase().includes(needle) ||
        order.buyer_id.toLowerCase().includes(needle),
    );
  }
  if (status) {
    items = items.filter((order) => order.order_status === status.toUpperCase());
  }
  if (payment_status) {
    items = items.filter((order) => order.payment_status === payment_status.toUpperCase());
  }
  if (payment_method) {
    items = items.filter((order) => order.payment_method === payment_method.toUpperCase());
  }
  if (from) {
    const fromDate = parseDateParam(from);
    if (fromDate) {
      items = items.filter((order) => new Date(order.created_at) >= fromDate);
    }
  }
  if (to) {
    const toDate = parseDateParam(to);
    if (toDate) {
      items = items.filter((order) => new Date(order.created_at) <= toDate);
    }
  }

  const sortKey = sort === "updated_at" ? "updated_at" : "created_at";
  items.sort((a, b) => new Date(b[sortKey]).getTime() - new Date(a[sortKey]).getTime());

  const totalElements = items.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const start = (safePage - 1) * size;

  return {
    page: safePage,
    size,
    total_elements: totalElements,
    total_pages: totalPages,
    orders: items.slice(start, start + size),
  };
}

function buildMockOrderDetailFromListRow(row) {
  if (!row) return null;

  const isPrimary = row.order_id === MOCK_ORDER_ID;

  return {
    order_id: row.order_id,
    buyer_id: row.buyer_id,
    order_status: row.order_status,
    order_payment_status: row.payment_status,
    payment_method: row.payment_method,
    total_amount: row.final_amount,
    final_amount: row.final_amount,
    created_at: row.created_at,
    updated_at: row.updated_at,
    completed_at: row.order_status === "COMPLETED" ? row.updated_at : null,
    contact_fields_masked: true,
    cancellation_note: row.order_status === "CANCELLED" ? "Khách hủy đơn" : null,
    active_refund_request:
      row.order_status === "PROCESSING"
        ? {
            status: "PENDING",
            amount: 100000,
          }
        : null,
    payment: isPrimary
      ? {
          payment_id: MOCK_PAYMENT_ID,
          status: row.payment_status,
          payment_method: row.payment_method,
          amount: row.final_amount,
          currency: "VND",
          paid_at: row.payment_status === "PAID" ? row.updated_at : null,
          expired_at: null,
          checkout_url_expired_at: null,
          timeline: [
            {
              old_status: "PENDING",
              new_status: row.payment_status,
              occurred_at: row.updated_at,
            },
          ],
        }
      : null,
    items: [
      {
        order_item_id: "c4444444-4444-4444-8444-444444444404",
        product_id: "d5555555-5555-4555-8555-555555555505",
        seller_id: "e6666666-6666-4666-8666-666666666606",
        shipment_id: isPrimary ? MOCK_SHIPMENT_ID : null,
        quantity: 1,
        status: row.order_status,
        unit_price_snapshot: row.final_amount - 30000,
        final_price: row.final_amount - 30000,
        sku_snapshot: "SKU-001",
        product_name_snapshot: "Ao thun vintage",
        image_snapshot: "",
        attributes_snapshot: "M",
        shop_name_snapshot: "2Hands Shop",
        shipping_fee_allocated: 30000,
        completed_at: null,
      },
    ],
    shipments: isPrimary
      ? [
          {
            shipment_id: MOCK_SHIPMENT_ID,
            seller_id: "e6666666-6666-4666-8666-666666666606",
            status: "SHIPPED",
            carrier: "GHN",
            tracking_number: "TRACK-9",
            shipping_fee: 30000,
            shipment_type: "STANDARD",
            estimated_delivery_date: "2026-05-22",
            shipped_at: "2026-05-20T08:00:00Z",
            delivered_at: null,
            shipping_address: {
              receiver_name: "Nguyen ***",
              phone: "***4567",
              province_code: "79",
              district_code: "760",
              ward_code: "26734",
              address_detail: "***",
              full_address: "*** Quan 1, TP.HCM",
            },
            timeline: [
              {
                old_status: "PENDING",
                new_status: "SHIPPED",
                raw_status: "transporting",
                occurred_at: "2026-05-20T08:00:00Z",
              },
            ],
          },
        ]
      : [],
    order_timeline: [
      {
        old_status: "CREATED",
        new_status: row.order_status,
        changed_by: "SYSTEM",
        note: "Order updated",
        occurred_at: row.updated_at,
      },
    ],
  };
}

export function getMockOrderSupportDetail(orderId) {
  const row = MOCK_ORDERS_LIST.find((order) => order.order_id === orderId);
  if (!row) return null;
  return buildMockOrderDetailFromListRow(row);
}

const MOCK_PAYMENT_COD_ID = "b2222222-2222-4222-8222-222222222203";

const MOCK_PAYMENT_VNPAY_ID = "c3333333-3333-4333-8333-333333333303";

const MOCK_PAYMENTS_LIST = [
  {
    payment_id: MOCK_PAYMENT_COD_ID,
    order_id: "b1111111-1111-4111-8111-111111111102",
    payment_method: "COD",
    amount: 420000,
    currency: "VND",
    status: "PENDING",
    paid_at: null,
    created_at: "2026-06-09T14:30:00Z",
    reconciliation_status: "NOT_APPLICABLE",
  },
  {
    payment_id: MOCK_PAYMENT_ID,
    order_id: MOCK_ORDER_ID,
    payment_method: "PAYOS",
    amount: 350000,
    currency: "VND",
    status: "PAID",
    paid_at: "2026-05-20T08:00:00Z",
    created_at: "2026-05-19T10:00:00Z",
    reconciliation_status: "RECONCILED",
  },
  {
    payment_id: MOCK_PAYMENT_VNPAY_ID,
    order_id: "c2222222-2222-4222-8222-222222222203",
    payment_method: "VNPAY",
    amount: 510000,
    currency: "VND",
    status: "PAID",
    paid_at: "2026-06-16T11:00:00Z",
    created_at: "2026-06-15T09:00:00Z",
    reconciliation_status: "NOT_APPLICABLE",
  },
];

export function getMockPaymentsForSupport({
  q,
  status,
  payment_method,
  reconciliation_status,
  order_id,
  from,
  to,
  page = 1,
  size = 20,
} = {}) {
  let items = [...MOCK_PAYMENTS_LIST].sort(
    (a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime(),
  );

  if (q) {
    const needle = q.toLowerCase();
    items = items.filter((payment) => payment.payment_id.toLowerCase().includes(needle));
  }
  if (status) {
    items = items.filter((payment) => payment.status === status.toUpperCase());
  }
  if (payment_method) {
    items = items.filter((payment) => payment.payment_method === payment_method.toUpperCase());
  }
  if (reconciliation_status) {
    items = items.filter(
      (payment) => payment.reconciliation_status === reconciliation_status.toUpperCase(),
    );
  }
  if (order_id) {
    items = items.filter((payment) => payment.order_id === order_id);
  }
  if (from) {
    const fromDate = parseDateParam(from);
    if (fromDate) {
      items = items.filter((payment) => new Date(payment.created_at) >= fromDate);
    }
  }
  if (to) {
    const toDate = parseDateParam(to);
    if (toDate) {
      items = items.filter((payment) => new Date(payment.created_at) <= toDate);
    }
  }

  const totalElements = items.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const start = (safePage - 1) * size;

  return {
    page: safePage,
    size,
    total_elements: totalElements,
    total_pages: totalPages,
    payments: items.slice(start, start + size),
  };
}

function buildMockPaymentDetailFromListRow(row) {
  if (!row) return null;

  const isPayos = row.payment_id === MOCK_PAYMENT_ID;

  return {
    payment_id: row.payment_id,
    order_id: row.order_id,
    payer_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    payment_method: row.payment_method,
    amount: row.amount,
    currency: row.currency || "VND",
    status: row.status,
    paid_at: row.paid_at,
    expired_at: null,
    created_at: row.created_at,
    updated_at: row.paid_at || row.created_at,
    provider_order_code: isPayos ? "PAYOS-123" : row.payment_method === "VNPAY" ? "VNPAY-456" : null,
    provider_transaction_id: isPayos ? "TX-999" : null,
    checkout_url_available: false,
    checkout_url_expired_at: null,
    order_status: "PROCESSING",
    order_payment_status: row.status,
    reconciliation_status: row.reconciliation_status || "NOT_APPLICABLE",
    status_timeline: [
      {
        old_status: "PENDING",
        new_status: row.status,
        occurred_at: row.paid_at || row.created_at,
      },
    ],
    webhook_events: isPayos
      ? [
          {
            provider: "PAYOS",
            event_type: "PAYMENT_SUCCESS",
            signature_valid: true,
            processed: true,
            received_at: "2026-05-20T08:00:01Z",
          },
        ]
      : [],
  };
}

export function getMockPaymentSupportDetail(paymentId) {
  const row = MOCK_PAYMENTS_LIST.find((payment) => payment.payment_id === paymentId);
  if (!row) return null;
  return buildMockPaymentDetailFromListRow(row);
}

const MOCK_SHIPMENTS_LIST = [
  {
    shipment_id: MOCK_SHIPMENT_ID,
    order_id: MOCK_ORDER_ID,
    seller_id: "e6666666-6666-4666-8666-666666666606",
    carrier: "GHN",
    internal_status: "SHIPPED",
    tracking_number: "TRACK-9",
    ghn_order_code: "GHN-123",
    shipped_at: "2026-05-20T08:00:00Z",
    created_at: "2026-05-19T10:00:00Z",
    updated_at: "2026-05-20T08:00:00Z",
  },
  {
    shipment_id: "a4444444-4444-4444-8444-444444444404",
    order_id: "b1111111-1111-4111-8111-111111111102",
    seller_id: "e6666666-6666-4666-8666-666666666606",
    carrier: "MANUAL",
    internal_status: "PENDING",
    tracking_number: null,
    ghn_order_code: null,
    shipped_at: null,
    created_at: "2026-06-08T12:00:00Z",
    updated_at: "2026-06-08T12:00:00Z",
  },
];

export function getMockShipmentSupportList({
  q,
  status,
  carrier,
  order_id,
  from,
  to,
  sort = "updated_at",
  page = 1,
  size = 20,
} = {}) {
  let items = [...MOCK_SHIPMENTS_LIST];

  if (q) {
    const needle = q.toLowerCase();
    items = items.filter(
      (row) =>
        row.shipment_id.toLowerCase().includes(needle) ||
        row.order_id.toLowerCase().includes(needle) ||
        (row.tracking_number || "").toLowerCase().includes(needle) ||
        (row.ghn_order_code || "").toLowerCase().includes(needle),
    );
  }
  if (status) {
    items = items.filter((row) => row.internal_status === status.toUpperCase());
  }
  if (carrier) {
    items = items.filter((row) => row.carrier === carrier.toUpperCase());
  }
  if (order_id) {
    items = items.filter((row) => row.order_id === order_id);
  }
  if (from) {
    const fromDate = parseDateParam(from);
    if (fromDate) {
      items = items.filter((row) => new Date(row.created_at) >= fromDate);
    }
  }
  if (to) {
    const toDate = parseDateParam(to);
    if (toDate) {
      items = items.filter((row) => new Date(row.created_at) <= toDate);
    }
  }

  const sortKey = sort || "updated_at";
  items.sort((a, b) => {
    const left = a[sortKey] ? new Date(a[sortKey]).getTime() : 0;
    const right = b[sortKey] ? new Date(b[sortKey]).getTime() : 0;
    return right - left;
  });

  const totalElements = items.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const start = (safePage - 1) * size;

  return {
    page: safePage,
    size,
    total_elements: totalElements,
    total_pages: totalPages,
    shipments: items.slice(start, start + size),
  };
}

export function getMockShipmentSupportDetail(shipmentId) {
  const row = MOCK_SHIPMENTS_LIST.find((item) => item.shipment_id === shipmentId);
  if (!row) return null;

  if (row.shipment_id === MOCK_SHIPMENT_ID) {
    return {
      shipment_id: MOCK_SHIPMENT_ID,
      order_id: MOCK_ORDER_ID,
      seller_id: "e6666666-6666-4666-8666-666666666606",
      buyer_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
      order_status: "PROCESSING",
      carrier: "GHN",
      shipment_type: "STANDARD",
      internal_status: "SHIPPED",
      carrier_status: "transporting",
      ghn_order_code: "GHN-123",
      tracking_number: "TRACK-9",
      shipping_fee: 30000,
      cod_amount: 0,
      weight_gram: 500,
      estimated_delivery_date: "2026-05-22",
      shipped_at: "2026-05-20T08:00:00Z",
      delivered_at: null,
      created_at: "2026-05-19T10:00:00Z",
      updated_at: "2026-05-20T08:00:00Z",
      contact_fields_masked: true,
      shipping_address: {
        receiver_name: "Nguyen ***",
        phone: "***4567",
        province_code: "79",
        district_code: "760",
        ward_code: "26734",
        address_detail: "***",
        full_address: "*** Quan 1, TP.HCM",
      },
      order_items: [
        {
          order_item_id: "c4444444-4444-4444-8444-444444444404",
          product_name_snapshot: "Ao thun vintage",
          quantity: 1,
          status: "PROCESSING",
        },
      ],
      status_history: [
        {
          old_status: "PENDING",
          new_status: "SHIPPED",
          raw_status: "transporting",
          occurred_at: "2026-05-20T08:00:00Z",
        },
      ],
      carrier_webhook_events: [
        {
          carrier_status: "transporting",
          processed: true,
          received_at: "2026-05-20T08:00:01Z",
        },
      ],
    };
  }

  return {
    shipment_id: row.shipment_id,
    order_id: row.order_id,
    seller_id: row.seller_id,
    buyer_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    order_status: "AWAITING_PAYMENT",
    carrier: row.carrier,
    shipment_type: "STANDARD",
    internal_status: row.internal_status,
    carrier_status: row.carrier === "GHN" ? "ready_to_pick" : null,
    ghn_order_code: row.ghn_order_code,
    tracking_number: row.tracking_number,
    shipping_fee: 25000,
    cod_amount: 420000,
    weight_gram: 800,
    estimated_delivery_date: null,
    shipped_at: row.shipped_at,
    delivered_at: null,
    created_at: row.created_at,
    updated_at: row.updated_at,
    contact_fields_masked: true,
    shipping_address: {
      receiver_name: "Tran ***",
      phone: "***7890",
      full_address: "*** Quan 3, TP.HCM",
    },
    order_items: [],
    status_history: [
      {
        old_status: null,
        new_status: row.internal_status,
        raw_status: null,
        occurred_at: row.created_at,
      },
    ],
    carrier_webhook_events: [],
  };
}

const MOCK_WEBHOOK_LOGS = [
  {
    log_id: "f7777777-7777-4777-8777-777777777707",
    provider: "PAYOS",
    reference_id: "PAYOS-123",
    event_type: "PAYMENT_SUCCESS",
    processing_status: "PROCESSED",
    signature_valid: true,
    idempotency_key: "PAYOS:PAYOS-123:PAYMENT_SUCCESS",
    payload_summary: { code: "00", order_code: "PAYOS-123" },
    received_at: "2026-05-20T08:00:01Z",
    payment_id: "a1111111-1111-4111-8111-111111111101",
    shipment_id: null,
    order_id: "b1111111-1111-4111-8111-111111111101",
  },
  {
    log_id: "f8888888-8888-4888-8888-888888888808",
    provider: "GHN",
    reference_id: "GHN-123",
    event_type: "STATUS_UPDATE",
    processing_status: "PROCESSED",
    signature_valid: null,
    idempotency_key: "GHN:GHN-123:STATUS_UPDATE",
    payload_summary: { status: "transporting" },
    received_at: "2026-05-20T08:05:00Z",
    payment_id: null,
    shipment_id: "c1111111-1111-4111-8111-111111111101",
    order_id: "b1111111-1111-4111-8111-111111111101",
  },
];

export function getMockWebhookLogStats(logs = MOCK_WEBHOOK_LOGS) {
  return {
    total: logs.length,
    pending: logs.filter((log) => log.processing_status === "PENDING").length,
    invalid_signature: logs.filter((log) => log.processing_status === "INVALID_SIGNATURE").length,
    processed: logs.filter((log) => log.processing_status === "PROCESSED").length,
    by_provider: {
      payos: logs.filter((log) => log.provider === "PAYOS").length,
      ghn: logs.filter((log) => log.provider === "GHN").length,
    },
  };
}

export function getMockWebhookLogs({ provider, reference_id, q, status, page = 1, size = 20 }) {
  let items = [...MOCK_WEBHOOK_LOGS];
  if (provider) {
    items = items.filter((log) => log.provider === provider.toUpperCase());
  }
  const search = q || reference_id;
  if (search) {
    const needle = search.toLowerCase();
    items = items.filter((log) => log.reference_id.toLowerCase().includes(needle));
  }
  if (status) {
    items = items.filter((log) => log.processing_status === status.toUpperCase());
  }

  const totalElements = items.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const start = (safePage - 1) * size;

  return {
    page: safePage,
    size,
    total_elements: totalElements,
    total_pages: totalPages,
    logs: items.slice(start, start + size),
  };
}

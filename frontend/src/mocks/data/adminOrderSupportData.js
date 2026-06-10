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
];

export function getMockOrdersForSupport({
  status,
  payment_method,
  sort = "created_at",
  page = 1,
  size = 20,
} = {}) {
  let items = [...MOCK_ORDERS_LIST];

  if (status) {
    items = items.filter((order) => order.order_status === status.toUpperCase());
  }
  if (payment_method) {
    items = items.filter((order) => order.payment_method === payment_method.toUpperCase());
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

export function getMockOrderSupportDetail(orderId) {
  if (orderId !== MOCK_ORDER_ID) return null;

  return {
    order_id: MOCK_ORDER_ID,
    buyer_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    order_status: "PROCESSING",
    order_payment_status: "PAID",
    payment_method: "PAYOS",
    total_amount: 350000,
    final_amount: 350000,
    created_at: "2026-05-19T10:00:00Z",
    updated_at: "2026-05-20T08:00:00Z",
    completed_at: null,
    contact_fields_masked: true,
    payment: {
      payment_id: MOCK_PAYMENT_ID,
      status: "PAID",
      payment_method: "PAYOS",
      amount: 350000,
      currency: "VND",
      paid_at: "2026-05-20T08:00:00Z",
      expired_at: null,
      checkout_url_expired_at: null,
      timeline: [
        {
          old_status: "PENDING",
          new_status: "PAID",
          occurred_at: "2026-05-20T08:00:00Z",
        },
      ],
    },
    items: [
      {
        order_item_id: "c4444444-4444-4444-8444-444444444404",
        product_id: "d5555555-5555-4555-8555-555555555505",
        seller_id: "e6666666-6666-4666-8666-666666666606",
        shipment_id: MOCK_SHIPMENT_ID,
        quantity: 1,
        status: "PROCESSING",
        unit_price_snapshot: 320000,
        final_price: 320000,
        sku_snapshot: "SKU-001",
        product_name_snapshot: "Ao thun vintage",
        image_snapshot: "",
        attributes_snapshot: "M",
        shop_name_snapshot: "2Hands Shop",
        shipping_fee_allocated: 30000,
        completed_at: null,
      },
    ],
    shipments: [
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
    ],
    order_timeline: [
      {
        old_status: "CREATED",
        new_status: "PROCESSING",
        changed_by: "SYSTEM",
        note: "Payment confirmed",
        occurred_at: "2026-05-20T08:00:01Z",
      },
    ],
  };
}

const MOCK_PAYMENT_COD_ID = "b2222222-2222-4222-8222-222222222203";

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
  },
];

export function getMockPaymentsForSupport({
  status,
  payment_method,
  order_id,
  page = 1,
  size = 20,
} = {}) {
  let items = [...MOCK_PAYMENTS_LIST].sort(
    (a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime(),
  );

  if (status) {
    items = items.filter((payment) => payment.status === status.toUpperCase());
  }
  if (payment_method) {
    items = items.filter((payment) => payment.payment_method === payment_method.toUpperCase());
  }
  if (order_id) {
    items = items.filter((payment) => payment.order_id === order_id);
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

export function getMockPaymentSupportDetail(paymentId) {
  if (paymentId !== MOCK_PAYMENT_ID) return null;

  return {
    payment_id: MOCK_PAYMENT_ID,
    order_id: MOCK_ORDER_ID,
    payer_id: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    payment_method: "PAYOS",
    amount: 350000,
    currency: "VND",
    status: "PAID",
    paid_at: "2026-05-20T08:00:00Z",
    expired_at: null,
    created_at: "2026-05-19T10:00:00Z",
    updated_at: "2026-05-20T08:00:00Z",
    provider_order_code: "PAYOS-123",
    provider_transaction_id: "TX-999",
    checkout_url_available: false,
    checkout_url_expired_at: null,
    order_status: "PROCESSING",
    order_payment_status: "PAID",
    reconciliation_status: "RECONCILED",
    status_timeline: [
      {
        old_status: "PENDING",
        new_status: "PAID",
        occurred_at: "2026-05-20T08:00:00Z",
      },
    ],
    webhook_events: [
      {
        provider: "PAYOS",
        event_type: "PAYMENT_SUCCESS",
        signature_valid: true,
        processed: true,
        received_at: "2026-05-20T08:00:01Z",
      },
    ],
  };
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

export function getMockShipmentSupportList({ status, carrier, sort = "updated_at", page = 1, size = 20 } = {}) {
  let items = [...MOCK_SHIPMENTS_LIST];

  if (status) {
    items = items.filter((row) => row.internal_status === status.toUpperCase());
  }
  if (carrier) {
    items = items.filter((row) => row.carrier === carrier.toUpperCase());
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
  if (shipmentId !== MOCK_SHIPMENT_ID) return null;

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

const MOCK_WEBHOOK_LOGS = [
  {
    log_id: "f7777777-7777-4777-8777-777777777707",
    provider: "PAYOS",
    reference_id: "PAYOS-123",
    event_type: "PAYMENT_SUCCESS",
    processing_status: "PROCESSED",
    signature_valid: true,
    retry_count: 0,
    idempotency_key: "PAYOS:PAYOS-123:PAYMENT_SUCCESS",
    payload_summary: { code: "00", order_code: "PAYOS-123" },
    received_at: "2026-05-20T08:00:01Z",
  },
  {
    log_id: "f8888888-8888-4888-8888-888888888808",
    provider: "GHN",
    reference_id: "GHN-123",
    event_type: "STATUS_UPDATE",
    processing_status: "PROCESSED",
    signature_valid: null,
    retry_count: 0,
    idempotency_key: "GHN:GHN-123:STATUS_UPDATE",
    payload_summary: { status: "transporting" },
    received_at: "2026-05-20T08:05:00Z",
  },
];

export function getMockWebhookLogs({ provider, reference_id, status, page = 1, size = 20 }) {
  let items = [...MOCK_WEBHOOK_LOGS];
  if (provider) {
    items = items.filter((log) => log.provider === provider.toUpperCase());
  }
  if (reference_id) {
    const needle = reference_id.toLowerCase();
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

export const orderKeys = {
  all: ["commerce", "orders"],
  list: (filters = {}) => [...orderKeys.all, "list", filters],
  detail: (orderId) => [...orderKeys.all, "detail", orderId],
  track: (orderId) => [...orderKeys.all, "track", orderId],
  shipment: (shipmentId) => [...orderKeys.all, "shipment", shipmentId],
  shipmentTracking: (shipmentId) => [...orderKeys.all, "shipment-tracking", shipmentId],
};

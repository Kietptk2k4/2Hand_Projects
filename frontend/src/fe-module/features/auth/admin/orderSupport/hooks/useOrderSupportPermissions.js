import { useMemo } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  ORDER_SUPPORT_PERMISSIONS,
  hasSupportPermission,
} from "../constants/orderSupportPermissions.js";

export function useOrderSupportPermissions() {
  const { user } = useAuthSession();
  const permissions = user?.permissions || [];

  return useMemo(
    () => ({
      permissions,
      canReadOrder: hasSupportPermission(permissions, ORDER_SUPPORT_PERMISSIONS.READ_ORDER),
      canReadPayment: hasSupportPermission(permissions, ORDER_SUPPORT_PERMISSIONS.READ_PAYMENT),
      canReadShipment: hasSupportPermission(permissions, ORDER_SUPPORT_PERMISSIONS.READ_SHIPMENT),
      canWriteShipment: hasSupportPermission(permissions, ORDER_SUPPORT_PERMISSIONS.WRITE_SHIPMENT),
      canForceWriteShipment: hasSupportPermission(
        permissions,
        ORDER_SUPPORT_PERMISSIONS.FORCE_WRITE_SHIPMENT,
      ),
      canReadWebhook: hasSupportPermission(permissions, ORDER_SUPPORT_PERMISSIONS.READ_WEBHOOK),
    }),
    [permissions],
  );
}

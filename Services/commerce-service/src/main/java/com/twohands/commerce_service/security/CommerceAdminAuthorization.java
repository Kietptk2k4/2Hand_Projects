package com.twohands.commerce_service.security;

import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CommerceAdminAuthorization {

    public static final String PERMISSION_REVIEW_HIDE = "COMMERCE_REVIEW_HIDE";
    public static final String PERMISSION_SHOP_SUSPEND = "COMMERCE_SHOP_SUSPEND";
    public static final String PERMISSION_SHOP_CLOSE = "COMMERCE_SHOP_CLOSE";
    public static final String PERMISSION_PRODUCT_REMOVE = "COMMERCE_PRODUCT_REMOVE";
    public static final String PERMISSION_ORDER_SUPPORT_READ = "ORDER_SUPPORT_READ";
    public static final String PERMISSION_PAYMENT_SUPPORT_READ = "PAYMENT_SUPPORT_READ";
    public static final String PERMISSION_SHIPMENT_SUPPORT_READ = "SHIPMENT_SUPPORT_READ";
    public static final String PERMISSION_SHIPMENT_SUPPORT_WRITE = "SHIPMENT_SUPPORT_WRITE";
    public static final String PERMISSION_SHIPMENT_SUPPORT_FORCE_WRITE = "SHIPMENT_SUPPORT_FORCE_WRITE";
    public static final String PERMISSION_WEBHOOK_SUPPORT_READ = "WEBHOOK_SUPPORT_READ";
    public static final String PERMISSION_PAYOUT_SUPPORT_READ = "PAYOUT_SUPPORT_READ";
    public static final String PERMISSION_PAYOUT_SUPPORT_APPROVE = "PAYOUT_SUPPORT_APPROVE";
    public static final String PERMISSION_REFUND_SUPPORT_READ = "REFUND_SUPPORT_READ";
    public static final String PERMISSION_REFUND_SUPPORT_APPROVE = "REFUND_SUPPORT_APPROVE";
    public static final String PERMISSION_FINANCE_SUPPORT_READ = "FINANCE_SUPPORT_READ";
    private static final String ADMIN_ROLE = "ADMIN";

    public void requirePermission(AuthenticatedUser user, String permission) {
        if (hasPermission(user, permission)) {
            return;
        }
        throw new AppException(ErrorCode.FORBIDDEN, "Missing permission: " + permission);
    }

    public void requireAnyPermission(AuthenticatedUser user, String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(user, permission)) {
                return;
            }
        }
        throw new AppException(ErrorCode.FORBIDDEN, "Missing required admin permission");
    }

    private boolean hasPermission(AuthenticatedUser user, String permission) {
        if (user.permissions() != null
                && user.permissions().stream().anyMatch(permission::equals)) {
            return true;
        }
        return user.roles() != null
                && user.roles().stream().anyMatch(role -> ADMIN_ROLE.equalsIgnoreCase(role));
    }
}

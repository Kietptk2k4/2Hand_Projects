package com.twohands.notification_service.domain.push;

import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.social.SocialNotificationTemplatePolicy;

import java.util.Optional;

public final class PushNotificationTemplatePolicy {

    private PushNotificationTemplatePolicy() {
    }

    public static final String SELLER_TEMPLATE_VARIANT = "seller";

    public static Optional<PushNotificationTemplate> resolve(String eventType) {
        return resolve(eventType, null);
    }

    public static Optional<PushNotificationTemplate> resolve(
            String eventType,
            String templateVariant,
            String actorDisplayName
    ) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        if (templateVariant == null) {
            if ("POST_CREATED".equals(eventType)) {
                return Optional.of(SocialNotificationTemplatePolicy.postCreatedPush(actorDisplayName));
            }
            if ("USER_AVATAR_UPDATED".equals(eventType)) {
                return Optional.of(SocialNotificationTemplatePolicy.avatarUpdatedPush(actorDisplayName));
            }
        }
        return resolve(eventType, templateVariant);
    }

    public static Optional<PushNotificationTemplate> resolve(String eventType, String templateVariant) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        if ("ORDER_CREATED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Đơn hàng mới",
                    "Bạn có đơn hàng mới."
            ));
        }
        if ("ORDER_COMPLETED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Hoàn tất đơn hàng",
                    "Người mua đã xác nhận nhận hàng."
            ));
        }
        if ("ORDER_CANCELLED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Đơn hàng đã hủy",
                    "Người mua đã hủy đơn hàng."
            ));
        }
        if ("ORDER_CANCELLED".equals(eventType)
                && InAppNotificationTemplatePolicy.ADMIN_CONFIRMED_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Đơn hàng đã hủy",
                    "Đơn hàng đã được hủy sau khi admin xác nhận hoàn tiền."
            ));
        }
        if ("ORDER_CANCEL_PENDING_REFUND".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Yêu cầu hủy đơn",
                    "Người mua đã yêu cầu hủy đơn và chờ hoàn tiền."
            ));
        }
        if ("ORDER_CANCEL_PENDING_REFUND".equals(eventType)
                && InAppNotificationTemplatePolicy.BUYER_COUNTERPARTY_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Chờ hoàn tiền",
                    "Người bán đã hủy đơn hàng. Đơn đang chờ hoàn tiền."
            ));
        }
        if ("PAYOUT_REQUEST_APPROVED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Rút tiền được duyệt",
                    "Yêu cầu rút tiền của bạn đã được duyệt."
            ));
        }
        if ("POST_MODERATED".equals(eventType)
                && InAppNotificationTemplatePolicy.HIDE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Bài viết bị ẩn",
                    "Bài viết của bạn đã bị ẩn do vi phạm chính sách."
            ));
        }
        if ("POST_MODERATED".equals(eventType)
                && InAppNotificationTemplatePolicy.REMOVE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Bài viết bị gỡ",
                    "Bài viết của bạn đã bị gỡ do vi phạm chính sách."
            ));
        }
        if ("COMMENT_MODERATED".equals(eventType)
                && InAppNotificationTemplatePolicy.HIDE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Bình luận bị ẩn",
                    "Bình luận của bạn đã bị ẩn do vi phạm chính sách."
            ));
        }
        if ("COMMENT_MODERATED".equals(eventType)
                && InAppNotificationTemplatePolicy.REMOVE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Bình luận bị gỡ",
                    "Bình luận của bạn đã bị gỡ do vi phạm chính sách."
            ));
        }
        if ("REVIEW_REMOVED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Đánh giá trên sản phẩm bị gỡ",
                    "Một đánh giá trên sản phẩm của bạn đã bị gỡ."
            ));
        }
        if ("REVIEW_RESTORED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new PushNotificationTemplate(
                    "Đánh giá trên sản phẩm được khôi phục",
                    "Một đánh giá trên sản phẩm của bạn đã được khôi phục sau khi rà soát."
            ));
        }
        return Optional.ofNullable(switch (eventType) {
            case "PASSWORD_CHANGED" -> new PushNotificationTemplate(
                    "Đổi mật khẩu",
                    "Mật khẩu tài khoản của bạn vừa được thay đổi."
            );
            case "POST_CREATED" -> new PushNotificationTemplate(
                    "Bài viết mới",
                    "Người bạn đang theo dõi đã đăng bài viết mới."
            );
            case "POST_LIKED" -> new PushNotificationTemplate(
                    "Thích bài viết",
                    "Có người đã thích bài viết của bạn."
            );
            case "USER_FOLLOWED" -> new PushNotificationTemplate(
                    "Người theo dõi mới",
                    "Có người đã bắt đầu theo dõi bạn."
            );
            case "USER_AVATAR_UPDATED" -> new PushNotificationTemplate(
                    "Cập nhật ảnh đại diện",
                    "Người bạn đang theo dõi đã cập nhật ảnh đại diện."
            );
            case "COMMENT_CREATED" -> new PushNotificationTemplate(
                    "Bình luận mới",
                    "Có người đã bình luận bài viết của bạn."
            );
            case "COMMENT_REPLIED" -> new PushNotificationTemplate(
                    "Trả lời bình luận",
                    "Có người đã trả lời bình luận của bạn."
            );
            case "COMMENT_LIKED" -> new PushNotificationTemplate(
                    "Thích bình luận",
                    "Có người đã thích bình luận của bạn."
            );
            case "ORDER_CREATED" -> new PushNotificationTemplate(
                    "Xác nhận đơn hàng",
                    "Đơn hàng của bạn đã được tạo."
            );
            case "PAYMENT_SUCCESS" -> new PushNotificationTemplate(
                    "Thanh toán thành công",
                    "Thanh toán của bạn đã thành công."
            );
            case "PAYMENT_FAILED" -> new PushNotificationTemplate(
                    "Thanh toán thất bại",
                    "Thanh toán của bạn không thể hoàn tất."
            );
            case "PAYMENT_REFUNDED" -> new PushNotificationTemplate(
                    "Hoàn tiền thành công",
                    "Khoản hoàn tiền của bạn đã được xử lý thành công."
            );
            case "ORDER_CANCELLED" -> new PushNotificationTemplate(
                    "Đơn hàng đã hủy",
                    "Người bán đã hủy đơn hàng của bạn."
            );
            case "SHIPMENT_READY_TO_SHIP" -> new PushNotificationTemplate(
                    "Đơn sẵn sàng giao",
                    "Gói hàng của bạn đã sẵn sàng để giao."
            );
            case "SHIPMENT_CANCELLED" -> new PushNotificationTemplate(
                    "Vận đơn đã hủy",
                    "Người bán đã hủy vận đơn."
            );
            case "ORDER_CANCEL_PENDING_REFUND" -> new PushNotificationTemplate(
                    "Chờ hoàn tiền",
                    "Yêu cầu hủy đơn của bạn đã được ghi nhận. Chúng tôi đang xử lý hoàn tiền."
            );
            case "SHIPMENT_SHIPPED" -> new PushNotificationTemplate(
                    "Đơn đang giao",
                    "Đơn hàng của bạn đang được vận chuyển."
            );
            case "SHIPMENT_DELIVERED" -> new PushNotificationTemplate(
                    "Giao hàng thành công",
                    "Đơn hàng của bạn đã được giao."
            );
            case "ORDER_COMPLETED" -> new PushNotificationTemplate(
                    "Hoàn tất đơn hàng",
                    "Đơn hàng của bạn đã hoàn tất."
            );
            case "REVIEW_REMINDER" -> new PushNotificationTemplate(
                    "Nhắc đánh giá",
                    "Hãy cho chúng tôi biết cảm nhận của bạn về đơn hàng."
            );
            case "REVIEW_REPLIED" -> new PushNotificationTemplate(
                    "Shop phản hồi đánh giá",
                    "Cửa hàng đã phản hồi đánh giá sản phẩm của bạn."
            );
            case "USER_SUSPENDED" -> new PushNotificationTemplate(
                    "Tài khoản bị đình chỉ",
                    "Tài khoản của bạn đã bị đình chỉ."
            );
            case "USER_BANNED" -> new PushNotificationTemplate(
                    "Tài khoản bị cấm",
                    "Tài khoản của bạn đã bị cấm."
            );
            case "USER_RESTRICTED" -> new PushNotificationTemplate(
                    "Tài khoản bị hạn chế",
                    "Quyền truy cập tài khoản của bạn đã bị hạn chế."
            );
            case "POST_MODERATED" -> new PushNotificationTemplate(
                    "Bài viết bị kiểm duyệt",
                    "Bài viết của bạn đã bị xử lý do vi phạm chính sách."
            );
            case "COMMENT_MODERATED" -> new PushNotificationTemplate(
                    "Bình luận bị kiểm duyệt",
                    "Bình luận của bạn đã bị xử lý do vi phạm chính sách."
            );
            case "COMMENT_RESTORED" -> new PushNotificationTemplate(
                    "Bình luận được khôi phục",
                    "Bình luận của bạn đã được khôi phục sau khi rà soát."
            );
            case "PRODUCT_REMOVED" -> new PushNotificationTemplate(
                    "Sản phẩm bị gỡ",
                    "Một sản phẩm của bạn đã bị gỡ."
            );
            case "PRODUCT_RESTORED" -> new PushNotificationTemplate(
                    "Sản phẩm được khôi phục",
                    "Một sản phẩm của bạn đã được khôi phục sau khi rà soát."
            );
            case "REVIEW_HIDDEN" -> new PushNotificationTemplate(
                    "Đánh giá bị ẩn",
                    "Một đánh giá của bạn đã bị ẩn."
            );
            case "REVIEW_REMOVED" -> new PushNotificationTemplate(
                    "Đánh giá bị gỡ",
                    "Một đánh giá của bạn đã bị gỡ."
            );
            case "REVIEW_RESTORED" -> new PushNotificationTemplate(
                    "Đánh giá được khôi phục",
                    "Một đánh giá của bạn đã được khôi phục sau khi rà soát."
            );
            case "SHOP_SUSPENDED" -> new PushNotificationTemplate(
                    "Cửa hàng bị đình chỉ",
                    "Cửa hàng của bạn đã bị đình chỉ."
            );
            case "SHOP_CLOSED" -> new PushNotificationTemplate(
                    "Cửa hàng đóng",
                    "Cửa hàng của bạn đã bị đóng."
            );
            case "SHOP_RESTORED" -> new PushNotificationTemplate(
                    "Cửa hàng mở lại",
                    "Cửa hàng của bạn đã được mở lại sau khi rà soát."
            );
            case "USER_ENFORCEMENT_REVOKED" -> new PushNotificationTemplate(
                    "Gỡ hạn chế tài khoản",
                    "Một hạn chế trên tài khoản đã được gỡ."
            );
            case "USER_ENFORCEMENT_EXPIRED" -> new PushNotificationTemplate(
                    "Hết hạn hạn chế",
                    "Hạn chế tạm thời trên tài khoản đã kết thúc."
            );
            case "SYSTEM_ANNOUNCEMENT_SENT" -> new PushNotificationTemplate(
                    "Thông báo hệ thống",
                    "Bạn có thông báo mới từ hệ thống."
            );
            default -> null;
        });
    }
}

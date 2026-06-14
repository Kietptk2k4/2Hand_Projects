package com.twohands.notification_service.domain.inapp;

import com.twohands.notification_service.domain.social.SocialNotificationTemplatePolicy;

import java.util.Optional;

public final class InAppNotificationTemplatePolicy {

    private InAppNotificationTemplatePolicy() {
    }

    public static final String SELLER_TEMPLATE_VARIANT = "seller";
    public static final String BUYER_COUNTERPARTY_TEMPLATE_VARIANT = "buyer_counterparty";
    public static final String HIDE_TEMPLATE_VARIANT = "hide";
    public static final String REMOVE_TEMPLATE_VARIANT = "remove";

    public static Optional<InAppNotificationTemplate> resolve(String eventType) {
        return resolve(eventType, null);
    }

    public static Optional<InAppNotificationTemplate> resolve(
            String eventType,
            String templateVariant,
            String actorDisplayName
    ) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        if (templateVariant == null) {
            if ("POST_CREATED".equals(eventType)) {
                return Optional.of(SocialNotificationTemplatePolicy.postCreatedInApp(actorDisplayName));
            }
            if ("USER_AVATAR_UPDATED".equals(eventType)) {
                return Optional.of(SocialNotificationTemplatePolicy.avatarUpdatedInApp(actorDisplayName));
            }
        }
        return resolve(eventType, templateVariant);
    }

    public static Optional<InAppNotificationTemplate> resolve(String eventType, String templateVariant) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        if ("ORDER_CREATED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Đơn hàng mới",
                    "Bạn có đơn hàng mới."
            ));
        }
        if ("SHIPMENT_CREATED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Tạo vận đơn",
                    "Vận đơn đã được tạo cho đơn hàng bạn đang xử lý."
            ));
        }
        if ("ORDER_COMPLETED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Hoàn tất đơn hàng",
                    "Người mua đã xác nhận nhận hàng."
            ));
        }
        if ("ORDER_CANCELLED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Đơn hàng đã hủy",
                    "Người mua đã hủy đơn hàng."
            ));
        }
        if ("ORDER_CANCEL_PENDING_REFUND".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Yêu cầu hủy đơn",
                    "Người mua đã yêu cầu hủy đơn và chờ hoàn tiền."
            ));
        }
        if ("ORDER_CANCEL_PENDING_REFUND".equals(eventType)
                && BUYER_COUNTERPARTY_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Chờ hoàn tiền",
                    "Người bán đã hủy đơn hàng. Đơn đang chờ hoàn tiền."
            ));
        }
        if ("PAYOUT_REQUEST_APPROVED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Rút tiền được duyệt",
                    "Yêu cầu rút tiền của bạn đã được duyệt."
            ));
        }
        if ("REVIEW_HIDDEN".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Đánh giá trên sản phẩm bị ẩn",
                    "Một đánh giá trên sản phẩm của bạn đã bị ẩn."
            ));
        }
        if ("REVIEW_REMOVED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Đánh giá trên sản phẩm bị gỡ",
                    "Một đánh giá trên sản phẩm của bạn đã bị gỡ."
            ));
        }
        if ("REVIEW_RESTORED".equals(eventType) && SELLER_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Đánh giá trên sản phẩm được khôi phục",
                    "Một đánh giá trên sản phẩm của bạn đã được khôi phục sau khi rà soát."
            ));
        }
        if ("POST_MODERATED".equals(eventType) && HIDE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Bài viết bị ẩn",
                    "Bài viết của bạn đã bị ẩn do vi phạm chính sách."
            ));
        }
        if ("POST_MODERATED".equals(eventType) && REMOVE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Bài viết bị gỡ",
                    "Bài viết của bạn đã bị gỡ do vi phạm chính sách."
            ));
        }
        if ("COMMENT_MODERATED".equals(eventType) && HIDE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Bình luận bị ẩn",
                    "Bình luận của bạn đã bị ẩn do vi phạm chính sách."
            ));
        }
        if ("COMMENT_MODERATED".equals(eventType) && REMOVE_TEMPLATE_VARIANT.equals(templateVariant)) {
            return Optional.of(new InAppNotificationTemplate(
                    "Bình luận bị gỡ",
                    "Bình luận của bạn đã bị gỡ do vi phạm chính sách."
            ));
        }
        return Optional.ofNullable(switch (eventType) {
            case "POST_CREATED" -> new InAppNotificationTemplate(
                    "Bài viết mới",
                    "Người bạn đang theo dõi đã đăng bài viết mới."
            );
            case "POST_LIKED" -> new InAppNotificationTemplate(
                    "Thích bài viết",
                    "Có người đã thích bài viết của bạn."
            );
            case "USER_FOLLOWED" -> new InAppNotificationTemplate(
                    "Người theo dõi mới",
                    "Có người đã bắt đầu theo dõi bạn."
            );
            case "USER_AVATAR_UPDATED" -> new InAppNotificationTemplate(
                    "Cập nhật ảnh đại diện",
                    "Người bạn đang theo dõi đã cập nhật ảnh đại diện."
            );
            case "COMMENT_CREATED" -> new InAppNotificationTemplate(
                    "Bình luận mới",
                    "Có người đã bình luận bài viết của bạn."
            );
            case "COMMENT_REPLIED" -> new InAppNotificationTemplate(
                    "Trả lời bình luận",
                    "Có người đã trả lời bình luận của bạn."
            );
            case "COMMENT_LIKED" -> new InAppNotificationTemplate(
                    "Thích bình luận",
                    "Có người đã thích bình luận của bạn."
            );
            case "ORDER_CREATED" -> new InAppNotificationTemplate(
                    "Xác nhận đơn hàng",
                    "Đơn hàng của bạn đã được đặt thành công."
            );
            case "PAYMENT_SUCCESS" -> new InAppNotificationTemplate(
                    "Thanh toán thành công",
                    "Thanh toán của bạn đã thành công."
            );
            case "PAYMENT_FAILED" -> new InAppNotificationTemplate(
                    "Thanh toán thất bại",
                    "Thanh toán của bạn không thể hoàn tất."
            );
            case "PAYMENT_REFUNDED" -> new InAppNotificationTemplate(
                    "Hoàn tiền thành công",
                    "Khoản hoàn tiền của bạn đã được xử lý thành công."
            );
            case "ORDER_CANCELLED" -> new InAppNotificationTemplate(
                    "Đơn hàng đã hủy",
                    "Người bán đã hủy đơn hàng của bạn."
            );
            case "SHIPMENT_CREATED" -> new InAppNotificationTemplate(
                    "Tạo vận đơn",
                    "Vận đơn đã được tạo cho đơn hàng của bạn."
            );
            case "SHIPMENT_READY_TO_SHIP" -> new InAppNotificationTemplate(
                    "Đơn sẵn sàng giao",
                    "Gói hàng của bạn đã sẵn sàng để giao."
            );
            case "SHIPMENT_CANCELLED" -> new InAppNotificationTemplate(
                    "Vận đơn đã hủy",
                    "Người bán đã hủy vận đơn. Hệ thống có thể tạo vận đơn mới cho đơn hàng của bạn."
            );
            case "ORDER_CANCEL_PENDING_REFUND" -> new InAppNotificationTemplate(
                    "Chờ hoàn tiền",
                    "Yêu cầu hủy đơn của bạn đã được ghi nhận. Chúng tôi đang xử lý hoàn tiền."
            );
            case "SHIPMENT_SHIPPED" -> new InAppNotificationTemplate(
                    "Đơn đang giao",
                    "Đơn hàng của bạn đang được vận chuyển."
            );
            case "SHIPMENT_DELIVERED" -> new InAppNotificationTemplate(
                    "Giao hàng thành công",
                    "Đơn hàng của bạn đã được giao."
            );
            case "ORDER_COMPLETED" -> new InAppNotificationTemplate(
                    "Hoàn tất đơn hàng",
                    "Đơn hàng của bạn đã hoàn tất."
            );
            case "REVIEW_REMINDER" -> new InAppNotificationTemplate(
                    "Nhắc đánh giá",
                    "Hãy chia sẻ trải nghiệm về sản phẩm từ đơn hàng gần đây của bạn."
            );
            case "REVIEW_REPLIED" -> new InAppNotificationTemplate(
                    "Shop phản hồi đánh giá",
                    "Cửa hàng đã phản hồi đánh giá sản phẩm của bạn."
            );
            case "USER_SUSPENDED" -> new InAppNotificationTemplate(
                    "Tài khoản bị đình chỉ",
                    "Tài khoản của bạn đã bị đình chỉ."
            );
            case "USER_BANNED" -> new InAppNotificationTemplate(
                    "Tài khoản bị cấm",
                    "Tài khoản của bạn đã bị cấm."
            );
            case "USER_RESTRICTED" -> new InAppNotificationTemplate(
                    "Tài khoản bị hạn chế",
                    "Một số tính năng trên tài khoản của bạn đã bị hạn chế."
            );
            case "POST_MODERATED" -> new InAppNotificationTemplate(
                    "Bài viết bị kiểm duyệt",
                    "Bài viết của bạn đã bị xử lý do vi phạm chính sách."
            );
            case "COMMENT_MODERATED" -> new InAppNotificationTemplate(
                    "Bình luận bị kiểm duyệt",
                    "Bình luận của bạn đã bị xử lý do vi phạm chính sách."
            );
            case "COMMENT_RESTORED" -> new InAppNotificationTemplate(
                    "Bình luận được khôi phục",
                    "Bình luận của bạn đã được khôi phục sau khi rà soát."
            );
            case "PRODUCT_REMOVED" -> new InAppNotificationTemplate(
                    "Sản phẩm bị gỡ",
                    "Một sản phẩm của bạn đã bị gỡ."
            );
            case "PRODUCT_RESTORED" -> new InAppNotificationTemplate(
                    "Sản phẩm được khôi phục",
                    "Một sản phẩm của bạn đã được khôi phục sau khi rà soát."
            );
            case "REVIEW_HIDDEN" -> new InAppNotificationTemplate(
                    "Đánh giá bị ẩn",
                    "Một đánh giá của bạn đã bị ẩn."
            );
            case "REVIEW_REMOVED" -> new InAppNotificationTemplate(
                    "Đánh giá bị gỡ",
                    "Một đánh giá của bạn đã bị gỡ."
            );
            case "REVIEW_RESTORED" -> new InAppNotificationTemplate(
                    "Đánh giá được khôi phục",
                    "Một đánh giá của bạn đã được khôi phục sau khi rà soát."
            );
            case "SHOP_SUSPENDED" -> new InAppNotificationTemplate(
                    "Cửa hàng bị đình chỉ",
                    "Cửa hàng của bạn đã bị đình chỉ."
            );
            case "SHOP_CLOSED" -> new InAppNotificationTemplate(
                    "Cửa hàng đóng",
                    "Cửa hàng của bạn đã bị đóng."
            );
            case "SHOP_RESTORED" -> new InAppNotificationTemplate(
                    "Cửa hàng mở lại",
                    "Cửa hàng của bạn đã được mở lại sau khi rà soát."
            );
            case "USER_ENFORCEMENT_REVOKED" -> new InAppNotificationTemplate(
                    "Gỡ hạn chế tài khoản",
                    "Một hạn chế trên tài khoản của bạn đã được gỡ."
            );
            case "USER_ENFORCEMENT_EXPIRED" -> new InAppNotificationTemplate(
                    "Hết hạn hạn chế",
                    "Hạn chế tạm thời trên tài khoản của bạn đã kết thúc."
            );
            default -> null;
        });
    }
}

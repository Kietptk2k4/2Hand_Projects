package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.SellerRevenueBucket;

import java.math.BigDecimal;

public record SellerRevenueBucketResponse(
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("item_count") long itemCount
) {
    public static SellerRevenueBucketResponse from(SellerRevenueBucket bucket) {
        return new SellerRevenueBucketResponse(bucket.amount(), bucket.itemCount());
    }
}

package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.seller.SellerRevenueBucketResponse;
import com.twohands.commerce_service.domain.finance.PlatformCodPipeline;

public record PlatformCodPipelineResponse(
        @JsonProperty("in_transit") SellerRevenueBucketResponse inTransit,
        @JsonProperty("pending_confirm") SellerRevenueBucketResponse pendingConfirm,
        @JsonProperty("recognized") SellerRevenueBucketResponse recognized
) {
    public static PlatformCodPipelineResponse from(PlatformCodPipeline pipeline) {
        return new PlatformCodPipelineResponse(
                SellerRevenueBucketResponse.from(pipeline.inTransit()),
                SellerRevenueBucketResponse.from(pipeline.pendingConfirm()),
                SellerRevenueBucketResponse.from(pipeline.recognized())
        );
    }
}

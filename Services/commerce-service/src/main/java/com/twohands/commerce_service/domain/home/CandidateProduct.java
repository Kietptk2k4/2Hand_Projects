package com.twohands.commerce_service.domain.home;

import java.util.EnumSet;

public record CandidateProduct(
        HomeProductSnapshot product,
        EnumSet<RetrievalSource> sources,
        Double personalScore,
        Double cfScore,
        Double arScore
) {
}

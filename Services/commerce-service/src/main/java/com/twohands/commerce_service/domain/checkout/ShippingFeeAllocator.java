package com.twohands.commerce_service.domain.checkout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class ShippingFeeAllocator {

    private static final int MONEY_SCALE = 0;

    private ShippingFeeAllocator() {
    }

    public static List<BigDecimal> allocateProportionally(
            BigDecimal groupShippingFee,
            List<BigDecimal> itemTotals
    ) {
        if (itemTotals.isEmpty()) {
            return List.of();
        }
        if (groupShippingFee.compareTo(BigDecimal.ZERO) == 0) {
            return itemTotals.stream().map(ignored -> BigDecimal.ZERO).toList();
        }

        BigDecimal groupSubtotal = itemTotals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (groupSubtotal.compareTo(BigDecimal.ZERO) == 0) {
            return splitEvenly(groupShippingFee, itemTotals.size());
        }

        List<BigDecimal> allocations = new ArrayList<>(itemTotals.size());
        BigDecimal allocatedSum = BigDecimal.ZERO;

        for (int i = 0; i < itemTotals.size() - 1; i++) {
            BigDecimal share = groupShippingFee
                    .multiply(itemTotals.get(i))
                    .divide(groupSubtotal, MONEY_SCALE, RoundingMode.HALF_UP);
            allocations.add(share);
            allocatedSum = allocatedSum.add(share);
        }

        BigDecimal lastShare = groupShippingFee.subtract(allocatedSum);
        if (lastShare.compareTo(BigDecimal.ZERO) < 0) {
            lastShare = BigDecimal.ZERO;
        }
        allocations.add(lastShare);
        return allocations;
    }

    private static List<BigDecimal> splitEvenly(BigDecimal groupShippingFee, int count) {
        BigDecimal base = groupShippingFee.divide(BigDecimal.valueOf(count), MONEY_SCALE, RoundingMode.HALF_UP);
        List<BigDecimal> allocations = new ArrayList<>(count);
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < count - 1; i++) {
            allocations.add(base);
            sum = sum.add(base);
        }
        allocations.add(groupShippingFee.subtract(sum));
        return allocations;
    }
}

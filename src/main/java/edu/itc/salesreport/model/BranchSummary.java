package main.java.edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record BranchSummary (String branch, BigDecimal revenue,
        BigDecimal discounts, long receipts,
        Map<String, BigDecimal> revenueByCategory,
        List<ProductTotal> topProducts,
        Map<PaymentMethod, BigDecimal> revenueByPayment){
    
    public BranchSummary {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(revenue, "revenue");
        Objects.requireNonNull(discounts, "discounts");
        revenueByCategory = Map.copyOf(revenueByCategory);   // unmodifiable copies:
        topProducts = List.copyOf(topProducts);              // callers cannot change
        revenueByPayment = Map.copyOf(revenueByPayment);     // our state afterwards
    }

    public BigDecimal averageBasket() {
        if (receipts == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return revenue.divide(BigDecimal.valueOf(receipts), 2, RoundingMode.HALF_UP);
    }
}

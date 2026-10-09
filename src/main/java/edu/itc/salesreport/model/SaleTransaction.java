package main.java.edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public record SaleTransaction (String branch, LocalDate date,
        String receiptNo, String sku, String productName,
        String category, int quantity, BigDecimal unitPrice,
        BigDecimal discount, PaymentMethod paymentMethod){
    
    public SaleTransaction {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(receiptNo, "RecieptNo");
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(productName, "productName");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(unitPrice, "unitPrice");
        Objects.requireNonNull(discount, "discount");
        Objects.requireNonNull(paymentMethod, "paymentMethod");

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unit_price must be >= 0");
        }
        if (discount.signum() < 0) {
            throw new IllegalArgumentException("discount must be >= 0");
        }
        if (discount.compareTo(lineTotal(quantity, unitPrice)) > 0) {
            throw new IllegalArgumentException(
                    "discount must not exceed quantity x unit_price");
        }
    }

    private static BigDecimal lineTotal(int quantity, BigDecimal unitPrice) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** quantity x unit price - discount, scale 2, HALF_UP. */
    public BigDecimal revenue() {
        return lineTotal(quantity, unitPrice)
                .subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

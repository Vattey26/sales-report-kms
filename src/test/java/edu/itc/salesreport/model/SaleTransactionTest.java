package test.java.edu.itc.salesreport.model;



public class SaleTransactionTest {
    
private static SaleTransaction sale(int qty, String price, String discount) {
        return new SaleTransaction("PNH", LocalDate.of(2026, 9, 1), "PNH-000001",
                "SKU-1", "Test item", "Grocery", qty,
                new BigDecimal(price), new BigDecimal(discount), PaymentMethod.CASH);
    }

    @Test
    void revenueRoundsHalfUp() {
        // 3 x 0.335 = 1.005 -> 1.01 (HALF_UP); a double would give 1.00
        assertEquals(new BigDecimal("1.01"), sale(3, "0.335", "0.00").revenue());
    }

    @Test
    void revenueSubtractsDiscountAndKeepsScaleTwo() {
        assertEquals(new BigDecimal("12.90"), sale(2, "6.50", "0.10").revenue());
    }

    @Test
    void constructorRejectsBrokenInvariants() {
        assertThrows(IllegalArgumentException.class, () -> sale(0, "1.00", "0.00"));
        assertThrows(IllegalArgumentException.class, () -> sale(1, "-1.00", "0.00"));
        assertThrows(IllegalArgumentException.class, () -> sale(1, "1.00", "-0.01"));
        assertThrows(IllegalArgumentException.class, () -> sale(1, "1.00", "1.01"));
        assertThrows(NullPointerException.class, () -> new SaleTransaction(null,
                LocalDate.now(), "r", "s", "p", "Grocery", 1,
                BigDecimal.ONE, BigDecimal.ZERO, PaymentMethod.CASH));
    }

    @Test
    void discountEqualToLineTotalIsAllowed() {
        assertEquals(new BigDecimal("0.00"), sale(2, "6.50", "13.00").revenue());
    }

    private static BranchSummary summary(long receipts, List<ProductTotal> top) {
        return new BranchSummary("PNH", new BigDecimal("100.00"), BigDecimal.ZERO,
                receipts, Map.of(), top, Map.of());
    }

    @Test
    void topProductsIsUnmodifiable() {
        var p = new ProductTotal("SKU-1", "Rice", new BigDecimal("10.00"), 2);
        var s = summary(4, List.of(p));
        assertThrows(UnsupportedOperationException.class, () -> s.topProducts().add(p));
    }

    @Test
    void summaryCopiesItsInputList() {
        var p = new ProductTotal("SKU-1", "Rice", new BigDecimal("10.00"), 2);
        var source = new ArrayList<>(List.of(p));
        var s = summary(4, source);
        source.clear();
        assertEquals(1, s.topProducts().size());
    }

    @Test
    void averageBasket() {
        assertEquals(new BigDecimal("25.00"), summary(4, List.of()).averageBasket());
        assertEquals(new BigDecimal("0.00"), summary(0, List.of()).averageBasket());
        assertEquals(new BigDecimal("33.33"), summary(3, List.of()).averageBasket());
    }
}
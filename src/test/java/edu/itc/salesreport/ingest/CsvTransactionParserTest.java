package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CsvTransactionParserTest {

    private final TransactionParser parser = new CsvTransactionParser();

    @Test
    void parsesAValidRow() throws InvalidRowException {
        var t = parser.parse("PNH,2026-09-01,PNH-000123,"
                + "SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR");
        assertEquals("PNH", t.branch());
        assertEquals(LocalDate.of(2026, 9, 1), t.date());
        assertEquals("PNH-000123", t.receiptNo());
        assertEquals("SKU-1001", t.sku());
        assertEquals("Jasmine Rice 5kg", t.productName());
        assertEquals("Grocery", t.category());
        assertEquals(2, t.quantity());
        assertEquals(new BigDecimal("6.50"), t.unitPrice());
        assertEquals(new BigDecimal("0.00"), t.discount());
        assertEquals(PaymentMethod.KHQR, t.paymentMethod());
        assertEquals(new BigDecimal("13.00"), t.revenue());
    }

    @Test
    void parsesARowWithADiscount() throws InvalidRowException {
        var t = parser.parse("REP,2026-09-02,REP-000007,"
                + "SKU-5001,USB-C Cable 1m,Electronics,2,4.50,0.10,CARD");
        assertEquals(new BigDecimal("0.10"), t.discount());
        assertEquals(new BigDecimal("8.90"), t.revenue());   // 9.00 - 0.10
    }

    @Test
    void parsesACategoryWithASpace() throws InvalidRowException {
        var t = parser.parse("BTB,2026-09-03,BTB-000001,"
                + "SKU-4002,Shampoo 400ml,Personal Care,1,3.40,0.00,CASH");
        assertEquals("Personal Care", t.category());
    }

    static Stream<Arguments> invalidRows() {
        return Stream.of(
            Arguments.of("", "empty"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00",
                    "10 fields"),
            Arguments.of("XYZ,2026-09-01,XYZ-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH",
                    "branch"),
            Arguments.of("PNH,2026-09-31,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH",
                    "date"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Toys,2,6.50,0.00,CASH",
                    "category"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,two,6.50,0.00,CASH",
                    "quantity"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,0,6.50,0.00,CASH",
                    "quantity"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,abc,0.00,CASH",
                    "unit_price"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,-0.10,CASH",
                    "discount"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,20.00,CASH",
                    "discount"),
            Arguments.of("PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,PAYPAL",
                    "payment_method"),
            Arguments.of("PNH,2026-09-01,,SKU-1,Rice,Grocery,2,6.50,0.00,CASH",
                    "receipt_no"));
    }

    @ParameterizedTest
    @MethodSource("invalidRows")
    void rejectsInvalidRowsAndNamesTheField(String line, String fieldName) {
        var e = assertThrows(InvalidRowException.class, () -> parser.parse(line));
        assertTrue(e.getMessage().contains(fieldName),
                "message '" + e.getMessage() + "' should mention " + fieldName);
    }

    @Test
    void rejectsNull() {
        assertThrows(InvalidRowException.class, () -> parser.parse(null));
    }
}

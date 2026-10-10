package edu.itc.salesreport.render;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.ProductTotal;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

final class TestReports {

    private TestReports() { }

    static BranchSummary branch(String code, String revenue, long receipts) {
        var rev = new BigDecimal(revenue);
        return new BranchSummary(code, rev, new BigDecimal("0.00"),
                receipts, Map.of("Grocery", rev),
                List.of(new ProductTotal("SKU-1001", "Jasmine Rice 5kg",
                        rev, receipts)),
                Map.of(PaymentMethod.KHQR, rev));
    }

    static MonthlyReport september() {
        return new MonthlyReport(YearMonth.of(2026, 9),
                List.of(branch("PNH", "1200.00", 100),
                        branch("REP", "800.50", 50),
                        branch("BTB", "300.00", 40)),
                branch("ALL", "2300.50", 190),
                List.of());
    }
}

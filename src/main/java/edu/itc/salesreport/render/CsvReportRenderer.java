package edu.itc.salesreport.render;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;
import java.nio.charset.StandardCharsets;

public final class CsvReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "csv";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder();
        sb.append("month,branch,revenue,discounts,receipts,average_basket\n");
        for (BranchSummary b : report.branches()) {
            line(sb, report.month().toString(), b);
        }
        line(sb, report.month().toString(), report.chain());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void line(StringBuilder sb, String month, BranchSummary b) {
        sb.append(month).append(',')
          .append(b.branch()).append(',')
          .append(b.revenue()).append(',')
          .append(b.discounts()).append(',')
          .append(b.receipts()).append(',')
          .append(b.averageBasket()).append('\n');
    }
}

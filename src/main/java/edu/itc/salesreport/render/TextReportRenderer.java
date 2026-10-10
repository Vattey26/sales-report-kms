package edu.itc.salesreport.render;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;
import java.nio.charset.StandardCharsets;

public final class TextReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "txt";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder();
        sb.append("Angkor Mart monthly sales ")
          .append(report.month()).append('\n');
        sb.append(String.format("%-6s %12s %9s %11s%n",
                "Branch", "Revenue", "Receipts", "Avg basket"));
        for (BranchSummary b : report.branches()) {
            line(sb, b);
        }
        line(sb, report.chain());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void line(StringBuilder sb, BranchSummary b) {
        sb.append(String.format("%-6s %12s %9d %11s%n", b.branch(),
                b.revenue(), b.receipts(), b.averageBasket()));
    }
}

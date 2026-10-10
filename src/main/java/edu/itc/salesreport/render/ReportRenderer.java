package edu.itc.salesreport.render;

import edu.itc.salesreport.model.MonthlyReport;

/** Strategy: one way of turning a report into a file. */
public sealed interface ReportRenderer
        permits TextReportRenderer, HtmlReportRenderer, CsvReportRenderer {

    /** File extension without the dot, for example "html". */
    String extension();

    /** Bytes, because PDF and XLSX (Lab-08) are binary formats. */
    byte[] render(MonthlyReport report);

    /** Simple factory: picks the strategy for a file extension. */
    static ReportRenderer forExtension(String ext) {
        return switch (ext) {
            case "txt" -> new TextReportRenderer();
            case "html" -> new HtmlReportRenderer();
            case "csv" -> new CsvReportRenderer();
            default -> throw new IllegalArgumentException(
                    "no renderer for ." + ext);
        };
    }
}

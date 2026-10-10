package edu.itc.salesreport.render;

import edu.itc.salesreport.model.MonthlyReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportRendererTest {

    @ParameterizedTest
    @ValueSource(strings = {"txt", "html", "csv"})
    void everyBranchCodeAndChainTotalAppear(String ext) {
        var renderer = ReportRenderer.forExtension(ext);
        var report = TestReports.september();
        var output = new String(renderer.render(report), StandardCharsets.UTF_8);

        assertTrue(output.contains("PNH"), "output should contain branch PNH");
        assertTrue(output.contains("REP"), "output should contain branch REP");
        assertTrue(output.contains("BTB"), "output should contain branch BTB");
        assertTrue(output.contains("2300.50"), "output should contain chain total 2300.50");
    }

    @Test
    void csvContainsExactBranchLine() {
        var renderer = ReportRenderer.forExtension("csv");
        var report = TestReports.september();
        var output = new String(renderer.render(report), StandardCharsets.UTF_8);

        assertTrue(output.contains("2026-09,REP,800.50,0.00,50,16.01"),
                "CSV output should contain the exact row for branch REP");
    }

    @Test
    void forExtensionThrowsOnPdf() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> ReportRenderer.forExtension("pdf"));
        assertTrue(exception.getMessage().contains("pdf"));
    }

    @Test
    void exhaustiveSwitchWithoutDefault() {
        for (String ext : List.of("txt", "html", "csv")) {
            ReportRenderer renderer = ReportRenderer.forExtension(ext);
            String matched = switch (renderer) {
                case TextReportRenderer t -> "txt";
                case HtmlReportRenderer h -> "html";
                case CsvReportRenderer c -> "csv";
            };
            assertEquals(ext, matched);
        }
    }

    @Test
    void htmlEscapesSpecialCharacters() {
        var report = new MonthlyReport(
                YearMonth.of(2026, 9),
                List.of(TestReports.branch("<AT&T>", "100.00", 10)),
                TestReports.branch("ALL", "100.00", 10),
                List.of());
        var html = new String(new HtmlReportRenderer().render(report), StandardCharsets.UTF_8);

        assertTrue(html.contains("&lt;AT&amp;T&gt;"), "should escape special characters &, <, >");
        assertFalse(html.contains("<AT&T>"), "raw unescaped tags should not be present");
    }
}

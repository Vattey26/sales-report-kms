package edu.itc.salesreport.model;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public record MonthlyReport(YearMonth month, List<BranchSummary> branches,
        BranchSummary chain, List<String> anomalies) {

    public MonthlyReport {
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(chain, "chain");
        branches = List.copyOf(branches);
        anomalies = List.copyOf(anomalies);
    }
}

package edu.itc.salesreport.ingest;

@FunctionalInterface
public interface LoadListener {
    void onEvent(LoadEvent event);
}

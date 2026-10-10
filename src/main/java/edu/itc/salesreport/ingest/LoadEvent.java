package edu.itc.salesreport.ingest;

import java.nio.file.Path;
import java.time.Duration;

/** What a MonthLoader tells its observers. */
public sealed interface LoadEvent {

    record FileLoaded(Path file, int accepted, int rejected,
            int filesDone, int filesTotal) implements LoadEvent { }

    record LoadFinished(int accepted, int rejected, Duration took)
            implements LoadEvent { }
}

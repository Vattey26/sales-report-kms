package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/** MonthLoader: the subject */
public final class MonthLoader {

    private final TransactionParser parser;
    private final List<LoadListener> listeners =
            new CopyOnWriteArrayList<>();
    private final List<String> rejectedRows = new ArrayList<>();

    public MonthLoader(TransactionParser parser) {
        this.parser = parser;
    }

    /** Registers a listener; the returned handle unsubscribes it. */
    public Runnable subscribe(LoadListener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public List<String> rejectedRows() {
        return List.copyOf(rejectedRows);
    }

    public List<SaleTransaction> load(YearMonth month, Path dataDir) {
        rejectedRows.clear();
        Instant start = Instant.now();
        Path monthDir = dataDir.resolve(month.toString());

        List<Path> files;
        if (!Files.exists(monthDir)) {
            files = List.of();
        } else {
            try (Stream<Path> stream = Files.list(monthDir)) {
                files = stream
                        .filter(p -> p.getFileName().toString().endsWith(".csv"))
                        .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                        .toList();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        List<SaleTransaction> transactions = new ArrayList<>();
        int filesTotal = files.size();
        int filesDone = 0;
        int totalAccepted = 0;
        int totalRejected = 0;

        for (Path file : files) {
            filesDone++;
            int acceptedInFile = 0;
            int rejectedInFile = 0;

            List<String> lines;
            try {
                lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            for (int i = 1; i < lines.size(); i++) {
                int lineNum = i + 1;
                String line = lines.get(i);
                try {
                    SaleTransaction tx = parser.parse(line);
                    transactions.add(tx);
                    acceptedInFile++;
                } catch (InvalidRowException e) {
                    rejectedInFile++;
                    rejectedRows.add(file.getFileName().toString() + ":" + lineNum + " " + e.getMessage());
                }
            }

            totalAccepted += acceptedInFile;
            totalRejected += rejectedInFile;
            publish(new LoadEvent.FileLoaded(file, acceptedInFile, rejectedInFile, filesDone, filesTotal));
        }

        Duration took = Duration.between(start, Instant.now());
        publish(new LoadEvent.LoadFinished(totalAccepted, totalRejected, took));
        return List.copyOf(transactions);
    }

    private void publish(LoadEvent event) {
        listeners.forEach(l -> l.onEvent(event));
    }
}

package edu.itc.salesreport.ingest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonthLoaderTest {

    private static final String HEADER = "branch,date,receipt_no,sku,product_name,category,quantity,unit_price,discount,payment_method";

    @Test
    void producesThreeEventsInOrderWithCorrectCounts(@TempDir Path tempDir) throws IOException {
        var month = YearMonth.of(2026, 9);
        var monthDir = Files.createDirectories(tempDir.resolve(month.toString()));

        var file1 = monthDir.resolve("BTB-2026-09-01.csv");
        Files.write(file1, List.of(
                HEADER,
                "BTB,2026-09-01,BTB-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,CASH",
                "BTB,2026-09-01,BTB-000002,SKU-1002,Fish Sauce 700ml,Grocery,1,1.80,0.00,KHQR"
        ));

        var file2 = monthDir.resolve("REP-2026-09-02.csv");
        Files.write(file2, List.of(
                HEADER,
                "REP,2026-09-02,REP-000001,SKU-2001,Mineral Water 1.5L,Beverages,3,0.45,0.00,CARD",
                "REP,2026-09-02,REP-000002,SKU-2002,Iced Coffee Can,Beverages,two,0.90,0.00,CASH" // invalid quantity
        ));

        var loader = new MonthLoader(new CsvTransactionParser());
        var events = new ArrayList<LoadEvent>();
        loader.subscribe(events::add);

        var transactions = loader.load(month, tempDir);

        assertEquals(3, transactions.size());
        assertEquals(3, events.size());

        // Event 1: file1 loaded
        assertInstanceOf(LoadEvent.FileLoaded.class, events.get(0));
        var fl1 = (LoadEvent.FileLoaded) events.get(0);
        assertEquals(file1, fl1.file());
        assertEquals(2, fl1.accepted());
        assertEquals(0, fl1.rejected());
        assertEquals(1, fl1.filesDone());
        assertEquals(2, fl1.filesTotal());

        // Event 2: file2 loaded
        assertInstanceOf(LoadEvent.FileLoaded.class, events.get(1));
        var fl2 = (LoadEvent.FileLoaded) events.get(1);
        assertEquals(file2, fl2.file());
        assertEquals(1, fl2.accepted());
        assertEquals(1, fl2.rejected());
        assertEquals(2, fl2.filesDone());
        assertEquals(2, fl2.filesTotal());

        // Event 3: finished
        assertInstanceOf(LoadEvent.LoadFinished.class, events.get(2));
        var finished = (LoadEvent.LoadFinished) events.get(2);
        assertEquals(3, finished.accepted());
        assertEquals(1, finished.rejected());
        assertNotNull(finished.took());

        // Check rejected rows
        assertEquals(1, loader.rejectedRows().size());
        assertTrue(loader.rejectedRows().getFirst().contains("REP-2026-09-02.csv:3 bad quantity 'two'"));
    }

    @Test
    void unsubscribedListenerReceivesNothing(@TempDir Path tempDir) throws IOException {
        var month = YearMonth.of(2026, 9);
        var monthDir = Files.createDirectories(tempDir.resolve(month.toString()));
        var file = monthDir.resolve("BTB-2026-09-01.csv");
        Files.write(file, List.of(
                HEADER,
                "BTB,2026-09-01,BTB-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,CASH"
        ));

        var loader = new MonthLoader(new CsvTransactionParser());
        var events = new ArrayList<LoadEvent>();
        Runnable unsubscribe = loader.subscribe(events::add);
        unsubscribe.run();

        loader.load(month, tempDir);

        assertTrue(events.isEmpty(), "Unsubscribed listener should receive zero events");
    }
}

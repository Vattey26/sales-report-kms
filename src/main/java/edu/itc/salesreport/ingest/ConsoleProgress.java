package edu.itc.salesreport.ingest;

public final class ConsoleProgress implements LoadListener {
    @Override
    public void onEvent(LoadEvent event) {
        switch (event) {
            case LoadEvent.FileLoaded(var file, var ok, var bad,
                    var done, var total) ->
                System.out.printf("[%d/%d] %s  accepted %d  rejected %d%n",
                        done, total, file.getFileName(), ok, bad);
            case LoadEvent.LoadFinished(var ok, var bad, var took) ->
                System.out.printf("Loaded %d rows, rejected %d%n", ok, bad);
        }
    }
}

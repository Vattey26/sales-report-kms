package edu.itc.salesreport;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyRulesTest {

    private static final Path SRC =
            Path.of("src/main/java/edu/itc/salesreport");

    private static List<String> importsOf(String pkg) throws IOException {
        try (Stream<Path> files = Files.walk(SRC.resolve(pkg))) {
            return files.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(DependencyRulesTest::lines)
                    .filter(l -> l.startsWith("import edu.itc.salesreport."))
                    .toList();
        }
    }

    private static Stream<String> lines(Path p) {
        try {
            return Files.readAllLines(p).stream().map(String::strip);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void modelImportsNoOtherProjectPackage() throws IOException {
        var offending = importsOf("model").stream()
                .filter(l -> !l.startsWith("import edu.itc.salesreport.model."))
                .toList();
        assertEquals(List.of(), offending);
    }

    @Test
    void ingestDoesNotKnowRendering() throws IOException {
        var offending = importsOf("ingest").stream()
                .filter(l -> l.contains(".render."))
                .toList();
        assertEquals(List.of(), offending);
    }
}

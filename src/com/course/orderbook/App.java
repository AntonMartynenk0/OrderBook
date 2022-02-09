package com.course.orderbook;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.*;

import static java.lang.System.*;
import static java.nio.file.Files.*;

public class App {
    public static void main(String[] args) {
        var processor = new Processor();
        // for file processing (stream)
        try (var writer = newBufferedWriter(Path.of("output.txt"))) {
            processor.process(
                    Files.lines(Path.of("input.txt")),
                    consume(writer));
        } catch (IOException e) {
            out.println(e.getMessage());
        }
    }

    static Consumer<String> consume(BufferedWriter writer) {
        return s -> {
            try {
                writer.write(s);
                writer.newLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            }
        };
    }
}

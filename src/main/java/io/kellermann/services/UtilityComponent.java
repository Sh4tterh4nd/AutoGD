package io.kellermann.services;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@Component
public class UtilityComponent {

    public void clearDirectory(Path toClear){
        try (Stream<Path> pathStream = Files.walk(toClear)) {
            pathStream
                    .filter(s->!s.equals(toClear))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

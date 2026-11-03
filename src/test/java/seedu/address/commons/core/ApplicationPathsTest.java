package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies real packaged code locations, including URI escaping and FileHandler pattern characters. */
public class ApplicationPathsTest {
    @TempDir
    public Path directory;

    @Test
    public void unpackagedClasses_useDevelopmentWorkingDirectory() {
        assertEquals(Path.of("").toAbsolutePath(), ApplicationPaths.getHomeDirectory());
    }

    @Test
    public void packagedLogger_differentWorkingDirectory_writesOnlyBesideJar() throws Exception {
        Path home = Files.createDirectory(directory.resolve("TutorTrack space %t"));
        Path elsewhere = Files.createDirectory(directory.resolve("launch directory"));
        Path jar = home.resolve("TutorTrack.jar");
        try (var archive = new JarOutputStream(Files.newOutputStream(jar))) {
            for (Class<?> type : List.of(ApplicationPaths.class, LogsCenter.class, LogsCenterTest.LoggingProbe.class)) {
                String entry = type.getName().replace('.', '/') + ".class";
                archive.putNextEntry(new JarEntry(entry));
                try (var source = type.getClassLoader().getResourceAsStream(entry)) {
                    source.transferTo(archive);
                }
                archive.closeEntry();
            }
        }
        Path java = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java");
        Path output = elsewhere.resolve("probe.txt");
        Process process = new ProcessBuilder(java.toString(), "-cp", jar.toString(),
                LogsCenterTest.LoggingProbe.class.getName()).directory(elsewhere.toFile())
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Packaged logger probe did not terminate");
            assertEquals(0, process.exitValue(), Files.readString(output));
            assertTrue(Files.isRegularFile(home.resolve("addressbook.log.0")));
            assertTrue(Files.readString(home.resolve("addressbook.log.0")).contains("LOGGER_READY"));
            assertFalse(Files.exists(elsewhere.resolve("addressbook.log.0")));
        } finally {
            process.destroyForcibly();
        }
    }
}

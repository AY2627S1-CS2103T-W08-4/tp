package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Uses fresh processes so static logger initialization is exercised in each environment. */
public class LogsCenterTest {
    @TempDir
    public Path directory;

    @Test
    public void initialize_writableDirectory_createsLogAndKeepsConsoleLogging() throws Exception {
        String output = runProbe();
        assertTrue(output.contains("LOGGER_READY"), output);
        assertTrue(Files.isRegularFile(directory.resolve("addressbook.log.0")));
    }

    @Test
    public void initialize_blockedLogFile_keepsConsoleLoggingWithoutCrashing() throws Exception {
        Files.createDirectory(directory.resolve("addressbook.log.0"));
        String output = runProbe();
        assertTrue(output.contains("Error adding file handler for logger."), output);
        assertTrue(output.contains("LOGGER_READY"), output);
        assertTrue(Files.isDirectory(directory.resolve("addressbook.log.0")));
    }

    private String runProbe() throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java");
        String classPath = Path.of(LogsCenter.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(LogsCenterTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path output = directory.resolve("probe-output.txt");
        Process process = new ProcessBuilder(java.toString(), "-cp", classPath, LoggingProbe.class.getName())
                .directory(directory.toFile()).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Logger probe did not terminate");
            String text = Files.readString(output);
            assertEquals(0, process.exitValue(), text);
            return text;
        } finally {
            process.destroyForcibly();
        }
    }

    public static class LoggingProbe {
        public static void main(String[] args) {
            LogsCenter.getLogger("audit").info("LOGGER_READY");
        }
    }
}

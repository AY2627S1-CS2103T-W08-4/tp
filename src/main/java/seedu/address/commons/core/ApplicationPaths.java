package seedu.address.commons.core;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Locates application-owned files beside the packaged JAR, independently of the launch directory. */
public final class ApplicationPaths {
    private static final Path HOME_DIRECTORY = locateHomeDirectory();

    private ApplicationPaths() {}

    /** Returns the JAR's directory, or the working directory when running unpackaged development classes. */
    public static Path getHomeDirectory() {
        return HOME_DIRECTORY;
    }

    private static Path locateHomeDirectory() {
        try {
            Path source = Path.of(ApplicationPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isRegularFile(source) ? source.toAbsolutePath().getParent() : Path.of("").toAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to determine the TutorTrack home folder.", e);
        }
    }
}

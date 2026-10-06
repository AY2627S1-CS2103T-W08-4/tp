package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a student's Telegram handle in TutorTrack's supported format.
 * Guarantees: immutable; stored in lowercase without surrounding ordinary spaces.
 */
public final class TelegramHandle {

    public static final String MESSAGE_CONSTRAINTS = "Telegram handle must start with @, followed by "
            + "5-32 letters, digits or underscores, starting with a letter.";
    private static final String VALIDATION_REGEX = "@[A-Za-z][A-Za-z0-9_]{4,31}";

    public final String value;

    /**
     * Constructs a {@code TelegramHandle} after removing surrounding ordinary spaces.
     *
     * @param handle A handle in the supported format, optionally surrounded by ordinary spaces.
     * @throws NullPointerException If {@code handle} is null.
     * @throws IllegalArgumentException If {@code handle} is invalid.
     */
    public TelegramHandle(String handle) {
        requireNonNull(handle);
        String trimmedHandle = trimSpaces(handle);
        checkArgument(isValidTelegramHandle(trimmedHandle), MESSAGE_CONSTRAINTS);
        value = trimmedHandle.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns true if the handle is valid after removing surrounding ordinary spaces.
     *
     * @throws NullPointerException If {@code test} is null.
     */
    public static boolean isValidTelegramHandle(String test) {
        requireNonNull(test);
        return trimSpaces(test).matches(VALIDATION_REGEX);
    }

    /**
     * Removes surrounding U+0020 spaces without accepting tabs or other whitespace.
     */
    private static String trimSpaces(String value) {
        return value.replaceAll("^ +| +$", "");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof TelegramHandle otherHandle && value.equals(otherHandle.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

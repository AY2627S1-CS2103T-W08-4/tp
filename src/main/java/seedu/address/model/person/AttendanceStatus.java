package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * Represents an attendance decision for a student's course and week.
 * Unrecorded means no decision has been recorded, rather than absence.
 */
public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent"),
    UNRECORDED("Unrecorded");

    public static final String MESSAGE_CONSTRAINTS =
            "Attendance status must be present, absent or unrecorded.";

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Converts a status word, ignoring case and surrounding ordinary U+0020 spaces.
     *
     * @throws NullPointerException if {@code input} is null.
     * @throws IllegalArgumentException if {@code input} is not a supported status.
     */
    public static AttendanceStatus fromString(String input) {
        requireNonNull(input);
        String normalized = input.replaceAll("^ +| +$", "").toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "present" -> PRESENT;
            case "absent" -> ABSENT;
            case "unrecorded" -> UNRECORDED;
            default -> throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}

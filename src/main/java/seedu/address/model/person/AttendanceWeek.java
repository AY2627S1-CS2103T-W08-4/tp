package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * A teaching week from 1 to 53, independent of calendar dates.
 * Guarantees: immutable; its display value is an ASCII integer without leading zeros.
 */
public final class AttendanceWeek {
    public static final String MESSAGE_CONSTRAINTS =
            "Week must be an integer from 1 to 53 without leading zeros.";
    private static final String VALIDATION_REGEX = "(?:[1-9]|[1-4][0-9]|5[0-3])";

    public final int value;

    /**
     * Constructs a week after removing surrounding ordinary U+0020 spaces only.
     *
     * @throws NullPointerException if {@code week} is null.
     * @throws IllegalArgumentException if {@code week} is not a supported week.
     */
    public AttendanceWeek(String week) {
        requireNonNull(week);
        String normalized = trimSpaces(week);
        checkArgument(isValidAttendanceWeek(normalized), MESSAGE_CONSTRAINTS);
        value = Integer.parseInt(normalized);
    }

    /**
     * Returns whether the input is a supported week, allowing surrounding ordinary spaces.
     *
     * @throws NullPointerException if {@code test} is null.
     */
    public static boolean isValidAttendanceWeek(String test) {
        requireNonNull(test);
        return trimSpaces(test).matches(VALIDATION_REGEX);
    }

    private static String trimSpaces(String input) {
        return input.replaceAll("^ +| +$", "");
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof AttendanceWeek otherWeek && value == otherWeek.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}

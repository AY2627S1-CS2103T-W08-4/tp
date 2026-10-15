package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a student's membership in a class, identified by a course code and a tutorial group,
 * e.g. {@code CS2103T:T04}.
 * Guarantees: immutable; course code and tutorial group are stored in uppercase.
 */
public final class ClassMembership {

    public static final String MESSAGE_CONSTRAINTS = "Class must be in the format COURSE:GROUP, e.g. CS2103T:T04. "
            + "COURSE is 2-4 letters, 4 digits and an optional letter. GROUP is 1 letter followed by 2 digits.";
    private static final String COURSE_REGEX = "[A-Za-z]{2,4}[0-9]{4}[A-Za-z]?";
    private static final String GROUP_REGEX = "[A-Za-z][0-9]{2}";
    private static final String VALIDATION_REGEX = COURSE_REGEX + ":" + GROUP_REGEX;
    private static final String SEPARATOR = ":";

    public final String courseCode;
    public final String tutorialGroup;

    /**
     * Constructs a {@code ClassMembership} after removing surrounding ordinary spaces.
     *
     * @param membership A membership in the format {@code COURSE:GROUP}, optionally surrounded by ordinary spaces.
     * @throws NullPointerException If {@code membership} is null.
     * @throws IllegalArgumentException If {@code membership} is invalid.
     */
    public ClassMembership(String membership) {
        requireNonNull(membership);
        String trimmedMembership = trimSpaces(membership);
        checkArgument(isValidClassMembership(trimmedMembership), MESSAGE_CONSTRAINTS);
        String[] parts = trimmedMembership.toUpperCase(Locale.ROOT).split(SEPARATOR);
        courseCode = parts[0];
        tutorialGroup = parts[1];
    }

    /**
     * Returns true if the membership is valid after removing surrounding ordinary spaces.
     *
     * @throws NullPointerException If {@code test} is null.
     */
    public static boolean isValidClassMembership(String test) {
        requireNonNull(test);
        return trimSpaces(test).matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if both memberships are for the same course, regardless of tutorial group.
     */
    public boolean isSameCourse(ClassMembership other) {
        requireNonNull(other);
        return courseCode.equals(other.courseCode);
    }

    /**
     * Removes surrounding U+0020 spaces without accepting tabs or other whitespace.
     */
    private static String trimSpaces(String value) {
        return value.replaceAll("^ +| +$", "");
    }

    @Override
    public String toString() {
        return courseCode + SEPARATOR + tutorialGroup;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof ClassMembership otherMembership
                && courseCode.equals(otherMembership.courseCode)
                && tutorialGroup.equals(otherMembership.tutorialGroup);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; validated with the legacy or student contract at construction.
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should only contain alphanumeric characters and spaces, and should not be blank";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";

    public static final String STUDENT_MESSAGE_CONSTRAINTS = "Name must be 1-50 characters, contain a letter, "
            + "and use only letters, combining marks, spaces, apostrophes, hyphens or periods.";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    private Name(String name, boolean student) {
        checkArgument(isValidStudentName(name), STUDENT_MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /** Creates a normalized MVP student name, retaining legacy validation for old contacts. */
    public static Name forStudent(String name) {
        requireNonNull(name);
        return new Name(normalizeStudentName(name), true);
    }

    /** Checks the normalized student name by Unicode code points rather than UTF-16 units. */
    public static boolean isValidStudentName(String name) {
        String normalized = normalizeStudentName(name);
        int length = normalized.codePointCount(0, normalized.length());
        return length >= 1 && length <= 50
                && normalized.matches("[\\p{L}\\p{M} '.’\\-]+")
                && normalized.codePoints().anyMatch(Character::isLetter);
    }

    private static String normalizeStudentName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFC).replaceAll("^ +| +$", "").replaceAll(" +", " ");
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}

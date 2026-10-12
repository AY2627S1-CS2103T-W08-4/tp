package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; validated with the legacy or student contract at construction.
 */
public class Email {

    public static final String STUDENT_MESSAGE_CONSTRAINTS = "Email must be at most 254 characters and use "
            + "local-part@domain with a valid dotted domain and no spaces.";

    private static final String SPECIAL_CHARACTERS = "+_.-";
    public static final String MESSAGE_CONSTRAINTS = "Emails should be of the format local-part@domain "
            + "and adhere to the following constraints:\n"
            + "1. The local-part should only contain alphanumeric characters and these special characters, excluding "
            + "the parentheses, (" + SPECIAL_CHARACTERS + "). The local-part may not start or end with any special "
            + "characters.\n"
            + "2. The local-part is followed by an '@' and then a domain name. The domain name is made up of domain "
            + "labels separated by periods.\n"
            + "The domain name must:\n"
            + "    - end with a domain label at least 2 characters long\n"
            + "    - have each domain label start and end with alphanumeric characters\n"
            + "    - have each domain label consist of alphanumeric characters, separated only by hyphens, if any.";
    // alphanumeric and special characters
    private static final String ALPHANUMERIC_NO_UNDERSCORE = "[^\\W_]+"; // alphanumeric characters except underscore
    private static final String LOCAL_PART_REGEX = "^" + ALPHANUMERIC_NO_UNDERSCORE + "([" + SPECIAL_CHARACTERS + "]"
            + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_LAST_PART_REGEX = "(" + DOMAIN_PART_REGEX + "){2,}$"; // At least two chars
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)*" + DOMAIN_LAST_PART_REGEX;
    public static final String VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    private Email(String email, boolean student) {
        checkArgument(isValidStudentEmail(email), STUDENT_MESSAGE_CONSTRAINTS);
        value = email.toLowerCase(Locale.ROOT);
    }

    /** Creates a normalized MVP email without changing legacy contact loading. */
    public static Email forStudent(String email) {
        requireNonNull(email);
        return new Email(email.replaceAll("^ +| +$", ""), true);
    }

    /** Checks the supported ASCII email subset, including all length boundaries. */
    public static boolean isValidStudentEmail(String email) {
        String trimmed = email.replaceAll("^ +| +$", "");
        String[] parts = trimmed.split("@", -1);
        if (trimmed.length() > 254 || parts.length != 2 || parts[0].length() > 64
                || !parts[0].matches("[A-Za-z0-9](?:[A-Za-z0-9._+\\-]*[A-Za-z0-9])?")
                || parts[0].contains("..")) {
            return false;
        }
        String[] labels = parts[1].split("\\.", -1);
        if (labels.length < 2 || !labels[labels.length - 1].matches("[A-Za-z]{2,63}")) {
            return false;
        }
        for (String label : labels) {
            if (!label.matches("[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9])?")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        return test.matches(VALIDATION_REGEX);
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

        // instanceof handles nulls
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}

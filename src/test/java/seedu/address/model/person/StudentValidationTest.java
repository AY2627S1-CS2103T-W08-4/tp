package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Duration;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class StudentValidationTest {
    @Test
    public void name_unicodeNormalizationAndAllowedPunctuation() {
        assertEquals("José 王 O’Connor-Anne.", Name.forStudent("  José   王 O’Connor-Anne.  ").fullName);
        assertEquals("O'Connor", Name.forStudent("O'Connor").fullName);
        assertEquals("a".repeat(50), Name.forStudent("a".repeat(50)).fullName);
        String supplementaryLetter = new String(Character.toChars(0x10400));
        assertTrue(Name.isValidStudentName(supplementaryLetter.repeat(50)));
        assertFalse(Name.isValidStudentName(supplementaryLetter.repeat(51)));
    }

    @Test
    public void name_relationshipMarkers_preserveCaseAndLengthLimits() {
        for (String name : new String[]{"Ravi s/o Kumar", "Priya d/o Raman", "Ravi S/O Kumar",
            "Priya D/o Raman", "Ravi s/o", "d/o Priya"}) {
            assertEquals(name, Name.forStudent(" " + name.replace(" ", "   ") + " ").fullName);
        }
        String maximum = "a".repeat(44) + " s/o B";
        assertEquals(maximum, Name.forStudent(maximum).fullName);
        assertFalse(Name.isValidStudentName(maximum + "b"));
        for (String invalid : new String[]{"Ravi s/other Kumar", "Ravi as/o Kumar", "Ravi s/oo Kumar",
            "Priya d/o/Raman", "Ravi s/o\tKumar", "Ravi s/o\nKumar", "Ravi s/o\u00a0Kumar"}) {
            assertFalse(Name.isValidStudentName(invalid), invalid);
        }
        assertFalse(Name.isValidName("Ravi s/o Kumar"));
    }

    @Test
    public void name_largeInternalSpacing_normalizesWithoutStalling() {
        // A pasted name is valid after collapsing spaces. The old end-anchored trim regex
        // rescanned this run quadratically and blocked the JavaFX command thread for seconds.
        assertTimeout(Duration.ofSeconds(2), () ->
                assertEquals("A B", Name.forStudent("A" + " ".repeat(100_000) + "B").fullName));
    }

    @Test
    public void name_invalidValuesRejected() {
        for (String invalid : new String[]{"", " ", "' - .", "́", "Anne1", "A/B", "A😀", "A\t", "A\n", "A B",
            "a".repeat(51)}) {
            assertThrows(IllegalArgumentException.class, Name.STUDENT_MESSAGE_CONSTRAINTS, () ->
                     Name.forStudent(invalid));
        }
        assertThrows(NullPointerException.class, () -> Name.forStudent(null));
    }

    @Test
    public void email_validSubsetAndLocaleIndependentNormalization() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("i+cs@comp.nus.edu", Email.forStudent(" I+CS@COMP.NUS.EDU ").value);
        } finally {
            Locale.setDefault(previous);
        }
        for (String valid : new String[]{"a@ex.com", "a_+-b@ex.com", "a".repeat(64) + "@ex.com",
            "a@" + "b".repeat(63) + ".com", "a@ex." + "b".repeat(63)}) {
            assertTrue(Email.isValidStudentEmail(valid), valid);
        }
        String maximum = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61);
        assertEquals(254, maximum.length());
        assertTrue(Email.isValidStudentEmail(maximum));
        assertFalse(Email.isValidStudentEmail(maximum + "d"));
    }

    @Test
    public void email_invalidValuesRejected() {
        for (String invalid : new String[]{"", "a@localhost", "a..b@ex.com", ".a@ex.com", "a+@ex.com",
            "a@@ex.com", "a@-ex.com", "a@ex-.com", "a@ex.c", "a@ex.c2", "a@ex..com", "a b@ex.com",
            "a@ex.com\t", "é@ex.com", "a".repeat(65) + "@ex.com", "a@" + "b".repeat(64) + ".com"}) {
            assertThrows(IllegalArgumentException.class, Email.STUDENT_MESSAGE_CONSTRAINTS, () ->
                     Email.forStudent(invalid));
        }
        assertThrows(NullPointerException.class, () -> Email.forStudent(null));
    }
}

package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ClassMembershipTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ClassMembership(null));
    }

    @Test
    public void isValidClassMembership_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ClassMembership.isValidClassMembership(null));
    }

    @Test
    public void constructor_supportedMemberships_storesNormalizedValue() {
        String[][] examples = {
            {"CS2103T:T04", "CS2103T", "T04"},
            {"cs2103t:t04", "CS2103T", "T04"}, // lowercase is normalized
            {"Cs2103T:t04", "CS2103T", "T04"},
            {"   CS2103T:T04   ", "CS2103T", "T04"},
            {"CS2040:B01", "CS2040", "B01"}, // no course suffix
            {"GEA1000:W12", "GEA1000", "W12"}, // 3-letter prefix
            {"ACCT1001X:A00", "ACCT1001X", "A00"} // 4-letter prefix with suffix
        };
        for (String[] example : examples) {
            assertTrue(ClassMembership.isValidClassMembership(example[0]), example[0]);
            ClassMembership membership = new ClassMembership(example[0]);
            assertEquals(example[1], membership.courseCode);
            assertEquals(example[2], membership.tutorialGroup);
            assertEquals(example[1] + ":" + example[2], membership.toString());
        }
    }

    @Test
    public void constructor_invalidMemberships_rejectsWithSpecifiedMessage() {
        String[] invalidMemberships = {
            "", "   ", ":", "CS2103T", "CS2103T:", ":T04", "T04",
            "CS2103T:T04:T05", "CS2103T::T04", "CS2103T;T04", "CS2103T/T04",
            "C2103T:T04", "ABCDE2103:T04", "CS210:T04", "CS21034:T04", "CS2103TT:T04", "2103:T04",
            "CS2103T:T4", "CS2103T:T004", "CS2103T:04", "CS2103T:TT4", "CS2103T:4T4",
            "CS2103T :T04", "CS2103T: T04", "CS 2103T:T04", "CS2103T:T 04",
            "\tCS2103T:T04", "CS2103T:T04\n", " CS2103T:T04", "CS2103T:T04 ",
            "ÇS2103T:T04", "CS２１03T:T04", "CS2103T:Т04"
        };
        for (String invalidMembership : invalidMemberships) {
            assertFalse(ClassMembership.isValidClassMembership(invalidMembership), invalidMembership);
            assertThrows(IllegalArgumentException.class, ClassMembership.MESSAGE_CONSTRAINTS, () ->
                    new ClassMembership(invalidMembership));
        }
    }

    @Test
    public void constructor_turkishDefaultLocale_usesAsciiUppercase() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("IS1108", new ClassMembership("is1108:t01").courseCode);
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void isSameCourse() {
        ClassMembership membership = new ClassMembership("CS2103T:T04");

        assertTrue(membership.isSameCourse(membership));
        assertTrue(membership.isSameCourse(new ClassMembership("cs2103t:T04")));
        assertTrue(membership.isSameCourse(new ClassMembership("CS2103T:F12"))); // different group
        assertFalse(membership.isSameCourse(new ClassMembership("CS2101:T04"))); // different course
        assertThrows(NullPointerException.class, () -> membership.isSameCourse(null));
    }

    @Test
    public void equals_normalizedMemberships_comparesByValue() {
        ClassMembership membership = new ClassMembership("CS2103T:T04");
        ClassMembership equivalent = new ClassMembership("  cs2103t:t04  ");

        assertTrue(membership.equals(membership));
        assertTrue(membership.equals(equivalent));
        assertTrue(equivalent.equals(membership));
        assertFalse(membership.equals(null));
        assertFalse(membership.equals("CS2103T:T04"));
        assertFalse(membership.equals(new ClassMembership("CS2103T:T05"))); // different group
        assertFalse(membership.equals(new ClassMembership("CS2101:T04"))); // different course
        assertEquals(membership.hashCode(), equivalent.hashCode());

        Set<ClassMembership> memberships = new HashSet<>();
        memberships.add(membership);
        memberships.add(equivalent);
        assertEquals(1, memberships.size());
    }
}

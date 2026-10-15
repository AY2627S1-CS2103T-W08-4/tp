package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.AttendanceTestData.ALICE;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class AttendanceRecordTest {
    @Test
    public void constructor_normalizesWithoutDependingOnLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            AttendanceRecord record = new AttendanceRecord(" is1108 ",
                    new AttendanceWeek("1"), AttendanceStatus.PRESENT);
            assertEquals("IS1108", record.course);
            assertEquals("IS1108 | Week 1 | Present", record.toString());
            assertEquals(record, new AttendanceRecord("IS1108", new AttendanceWeek("1"), AttendanceStatus.PRESENT));
            assertEquals(record.hashCode(),
                    new AttendanceRecord("IS1108", new AttendanceWeek("1"), AttendanceStatus.PRESENT).hashCode());
            assertFalse(record.equals(null));
            assertFalse(record.equals("IS1108"));
            assertNotEquals(record, new AttendanceRecord("CS1108", record.week, record.status));
            assertNotEquals(record, new AttendanceRecord(record.course, new AttendanceWeek("2"), record.status));
            assertNotEquals(record, new AttendanceRecord(record.course, record.week, AttendanceStatus.ABSENT));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void withAttendance_isImmutableAndIsolatesCoursesAndWeeks() {
        AttendanceWeek first = new AttendanceWeek("1");
        Person recorded = ALICE.withAttendance(new AttendanceRecord("CS2103T", first, AttendanceStatus.PRESENT))
                .withAttendance(new AttendanceRecord("CS1101S", first, AttendanceStatus.ABSENT))
                .withAttendance(new AttendanceRecord("CS2103T", new AttendanceWeek("2"), AttendanceStatus.ABSENT));
        assertTrue(ALICE.getAttendance().isEmpty());
        assertEquals(3, recorded.getAttendance().size());
        Person cleared = recorded.withAttendance(new AttendanceRecord("CS2103T", first, AttendanceStatus.UNRECORDED));
        assertEquals(AttendanceStatus.UNRECORDED, cleared.getAttendanceStatus("CS2103T", first));
        assertEquals(AttendanceStatus.ABSENT, cleared.getAttendanceStatus("cs1101s", first));
        assertEquals(AttendanceStatus.ABSENT, cleared.getAttendanceStatus("CS2103T", new AttendanceWeek("2")));
        assertEquals(2, cleared.getAttendance().size());
        assertEquals(ALICE.getName(), cleared.getName());
        assertEquals(ALICE.getEmail(), cleared.getEmail());
        assertEquals(ALICE.getTelegramHandle(), cleared.getTelegramHandle());
        assertEquals(ALICE.getMemberships(), cleared.getMemberships());
        assertTrue(ALICE.isSamePerson(cleared));
        assertNotEquals(ALICE, cleared);
        assertThrows(UnsupportedOperationException.class, () -> cleared.getAttendance().clear());
        assertThrows(IllegalArgumentException.class, "Student Alice is not enrolled in MA2001.", () ->
                ALICE.withAttendance(new AttendanceRecord("MA2001", first, AttendanceStatus.PRESENT)));
    }

    @Test
    public void constructor_nulls_rejected() {
        AttendanceWeek week = new AttendanceWeek("1");
        assertThrows(NullPointerException.class, () -> new AttendanceRecord(null, week, AttendanceStatus.PRESENT));
        assertThrows(NullPointerException.class, () -> new AttendanceRecord("CS2103T", null, AttendanceStatus.PRESENT));
        assertThrows(NullPointerException.class, () -> new AttendanceRecord("CS2103T", week, null));
    }
}

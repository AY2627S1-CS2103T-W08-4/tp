package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.student;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class AttendanceSummaryTest {

    private static final ClassMembership CLASS = new ClassMembership("CS2103T:T04");
    private static final AttendanceWeek WEEK_3 = new AttendanceWeek("3");
    private static final AttendanceWeek WEEK_4 = new AttendanceWeek("4");

    private final Person alice = student("Alice", "@alice_1", "alice@example.com", "CS2103T:T04", "MA2001:T01")
            .withAttendance(new AttendanceRecord("CS2103T", WEEK_3, AttendanceStatus.PRESENT))
            .withAttendance(new AttendanceRecord("MA2001", WEEK_3, AttendanceStatus.ABSENT));
    private final Person bobby = student("Bobby", "@bobby_1", "bobby@example.com", "CS2103T:T04")
            .withAttendance(new AttendanceRecord("CS2103T", WEEK_3, AttendanceStatus.ABSENT))
            .withAttendance(new AttendanceRecord("CS2103T", WEEK_4, AttendanceStatus.PRESENT));
    private final Person carol = student("Carol", "@carol_1", "carol@example.com", "CS2103T:T04");

    @Test
    public void of_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> AttendanceSummary.of(null, CLASS, WEEK_3));
        assertThrows(NullPointerException.class, () -> AttendanceSummary.of(List.of(), null, WEEK_3));
        assertThrows(NullPointerException.class, () -> AttendanceSummary.of(List.of(), CLASS, null));
    }

    @Test
    public void of_mixedDecisions_countsEachStatusForViewedCourseAndWeek() {
        AttendanceSummary summary = AttendanceSummary.of(List.of(alice, bobby, carol), CLASS, WEEK_3);
        assertEquals(1, summary.present);
        assertEquals(1, summary.absent); // Alice's MA2001 absence is another course, so it is not counted
        assertEquals(1, summary.unrecorded);
        assertEquals(3, summary.total());
        assertEquals("1 present, 1 absent, 1 unrecorded", summary.toString());
    }

    @Test
    public void of_otherWeek_usesThatWeeksDecisions() {
        AttendanceSummary summary = AttendanceSummary.of(List.of(alice, bobby, carol), CLASS, WEEK_4);
        assertEquals("1 present, 0 absent, 2 unrecorded", summary.toString());
    }

    @Test
    public void of_studentsOutsideClassAndLegacyContacts_ignored() {
        Person otherGroup = student("David", "@david_1", "david@example.com", "CS2103T:T05");
        Person legacy = new PersonBuilder().build();
        AttendanceSummary summary = AttendanceSummary.of(List.of(alice, otherGroup, legacy), CLASS, WEEK_3);
        assertEquals("1 present, 0 absent, 0 unrecorded", summary.toString());
        assertEquals(1, summary.total());
    }

    @Test
    public void of_noStudents_allZero() {
        assertEquals("0 present, 0 absent, 0 unrecorded", AttendanceSummary.of(List.of(), CLASS, WEEK_3).toString());
    }

    @Test
    public void equals() {
        AttendanceSummary summary = AttendanceSummary.of(List.of(alice, bobby, carol), CLASS, WEEK_3);
        AttendanceSummary same = AttendanceSummary.of(List.of(carol, bobby, alice), CLASS, WEEK_3);

        assertTrue(summary.equals(summary));
        assertTrue(summary.equals(same));
        assertEquals(summary.hashCode(), same.hashCode());
        assertFalse(summary.equals(null));
        assertFalse(summary.equals("1 present, 1 absent, 1 unrecorded"));
        assertFalse(summary.equals(AttendanceSummary.of(List.of(alice, bobby, carol), CLASS, WEEK_4)));
    }
}

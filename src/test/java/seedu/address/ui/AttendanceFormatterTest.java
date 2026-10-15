package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.AttendanceTestData.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Person;

public class AttendanceFormatterTest {
    @Test
    public void details_listsSortedRecordsAndEmptyState() {
        assertEquals("No attendance recorded.", AttendanceFormatter.details(ALICE));
        Person recorded = ALICE.withAttendance(new AttendanceRecord("CS2103T",
                new AttendanceWeek("2"), AttendanceStatus.ABSENT))
                .withAttendance(new AttendanceRecord("CS2103T", new AttendanceWeek("1"), AttendanceStatus.PRESENT));
        assertEquals("CS2103T | Week 1 | Present\nCS2103T | Week 2 | Absent", AttendanceFormatter.details(recorded));
    }

    @Test
    public void badge_onlyUsesRequestedCourseAndWeek() {
        Person recorded = ALICE.withAttendance(new AttendanceRecord("CS2103T",
                new AttendanceWeek("1"), AttendanceStatus.PRESENT));
        assertEquals("CS2103T | Week 1 | Present", AttendanceFormatter.badge(recorded,
                new ClassMembership("CS2103T:T04"), new AttendanceWeek("1")));
        assertEquals("CS2103T | Week 2 | Unrecorded", AttendanceFormatter.badge(recorded,
                new ClassMembership("CS2103T:T04"), new AttendanceWeek("2")));
        assertEquals("CS1101S | Week 1 | Unrecorded", AttendanceFormatter.badge(recorded,
                new ClassMembership("CS1101S:T02"), new AttendanceWeek("1")));
    }
}

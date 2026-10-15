package seedu.address.ui;

import java.util.stream.Collectors;

import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Person;

/** Text shared by selected-student details and course/week roster badges. */
public final class AttendanceFormatter {
    private AttendanceFormatter() {
    }

    /** Lists recorded decisions in deterministic course/week order, or the empty state. */
    public static String details(Person student) {
        return student.getAttendance().isEmpty() ? "No attendance recorded."
                : student.getAttendance().stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    /** A badge always names its context; it never displays another course/week's status. */
    public static String badge(Person student, ClassMembership membership, AttendanceWeek week) {
        return membership.courseCode + " | Week " + week + " | "
                + student.getAttendanceStatus(membership.courseCode, week);
    }
}

package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collection;

/**
 * Counts the attendance decisions of a class's students for one teaching week.
 * Guarantees: immutable; the three counts add up to the number of students counted.
 */
public final class AttendanceSummary {

    public final int present;
    public final int absent;
    public final int unrecorded;

    private AttendanceSummary(int present, int absent, int unrecorded) {
        assert present >= 0 && absent >= 0 && unrecorded >= 0;
        this.present = present;
        this.absent = absent;
        this.unrecorded = unrecorded;
    }

    /**
     * Returns the summary of {@code students} for {@code membership}'s course in {@code week}.
     * Students who are not in {@code membership}'s class are ignored.
     */
    public static AttendanceSummary of(Collection<Person> students, ClassMembership membership,
            AttendanceWeek week) {
        requireAllNonNull(students, membership, week);
        int present = 0;
        int absent = 0;
        int unrecorded = 0;
        for (Person student : students) {
            if (!student.isStudent() || !student.getMemberships().contains(membership)) {
                continue;
            }
            switch (student.getAttendanceStatus(membership.courseCode, week)) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                case UNRECORDED -> unrecorded++;
                default -> throw new AssertionError("Unknown attendance status");
            }
        }
        return new AttendanceSummary(present, absent, unrecorded);
    }

    public int total() {
        return present + absent + unrecorded;
    }

    @Override
    public String toString() {
        return present + " present, " + absent + " absent, " + unrecorded + " unrecorded";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof AttendanceSummary otherSummary
                && present == otherSummary.present
                && absent == otherSummary.absent
                && unrecorded == otherSummary.unrecorded;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * present + absent) + unrecorded;
    }
}

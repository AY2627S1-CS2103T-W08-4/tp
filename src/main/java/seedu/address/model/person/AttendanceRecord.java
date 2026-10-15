package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Locale;
import java.util.Objects;

/** An immutable attendance decision for one course and teaching week. */
public final class AttendanceRecord {
    public static final String COURSE_CONSTRAINTS =
            "Course must be 2-4 letters, 4 digits and an optional letter, for example CS2103T.";

    public final String course;
    public final AttendanceWeek week;
    public final AttendanceStatus status;

    /** Creates a validated course/week decision; Unrecorded represents clearing it. */
    public AttendanceRecord(String course, AttendanceWeek week, AttendanceStatus status) {
        requireAllNonNull(course, week, status);
        this.course = normalizeCourse(course);
        this.week = week;
        this.status = status;
    }

    /** Validates and normalizes a course, trimming ordinary spaces only. */
    public static String normalizeCourse(String input) {
        requireNonNull(input);
        String normalized = input.replaceAll("^ +| +$", "");
        checkArgument(normalized.matches("[A-Za-z]{2,4}[0-9]{4}[A-Za-z]?"), COURSE_CONSTRAINTS);
        return normalized.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AttendanceRecord record && course.equals(record.course)
                && week.equals(record.week) && status == record.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, week, status);
    }

    @Override
    public String toString() {
        return course + " | Week " + week + " | " + status;
    }
}

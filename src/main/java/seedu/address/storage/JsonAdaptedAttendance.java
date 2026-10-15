package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;

/** Human-editable course/week attendance stored inside its student's JSON record. */
class JsonAdaptedAttendance {
    private final String course;
    private final String week;
    private final String status;

    @JsonCreator
    public JsonAdaptedAttendance(@JsonProperty("course") String course,
            @JsonProperty("week") String week, @JsonProperty("status") String status) {
        this.course = course;
        this.week = week;
        this.status = status;
    }

    public JsonAdaptedAttendance(AttendanceRecord record) {
        this(record.course, record.week.toString(), record.status.toString());
    }

    public AttendanceRecord toModelType() throws IllegalValueException {
        if (course == null || week == null || status == null) {
            throw new IllegalValueException("Attendance course, week and status are required.");
        }
        try {
            return new AttendanceRecord(course, new AttendanceWeek(week), AttendanceStatus.fromString(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}

package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.AttendanceTestData.ALICE;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;

public class AttendanceStorageTest {
    @Test
    public void oldStudent_missingAttendance_defaultsToUnrecorded() throws Exception {
        assertEquals(ALICE, new JsonAdaptedPerson(ALICE).toModelType());
        assertTrue(student(null).toModelType().getAttendance().isEmpty());
    }

    @Test
    public void attendance_normalizedAndExplicitUnrecorded_clears() throws Exception {
        JsonAdaptedPerson adapter = student(List.of(new JsonAdaptedAttendance("cs2103t", " 1 ", "pRESENT"),
                new JsonAdaptedAttendance("CS2103T", "2", "Unrecorded")));
        assertEquals(1, adapter.toModelType().getAttendance().size());
        assertEquals(AttendanceStatus.PRESENT,
                adapter.toModelType().getAttendanceStatus("CS2103T", new AttendanceWeek("1")));
    }

    @Test
    public void invalidEntries_rejectInsteadOfLosingData() {
        for (JsonAdaptedAttendance entry : Arrays.asList(null,
                new JsonAdaptedAttendance(null, "1", "Present"),
                new JsonAdaptedAttendance("CS2103T", null, "Present"),
                new JsonAdaptedAttendance("CS2103T", "1", null),
                new JsonAdaptedAttendance("invalid", "1", "Present"),
                new JsonAdaptedAttendance("CS2103T", "54", "Present"),
                new JsonAdaptedAttendance("CS2103T", "1", "Late"),
                new JsonAdaptedAttendance("MA2001", "1", "Present"))) {
            assertThrows(IllegalValueException.class, () -> student(Arrays.asList(entry)).toModelType());
        }
        JsonAdaptedPerson duplicate = student(List.of(new JsonAdaptedAttendance("CS2103T", "1", "Present"),
                new JsonAdaptedAttendance("cs2103t", "1", "Absent")));
        assertThrows(IllegalValueException.class, "Duplicate attendance for CS2103T week 1.", duplicate::toModelType);
        JsonAdaptedPerson legacy = new JsonAdaptedPerson("Legacy", "12345678", "legacy@example.com", "Road", List.of(),
                null, null, List.of(new JsonAdaptedAttendance("CS2103T", "1", "Present")));
        assertThrows(IllegalValueException.class, legacy::toModelType);
    }

    private JsonAdaptedPerson student(List<JsonAdaptedAttendance> attendance) {
        return new JsonAdaptedPerson("Alice", null, "alice@example.com", null, List.of(), "@alice_1",
                List.of("CS2103T:T04", "CS1101S:T02"), attendance);
    }
}

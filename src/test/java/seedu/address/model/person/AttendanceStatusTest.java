package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AttendanceStatusTest {

    @Test
    public void fromString_validStatuses_returnsStatus() {
        assertEquals(AttendanceStatus.PRESENT, AttendanceStatus.fromString("present"));
        assertEquals(AttendanceStatus.ABSENT, AttendanceStatus.fromString("absent"));
        assertEquals(AttendanceStatus.UNRECORDED, AttendanceStatus.fromString("unrecorded"));
    }

    @Test
    public void fromString_mixedCase_returnsStatus() {
        assertEquals(AttendanceStatus.PRESENT, AttendanceStatus.fromString("pReSeNt"));
        assertEquals(AttendanceStatus.ABSENT, AttendanceStatus.fromString("ABSENT"));
        assertEquals(AttendanceStatus.UNRECORDED, AttendanceStatus.fromString("UnReCoRdEd"));
    }

    @Test
    public void fromString_surroundingOrdinarySpaces_returnsStatus() {
        assertEquals(AttendanceStatus.PRESENT, AttendanceStatus.fromString("  present"));
        assertEquals(AttendanceStatus.ABSENT, AttendanceStatus.fromString("absent  "));
        assertEquals(AttendanceStatus.UNRECORDED, AttendanceStatus.fromString("  Unrecorded  "));
    }

    @Test
    public void fromString_invalidInput_throwsIllegalArgumentException() {
        String[] invalidInputs = {
            "", "   ", "late", "excused", "p", "present absent", "un recorded", "pre\tsent",
            "abs\nent", "\tpresent", "present\t", "\npresent", "present\n", "present\r\n",
            "  present\n  ", "\u00a0present", "present\u00a0", "\u2003absent", "absent\u2003"
        };
        String expectedMessage = "Attendance status must be present, absent or unrecorded.";
        for (String input : invalidInputs) {
            assertThrows(IllegalArgumentException.class, expectedMessage, () -> AttendanceStatus.fromString(input));
        }
    }

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> AttendanceStatus.fromString(null));
    }

    @Test
    public void toString_returnsDisplayLabel() {
        assertEquals("Present", AttendanceStatus.PRESENT.toString());
        assertEquals("Absent", AttendanceStatus.ABSENT.toString());
        assertEquals("Unrecorded", AttendanceStatus.UNRECORDED.toString());
    }
}

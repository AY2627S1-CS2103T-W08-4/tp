package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class AttendanceWeekTest {
    @Test
    public void constructor_allSupportedWeeks_preservesNumberAndDisplay() {
        for (int number = 1; number <= 53; number++) {
            String input = Integer.toString(number);
            assertTrue(AttendanceWeek.isValidAttendanceWeek(input));
            AttendanceWeek week = new AttendanceWeek(input);
            assertEquals(number, week.value);
            assertEquals(input, week.toString());
        }
    }

    @Test
    public void constructor_ordinarySpaces_normalizes() {
        for (String input : new String[]{" 1", "1 ", "  1  ", " 53 "}) {
            assertTrue(AttendanceWeek.isValidAttendanceWeek(input));
            AttendanceWeek week = new AttendanceWeek(input);
            assertEquals(input.trim(), week.toString());
        }
    }

    @Test
    public void constructor_invalidWeeks_rejectsWithSpecifiedMessage() {
        String[] invalidInputs = {
            "", " ", "0", "00", "01", "053", "54", "99", "100", "2147483648",
            "9".repeat(1000), "-1", "+1", "1.0", "1e1", "1,0", "1 0", "1/2", "week 1",
            "2026-10-15", "\u0661", "\uff11", "\t1", "1\t", "\n1", "1\n", "1\r\n",
            " 1\n ", "\u00a01", "1\u00a0", "\u20031", "1\u2003", "1\u0000", "1\f", "1\u000b"
        };
        for (String input : invalidInputs) {
            assertFalse(AttendanceWeek.isValidAttendanceWeek(input), input);
            assertThrows(IllegalArgumentException.class,
                    "Week must be an integer from 1 to 53 without leading zeros.", () -> new AttendanceWeek(input));
        }
    }

    @Test
    public void nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AttendanceWeek(null));
        assertThrows(NullPointerException.class, () -> AttendanceWeek.isValidAttendanceWeek(null));
    }

    @Test
    public void equals_comparesNormalizedWeek() {
        AttendanceWeek week = new AttendanceWeek("1");
        assertTrue(week.equals(week));
        assertTrue(week.equals(new AttendanceWeek(" 1 ")));
        assertFalse(week.equals(new AttendanceWeek("2")));
        assertFalse(week.equals(null));
        assertFalse(week.equals("1"));
    }

    @Test
    public void hashCode_equalWeeks_shareMapEntry() {
        Map<AttendanceWeek, AttendanceStatus> records = new HashMap<>();
        records.put(new AttendanceWeek("1"), AttendanceStatus.PRESENT);
        records.put(new AttendanceWeek(" 1 "), AttendanceStatus.ABSENT);
        records.put(new AttendanceWeek("53"), AttendanceStatus.PRESENT);
        assertEquals(2, records.size());
        assertEquals(AttendanceStatus.ABSENT, records.get(new AttendanceWeek("1")));
        assertEquals(AttendanceStatus.PRESENT, records.get(new AttendanceWeek("53")));
        assertEquals(new AttendanceWeek("1").hashCode(), new AttendanceWeek(" 1 ").hashCode());
    }
}

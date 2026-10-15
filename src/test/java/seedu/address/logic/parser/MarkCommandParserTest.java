package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;

public class MarkCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_valid_normalizesAndAcceptsAnyParameterOrder() throws Exception {
        MarkCommand expected = new MarkCommand(Index.fromOneBased(1),
                new AttendanceRecord("CS2103T", new AttendanceWeek("53"), AttendanceStatus.ABSENT));
        assertEquals(expected, parser.parseCommand("  mark 1 s/aBsEnT w/53 m/cs2103t  "));
        assertEquals(expected, parser.parseCommand("mark 1 m/CS2103T w/53 s/absent"));
        assertEquals(new MarkCommand(Index.fromOneBased(Integer.MAX_VALUE),
                new AttendanceRecord("MA2001", new AttendanceWeek("1"), AttendanceStatus.UNRECORDED)),
                parser.parseCommand("mark 2147483647 m/MA2001 w/1 s/unrecorded"));
    }

    @Test
    public void parse_missingIndex_usesFormatError() {
        for (String input : new String[]{"mark", "mark m/CS2103T w/1 s/present"}) {
            assertError(input, String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_invalidIndex_rejectsWithoutOverflow() {
        for (String index : new String[]{"0", "01", "-1", "+1", "1.0", "1e2", "1 2", "2147483648",
            "9".repeat(100), "\u0661", "\t1"}) {
            assertError("mark " + index + " m/CS2103T w/1 s/present", MarkCommandParser.INDEX_CONSTRAINTS);
        }
    }

    @Test
    public void parse_structuralErrors_reportsExactPrefix() {
        assertError("mark 1 w/1 s/present", "Missing required parameter: m/.");
        assertError("mark 1 m/CS2103T s/present", "Missing required parameter: w/.");
        assertError("mark 1 m/CS2103T w/1", "Missing required parameter: s/.");
        for (String prefix : new String[]{"m", "w", "s"}) {
            assertError("mark 1 m/CS2103T w/1 s/present " + prefix + "/x",
                    "Parameter " + prefix + "/ must be specified only once.");
        }
        assertError("mark 1 m/CS2103T w/1 s/present x/foo", "Unknown parameter: x/.");
        assertError("mark 1 M/CS2103T w/1 s/present", "Unknown parameter: M/.");
    }

    @Test
    public void parse_invalidValues_usesFieldErrors() {
        for (String course : new String[]{"", "CS2103T:T04", "C2103", "ABCDE1234", "CS 2103", "CS2103\t"}) {
            assertError("mark 1 m/" + course + " w/1 s/present", AttendanceRecord.COURSE_CONSTRAINTS);
        }
        for (String week : new String[]{"", "0", "01", "54", "1\n", "\u00a01"}) {
            assertError("mark 1 m/CS2103T w/" + week + " s/present", AttendanceWeek.MESSAGE_CONSTRAINTS);
        }
        for (String status : new String[]{"", "late", "pre sent", "present\t", "present extra"}) {
            assertError("mark 1 m/CS2103T w/1 s/" + status, AttendanceStatus.MESSAGE_CONSTRAINTS);
        }
    }

    private void assertError(String input, String message) {
        assertThrows(ParseException.class, message, () -> parser.parseCommand(input));
    }
}

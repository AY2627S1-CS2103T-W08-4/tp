package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SelectCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_validIndices_acceptsSurroundingOrdinarySpaces() throws Exception {
        assertEquals(new SelectCommand(Index.fromOneBased(1)), parser.parseCommand("select 1"));
        assertEquals(new SelectCommand(Index.fromOneBased(3)), parser.parseCommand("  select   3  "));
        assertEquals(new SelectCommand(Index.fromOneBased(Integer.MAX_VALUE)),
                parser.parseCommand("select 2147483647"));
    }

    @Test
    public void parse_invalidIndicesAndExtraArguments_rejected() {
        String error = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE);
        for (String value : new String[]{"", " ", "0", "01", "-1", "+1", "1.0", "1 2", "n/Alice", "1 x/a",
            "2147483648", "9".repeat(100), "\u0661", "\uff11"}) {
            assertThrows(ParseException.class, error, () -> parser.parseCommand("select " + value));
        }
    }

    @Test
    public void parse_controlAndUnicodeWhitespace_rejected() {
        String error = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE);
        for (String whitespace : new String[]{"\t", "\r", "\n", "\u0000", "\u00a0", "\u2003", "\u2028"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(whitespace + "select 1"));
            assertThrows(ParseException.class, () -> parser.parseCommand("select" + whitespace + "1"));
            assertThrows(ParseException.class, error, () -> parser.parseCommand("select " + whitespace + "1"));
            assertThrows(ParseException.class, error, () -> parser.parseCommand("select 1" + whitespace));
        }
        assertThrows(ParseException.class, () -> parser.parseCommand("SELECT 1"));
    }
}

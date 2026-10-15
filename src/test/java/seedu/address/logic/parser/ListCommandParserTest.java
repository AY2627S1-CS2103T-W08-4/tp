package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;

public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_noArguments_success() throws Exception {
        assertTrue(parser.parse("") instanceof ListCommand);
        assertTrue(parser.parse("   ") instanceof ListCommand);
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        String expectedMessage = "Invalid command format!\nFormat: list";
        assertParseFailure(parser, " 3", expectedMessage);
        assertParseFailure(parser, " all", expectedMessage);
        assertParseFailure(parser, " c/CS2103T:T04", expectedMessage);
        assertParseFailure(parser, " w/3", expectedMessage);
        assertParseFailure(parser, "  list  ", expectedMessage);
        assertParseFailure(parser, "\t", expectedMessage);
        assertParseFailure(parser, " \t ", expectedMessage);
        assertParseFailure(parser, "\n", expectedMessage);
        assertParseFailure(parser, "\r\n", expectedMessage);
        assertParseFailure(parser, "\u00a0", expectedMessage);
        assertParseFailure(parser, "\u2003", expectedMessage);
        assertParseFailure(parser, " 3", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
    }
}

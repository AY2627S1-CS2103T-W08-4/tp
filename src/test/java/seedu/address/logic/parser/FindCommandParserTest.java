package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;

public class FindCommandParserTest {
    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_valid_normalizesAndAcceptsEitherOrder() {
        FindCommand expected = new FindCommand(new ClassMembership("CS2103T:T04"), new AttendanceWeek("3"));
        assertParseSuccess(parser, " c/cs2103t:t04 w/3 ", expected);
        assertParseSuccess(parser, " w/3 c/CS2103T:T04 ", expected);
    }

    @Test
    public void parse_invalid_usesContractErrors() {
        assertParseFailure(parser, "", "Missing required parameter: c/.");
        assertParseFailure(parser, " c/T04", ClassMembership.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/CS2103T:T04", "Missing required parameter: w/.");
        assertParseFailure(parser, " c/CS2103T:T04 w/01", AttendanceWeek.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " c/CS2103T:T04 w/3 w/4", "Parameter w/ must be specified only once.");
        assertParseFailure(parser, " c/CS2103T:T04 c/CS1101S:T02 w/3",
                "Parameter c/ must be specified only once.");
        assertParseFailure(parser, " x/CS2103T:T04 w/3", "Unknown parameter: x/.");
        assertParseFailure(parser, " John", "Invalid command format!\n" + FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "\tc/CS2103T:T04 w/3",
                "Invalid command format!\n" + FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " c/CS2103T:T04 w/3\t", AttendanceWeek.MESSAGE_CONSTRAINTS);
    }
}

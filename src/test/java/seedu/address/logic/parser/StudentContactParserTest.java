package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Contract tests shared by add and update, using the same entry point as the command box. */
public class StudentContactParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_contactFieldOrders_produceEquivalentCommands() throws Exception {
        String[] fields = {"n/ José  Tan", "p/@JOSE_TAN", "e/JOSE@EXAMPLE.COM"};
        Command add = parser.parseCommand("add n/José Tan p/@jose_tan e/jose@example.com c/CS2103T:T04");
        Command update = parser.parseCommand("update 1 n/José Tan p/@jose_tan e/jose@example.com");
        // All six contact orders, with membership both before and after contacts, must normalize identically.
        for (int first = 0; first < fields.length; first++) {
            for (int second = 0; second < fields.length; second++) {
                if (first == second) {
                    continue;
                }
                int third = 3 - first - second;
                String contacts = fields[first] + " " + fields[second] + " " + fields[third];
                assertEquals(add, parser.parseCommand("add " + contacts + " c/cs2103t:t04"));
                assertEquals(add, parser.parseCommand("add c/cs2103t:t04 " + contacts));
                assertEquals(update, parser.parseCommand("update 1 " + contacts));
            }
        }
    }

    @Test
    public void parse_unsupportedWhitespaceInEachContact_rejectsWithoutSilentlyTrimming() {
        // Test each field independently; a valid ordinary-space control is covered above.
        for (String whitespace : List.of("\t", "\r", "\n", "\u0000", "\u00a0", "\u2003", "\u2028")) {
            for (String field : List.of("n/Alice", "p/@alice_1", "e/alice@example.com")) {
                String invalid = field + whitespace;
                String contacts = (field.startsWith("n/") ? invalid : "n/Alice") + " "
                        + (field.startsWith("p/") ? invalid : "p/@alice_1") + " "
                        + (field.startsWith("e/") ? invalid : "e/alice@example.com");
                assertThrows(ParseException.class, () -> parser.parseCommand("add " + contacts + " c/CS2103T:T04"));
                assertThrows(ParseException.class, () -> parser.parseCommand("update 1 " + invalid));
            }
        }
    }

    @Test
    public void parse_largeInternalSpaces_acceptsNameAndRejectsMalformedContactsPromptly() {
        String spaces = " ".repeat(100_000);
        // This is a generous regression guard, not a benchmark: the former regex took seconds per field.
        assertTimeout(Duration.ofSeconds(2), () -> {
            assertEquals(parser.parseCommand("update 1 n/A B"), parser.parseCommand("update 1 n/A" + spaces + "B"));
            assertEquals(parser.parseCommand("add n/A B p/@alice_1 e/alice@example.com c/CS2103T:T04"),
                    parser.parseCommand("add n/A" + spaces + "B p/@alice_1 e/alice@example.com c/CS2103T:T04"));
            assertThrows(ParseException.class, () -> parser.parseCommand("update 1 p/@alice" + spaces + "one"));
            assertThrows(ParseException.class, () -> parser.parseCommand("update 1 e/alice" + spaces + "@example.com"));
            assertThrows(ParseException.class, () -> parser.parseCommand("update 1" + spaces + "2 n/Alice"));
        });
    }
}

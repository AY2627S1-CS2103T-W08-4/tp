package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UpdateCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.TelegramHandle;

public class UpdateCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_valid_normalizesAndAcceptsAnyOrder() throws Exception {
        UpdateCommand expected = new UpdateCommand(Index.fromOneBased(1), Optional.of(Name.forStudent("Anne Tan")),
                Optional.of(new TelegramHandle("@anne_tan")), Optional.of(Email.forStudent("anne@example.com")));
        assertEquals(expected, parser.parseCommand("  update 1 e/ANNE@EXAMPLE.COM n/ Anne   Tan p/@Anne_Tan  "));
        assertEquals(expected, parser.parseCommand("update 1 n/Anne Tan p/@anne_tan e/anne@example.com"));
        assertEquals(new UpdateCommand(Index.fromOneBased(Integer.MAX_VALUE), Optional.empty(), Optional.empty(),
                Optional.of(Email.forStudent("a@example.com"))),
                parser.parseCommand("update 2147483647 e/a@example.com"));
    }

    @Test
    public void parse_relationshipMarkers_preservesLiteralName() throws Exception {
        for (String name : new String[]{"Ravi s/o Kumar", "Priya d/o Raman", "Ravi S/O Kumar",
            "Priya D/o Raman", "Ravi s/o", "d/o Priya"}) {
            UpdateCommand expected = new UpdateCommand(Index.fromOneBased(1), Optional.of(Name.forStudent(name)),
                    Optional.empty(), Optional.empty());
            assertEquals(expected, parser.parseCommand("update 1 n/" + name));
            assertEquals(expected, parser.parseCommand("update 1 n/ " + name.replace(" ", "   ") + " "));
        }
    }

    @Test
    public void parse_relationshipMarkers_doNotHideUnknownOrRepeatedPrefixes() {
        for (String token : new String[]{"s/other", "d/other", "x/o", "s/oops", "d/o/", "s/o\t", "s/", "d/"}) {
            String prefix = token.substring(0, token.indexOf('/'));
            assertThrows(ParseException.class, "Unknown parameter: " + prefix + "/.", () ->
                    parser.parseCommand("update 1 n/Ravi " + token + " Kumar"));
        }
        assertThrows(ParseException.class, "Unknown parameter: s/.", () ->
                parser.parseCommand("update 1 e/ravi@example.com s/o"));
        assertThrows(ParseException.class, "Unknown parameter: x/.", () ->
                parser.parseCommand("update 1 n/Ravi s/o Kumar x/value"));
        assertThrows(ParseException.class, "Parameter n/ must be specified only once.", () ->
                parser.parseCommand("update 1 n/Ravi s/o Kumar n/Priya"));
    }

    @Test
    public void parse_invalidIndices_rejected() {
        for (String index : new String[]{"0", "01", "-1", "+1", "1.0", "1 2", "2147483648",
            "9".repeat(100), "\u0661", "\t1"}) {
            assertThrows(ParseException.class, UpdateCommandParser.INDEX_CONSTRAINTS, () ->
                    parser.parseCommand("update " + index + " n/Alice"));
        }
        for (String input : new String[]{"update", "update n/Alice", "\tupdate 1 n/Alice"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parse_missingRepeatedUnknownFields_rejected() {
        assertThrows(ParseException.class, UpdateCommand.MESSAGE_NOT_EDITED, () -> parser.parseCommand("update 1"));
        for (String prefix : new String[]{"n", "p", "e"}) {
            assertThrows(ParseException.class, "Parameter " + prefix + "/ must be specified only once.", () ->
                    parser.parseCommand("update 1 " + prefix + "/first " + prefix + "/second"));
        }
        for (String prefix : new String[]{"N", "c", "a", "t", "x"}) {
            assertThrows(ParseException.class, "Unknown parameter: " + prefix + "/.", () ->
                    parser.parseCommand("update 1 n/Alice " + prefix + "/value"));
        }
    }

    @Test
    public void parse_emptyInvalidAndControlWhitespaceFields_rejected() {
        for (String field : new String[]{"n/", "p/", "e/", "n/123", "n/" + "a".repeat(51),
            "p/alice_1", "p/@a", "e/a@localhost", "n/Alice\t", "n/Alice\n", "p/@alice_1\t",
            "e/alice@example.com\n", "n/Alice\u00a0Tan", "p/@alice_1 extra", "e/a@example.com extra"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("update 1 " + field));
        }
    }
}

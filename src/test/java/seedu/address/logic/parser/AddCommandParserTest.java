package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.student;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.TelegramHandle;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();
    private final String valid = " n/Anne-Marie Tan p/@Anne_Tan e/Anne@Example.com c/cs2103t:t04";

    @Test
    public void parse_normalizationAndMultipleClasses_success() {
        assertParseSuccess(parser,
                " e/Anne@Example.com c/cs2103t:t04 n/ Anne-Marie   Tan p/@Anne_Tan c/ma2001:t04 ",
                new AddCommand(student("Anne-Marie Tan", "@anne_tan", "anne@example.com",
                        "CS2103T:T04", "MA2001:T04")));
        assertParseSuccess(parser, " n/José 王 p/@jose_w e/j@ex.com c/MA2001:T01",
                new AddCommand(student("José 王", "@jose_w", "j@ex.com", "MA2001:T01")));
    }

    @Test
    public void parse_relationshipMarkers_preservesLiteralName() throws Exception {
        for (String name : new String[]{"Ravi s/o Kumar", "Priya d/o Raman", "Ravi S/O Kumar",
            "Priya D/o Raman", "Ravi s/o", "d/o Priya"}) {
            AddCommand expected = new AddCommand(student(name, "@name_audit", "name.audit@example.com", "CS2103T:T04"));
            String contacts = " p/@name_audit e/name.audit@example.com c/CS2103T:T04";
            assertEquals(expected, new AddressBookParser().parseCommand("add n/" + name + contacts));
            assertParseSuccess(parser, contacts + " n/ " + name.replace(" ", "   ") + " ", expected);
        }
    }

    @Test
    public void parse_relationshipMarkers_doNotHideUnknownOrRepeatedPrefixes() {
        for (String token : new String[]{"s/other", "d/other", "x/o", "s/oops", "d/o/", "s/o\t"}) {
            String prefix = token.substring(0, token.indexOf('/'));
            assertParseFailure(parser, valid.replace("Anne-Marie Tan", "Ravi " + token + " Kumar"),
                    "Unknown parameter: " + prefix + "/.");
        }
        assertParseFailure(parser, valid + " s/o", "Unknown parameter: s/.");
        assertParseFailure(parser, valid.replace("Anne-Marie Tan", "Ravi s/o Kumar x/value"),
                "Unknown parameter: x/.");
        assertParseFailure(parser, valid.replace("Anne-Marie Tan", "Ravi s/o Kumar n/Priya"),
                "Parameter n/ must be specified only once.");
    }

    @Test
    public void parse_missingRequiredPrefixes_failure() {
        assertParseFailure(parser, "", "Missing required parameter: n/.");
        assertParseFailure(parser, " p/@anne_tan e/anne@example.com c/CS2103T:T04", "Missing required parameter: n/.");
        assertParseFailure(parser, " n/Anne e/anne@example.com c/CS2103T:T04", "Missing required parameter: p/.");
        assertParseFailure(parser, " n/Anne p/@anne_tan c/CS2103T:T04", "Missing required parameter: e/.");
        assertParseFailure(parser, " n/Anne p/@anne_tan e/anne@example.com", "Missing required parameter: c/.");
    }

    @Test
    public void parse_emptyValues_failure() {
        assertParseFailure(parser, valid.replace("Anne-Marie Tan", ""), Name.STUDENT_MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid.replace("@Anne_Tan", ""), TelegramHandle.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid.replace("Anne@Example.com", ""), Email.STUDENT_MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid.replace("cs2103t:t04", ""), ClassMembership.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedSingletonsAndUnknownPrefixes_failure() {
        for (String prefix : new String[]{"n", "p", "e"}) {
            assertParseFailure(parser, valid + " " + prefix + "/anything",
                    "Parameter " + prefix + "/ must be specified only once.");
        }
        for (String prefix : new String[]{"x", "N", "a", "t", "unknown"}) {
            assertParseFailure(parser, valid + " " + prefix + "/anything", "Unknown parameter: " + prefix + "/.");
        }
    }

    @Test
    public void parse_repeatedCourses_failureAfterValidatingAllClasses() {
        String duplicate = valid + " c/CS2103T:T05";
        assertParseFailure(parser, duplicate, "Course CS2103T may be specified only once per student.");
        assertParseFailure(parser, valid + " c/CS2103T:T04", "Course CS2103T may be specified only once per student.");
        assertParseFailure(parser, duplicate + " c/T04", ClassMembership.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_preambleAndInvalidValues_failure() {
        assertParseFailure(parser, " John Doe", "Invalid command format!\n" + AddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " unexpected" + valid, "Invalid command format!\n" + AddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, valid.replace("Anne-Marie Tan", "Anne2"), Name.STUDENT_MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid.replace("@Anne_Tan", "@ab"), TelegramHandle.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid.replace("Anne@Example.com", "a..b@ex.com"), Email.STUDENT_MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid.replace("cs2103t:t04", "CS2103T : T04"), ClassMembership.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_controlWhitespaceNotSilentlyTrimmed_failureThroughMainParser() {
        AddressBookParser mainParser = new AddressBookParser();
        for (String whitespace : new String[]{"\t", "\n", "\r", "\u00a0"}) {
            assertThrows(ParseException.class, ClassMembership.MESSAGE_CONSTRAINTS, () ->
                     mainParser.parseCommand("add" + valid + whitespace));
            assertThrows(ParseException.class, TelegramHandle.MESSAGE_CONSTRAINTS, () ->
                     mainParser.parseCommand("add" + valid.replace("@Anne_Tan", whitespace + "@Anne_Tan")));
        }
    }

    @Test
    public void parse_surroundingOrdinarySpaces_successThroughMainParser() throws Exception {
        assertEquals(parser.parse(valid), new AddressBookParser().parseCommand("  add" + valid + "  "));
    }
}

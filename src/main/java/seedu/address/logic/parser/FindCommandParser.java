package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.ClassMembership;

/** Parses an exact class/week query without accepting non-ordinary whitespace. */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Pattern PREFIX = Pattern.compile("(?<![^ ])([^ /\\s]+)/");

    @Override
    public FindCommand parse(String args) throws ParseException {
        Matcher matcher = PREFIX.matcher(args);
        Map<String, String> values = new LinkedHashMap<>();
        String previous = null;
        int start = 0;
        while (matcher.find()) {
            if (previous == null && !args.substring(0, matcher.start()).matches(" *")) {
                throw formatError();
            }
            if (previous != null) {
                values.put(previous, args.substring(start, matcher.start()));
            }
            String prefix = matcher.group(1);
            if (!prefix.equals("c") && !prefix.equals("w")) {
                throw new ParseException("Unknown parameter: " + prefix + "/.");
            }
            if (values.containsKey(prefix)) {
                throw new ParseException("Parameter " + prefix + "/ must be specified only once.");
            }
            previous = prefix;
            start = matcher.end();
        }
        if (previous != null) {
            values.put(previous, args.substring(start));
        } else if (!args.matches(" *")) {
            throw formatError();
        }
        if (!values.containsKey("c")) {
            throw new ParseException("Missing required parameter: c/.");
        }
        try {
            ClassMembership membership = new ClassMembership(values.get("c"));
            if (!values.containsKey("w")) {
                throw new ParseException("Missing required parameter: w/.");
            }
            return new FindCommand(membership, new AttendanceWeek(values.get("w")));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    private ParseException formatError() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }
}

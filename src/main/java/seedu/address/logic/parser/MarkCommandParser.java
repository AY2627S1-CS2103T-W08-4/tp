package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.AttendanceRecord;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;

/** Parses mark without accepting non-ordinary whitespace or discarding unknown prefixes. */
public class MarkCommandParser implements Parser<MarkCommand> {
    public static final String INDEX_CONSTRAINTS =
            "Index must be a positive integer without leading zeros, at most 2147483647.";
    private static final Pattern PREFIX = Pattern.compile("(?<![^ ])([^ /\\s]+)/");
    private static final List<String> REQUIRED = List.of("m", "w", "s");

    @Override
    public MarkCommand parse(String args) throws ParseException {
        Matcher matcher = PREFIX.matcher(args);
        Map<String, String> values = new LinkedHashMap<>();
        String indexText = args;
        String previous = null;
        int start = 0;
        while (matcher.find()) {
            if (previous == null) {
                indexText = args.substring(0, matcher.start());
            } else {
                values.put(previous, args.substring(start, matcher.start()));
            }
            String prefix = matcher.group(1);
            if (!REQUIRED.contains(prefix)) {
                throw new ParseException("Unknown parameter: " + prefix + "/.");
            }
            if (values.containsKey(prefix) || prefix.equals(previous)) {
                throw new ParseException("Parameter " + prefix + "/ must be specified only once.");
            }
            previous = prefix;
            start = matcher.end();
        }
        if (previous != null) {
            values.put(previous, args.substring(start));
        }
        indexText = indexText.replaceAll("^ +| +$", "");
        if (indexText.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkCommand.MESSAGE_USAGE));
        }
        int number;
        try {
            if (!indexText.matches("[1-9][0-9]*")) {
                throw new NumberFormatException();
            }
            number = Integer.parseInt(indexText);
        } catch (NumberFormatException e) {
            throw new ParseException(INDEX_CONSTRAINTS);
        }
        for (String prefix : REQUIRED) {
            if (!values.containsKey(prefix)) {
                throw new ParseException("Missing required parameter: " + prefix + "/.");
            }
        }
        try {
            String course = AttendanceRecord.normalizeCourse(values.get("m"));
            AttendanceWeek week = new AttendanceWeek(values.get("w"));
            AttendanceStatus status = AttendanceStatus.fromString(values.get("s"));
            return new MarkCommand(Index.fromOneBased(number), new AttendanceRecord(course, week, status));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}

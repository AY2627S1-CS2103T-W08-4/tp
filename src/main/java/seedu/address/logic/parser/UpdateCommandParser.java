package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UpdateCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.TelegramHandle;

/** Parses optional student contacts, retaining raw whitespace for field validation. */
public class UpdateCommandParser implements Parser<UpdateCommand> {
    public static final String INDEX_CONSTRAINTS =
            "Index must be a positive integer without leading zeros, at most 2147483647.";
    private static final Pattern PREFIX = Pattern.compile("(?<![^ ])([^ /\\s]+)/");
    private static final List<String> ALLOWED = List.of("n", "p", "e");

    @Override
    public UpdateCommand parse(String args) throws ParseException {
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
            if (!ALLOWED.contains(prefix)) {
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
        }
        Index index = parseIndex(indexText);
        try {
            Optional<Name> name = Optional.ofNullable(values.get("n")).map(Name::forStudent);
            Optional<TelegramHandle> telegram = Optional.ofNullable(values.get("p")).map(TelegramHandle::new);
            Optional<Email> email = Optional.ofNullable(values.get("e")).map(Email::forStudent);
            return new UpdateCommand(index, name, telegram, email);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    private Index parseIndex(String text) throws ParseException {
        String trimmed = text.replaceAll("^ +| +$", "");
        if (trimmed.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UpdateCommand.MESSAGE_USAGE));
        }
        try {
            if (!trimmed.matches("[1-9][0-9]*")) {
                throw new NumberFormatException();
            }
            return Index.fromOneBased(Integer.parseInt(trimmed));
        } catch (NumberFormatException e) {
            throw new ParseException(INDEX_CONSTRAINTS);
        }
    }
}

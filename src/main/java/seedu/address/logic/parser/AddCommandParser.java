package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramHandle;

/** Parses the student creation grammar without silently trimming control characters. */
public class AddCommandParser implements Parser<AddCommand> {
    private static final Pattern PREFIX = Pattern.compile("(?<![^ ])([^ /\\s]+)/");
    private static final List<String> REQUIRED = List.of("n", "p", "e", "c");

    @Override
    public AddCommand parse(String args) throws ParseException {
        Map<String, List<String>> values = tokenize(args);
        for (String prefix : REQUIRED) {
            if (!values.containsKey(prefix)) {
                throw new ParseException("Missing required parameter: " + prefix + "/.");
            }
        }
        try {
            Name name = Name.forStudent(values.get("n").getFirst());
            TelegramHandle telegram = new TelegramHandle(values.get("p").getFirst());
            Email email = Email.forStudent(values.get("e").getFirst());
            List<ClassMembership> classes = new ArrayList<>();
            for (String value : values.get("c")) {
                classes.add(new ClassMembership(value));
            }
            Set<String> courses = new LinkedHashSet<>();
            for (ClassMembership membership : classes) {
                if (!courses.add(membership.courseCode)) {
                    throw new ParseException("Course " + membership.courseCode
                            + " may be specified only once per student.");
                }
            }
            return new AddCommand(new Person(name, telegram, email, new LinkedHashSet<>(classes)));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /** Recognizes prefix tokens at ordinary-space boundaries, preserving every value byte. */
    private Map<String, List<String>> tokenize(String args) throws ParseException {
        Map<String, List<String>> values = new LinkedHashMap<>();
        Matcher matcher = PREFIX.matcher(args);
        String previous = null;
        int valueStart = 0;
        while (matcher.find()) {
            if ("n".equals(previous) && ParserUtil.isNameRelationshipMarker(args, matcher.start())) {
                continue;
            }
            if (previous == null && !args.substring(0, matcher.start()).matches(" *")) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
            }
            if (previous != null) {
                values.get(previous).add(args.substring(valueStart, matcher.start()));
            }
            String prefix = matcher.group(1);
            if (!REQUIRED.contains(prefix)) {
                throw new ParseException("Unknown parameter: " + prefix + "/.");
            }
            if (values.containsKey(prefix) && !prefix.equals("c")) {
                throw new ParseException("Parameter " + prefix + "/ must be specified only once.");
            }
            values.computeIfAbsent(prefix, key -> new ArrayList<>());
            previous = prefix;
            valueStart = matcher.end();
        }
        if (previous != null) {
            values.get(previous).add(args.substring(valueStart));
        } else if (!args.matches(" *")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        return values;
    }
}

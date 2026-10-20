package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses batch indices against the strict MVP integer syntax. */
public class DeleteCommandParser implements Parser<DeleteCommand> {
    @Override
    public DeleteCommand parse(String args) throws ParseException {
        String text = args.replaceAll("^ +| +$", "");
        if (text.isEmpty() || text.matches("[^ /]+/.*")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }
        List<Index> indices = new ArrayList<>();
        for (String value : text.split(" +")) {
            try {
                if (!value.matches("[1-9][0-9]*")) {
                    throw new NumberFormatException();
                }
                indices.add(Index.fromOneBased(Integer.parseInt(value)));
            } catch (NumberFormatException e) {
                throw new ParseException(MarkCommandParser.INDEX_CONSTRAINTS);
            }
        }
        return new DeleteCommand(indices);
    }
}

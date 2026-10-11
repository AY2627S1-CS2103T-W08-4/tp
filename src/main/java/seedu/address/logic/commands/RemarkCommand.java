package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/**
 * Changes a person's remark using their displayed index.
 */
public class RemarkCommand extends Command {

    public static final String COMMAND_WORD = "remark";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Changes or removes a person's remark.\n"
            + "Parameters: INDEX (a positive integer) r/[REMARK]\n"
            + "Example: remark 1 r/Likes swimming";

    public static final String MESSAGE_ADD_REMARK_SUCCESS =
            "Added remark to person: %1$s";
    public static final String MESSAGE_DELETE_REMARK_SUCCESS =
            "Removed remark from person: %1$s";

    private final Index index;
    private final Remark remark;

    /**
     * Creates a command with the target index and remark.
     */
    public RemarkCommand(Index index, Remark remark) {
        requireAllNonNull(index, remark);
        this.index = index;
        this.remark = remark;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person originalPerson = lastShownList.get(index.getZeroBased());
        Person updatedPerson = new Person(
                originalPerson.getName(),
                originalPerson.getPhone(),
                originalPerson.getEmail(),
                originalPerson.getAddress(),
                remark,
                originalPerson.getTags());

        model.setPerson(originalPerson, updatedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        String message = remark.value.isEmpty()
                ? MESSAGE_DELETE_REMARK_SUCCESS
                : MESSAGE_ADD_REMARK_SUCCESS;
        return new CommandResult(String.format(message, Messages.format(updatedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RemarkCommand otherCommand)) {
            return false;
        }
        return index.equals(otherCommand.index)
                && remark.equals(otherCommand.remark);
    }
}

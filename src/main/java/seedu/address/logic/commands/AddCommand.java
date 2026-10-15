package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = "Format: add n/NAME p/TELEGRAM e/EMAIL "
            + "c/COURSE:GROUP [c/COURSE:GROUP ...]";
    public static final String MESSAGE_SUCCESS = "New student added: %1$s.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";
    public static final String MESSAGE_DUPLICATE_EMAIL = "A student with this email already exists.";
    public static final String MESSAGE_DUPLICATE_TELEGRAM = "A student with this Telegram handle already exists.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (toAdd.isStudent()) {
            boolean duplicateEmail = model.getAddressBook().getPersonList().stream()
                    .anyMatch(person -> person.getEmail().value.equalsIgnoreCase(toAdd.getEmail().value));
            if (duplicateEmail) {
                throw new CommandException(MESSAGE_DUPLICATE_EMAIL);
            }
            boolean duplicateTelegram = model.getAddressBook().getPersonList().stream()
                    .anyMatch(person -> person.getTelegramHandle().equals(toAdd.getTelegramHandle()));
            if (duplicateTelegram) {
                throw new CommandException(MESSAGE_DUPLICATE_TELEGRAM);
            }
        } else if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.addPerson(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd.getName()), toAdd);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}

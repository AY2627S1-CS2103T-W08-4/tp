package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramHandle;

/** Corrects student contacts without removing their enrolments or attendance history. */
public class UpdateCommand extends Command {
    public static final String COMMAND_WORD = "update";
    public static final String MESSAGE_USAGE = "Format: update INDEX [n/NAME] [p/TELEGRAM] [e/EMAIL]";
    public static final String MESSAGE_NOT_EDITED = "At least one field to update must be provided.";
    public static final String MESSAGE_LEGACY = "Use edit to change a legacy phone/address contact.";
    public static final String INVALID_INDEX = "The student index provided is invalid.";

    private final Index index;
    private final Optional<Name> name;
    private final Optional<TelegramHandle> telegram;
    private final Optional<Email> email;

    /** Empty optionals preserve the current field; at least one replacement is required. */
    public UpdateCommand(Index index, Optional<Name> name, Optional<TelegramHandle> telegram, Optional<Email> email) {
        this.index = requireNonNull(index);
        this.name = requireNonNull(name);
        this.telegram = requireNonNull(telegram);
        this.email = requireNonNull(email);
        if (name.isEmpty() && telegram.isEmpty() && email.isEmpty()) {
            throw new IllegalArgumentException(MESSAGE_NOT_EDITED);
        }
    }

    /** Resolves against the current display, before changing any record. */
    public Person getTarget(Model model) throws CommandException {
        requireNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(INVALID_INDEX);
        }
        Person target = model.getFilteredPersonList().get(index.getZeroBased());
        if (!target.isStudent()) {
            throw new CommandException(MESSAGE_LEGACY);
        }
        return target;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person target = getTarget(model);
        Person updated = target.withContactDetails(name.orElse(target.getName()),
                telegram.orElse(target.getTelegramHandle().orElseThrow()), email.orElse(target.getEmail()));
        List<Person> others = model.getAddressBook().getPersonList().stream()
                .filter(person -> !person.equals(target)).toList();
        if (others.stream().anyMatch(person -> person.getEmail().value.equalsIgnoreCase(updated.getEmail().value))) {
            throw new CommandException(AddCommand.MESSAGE_DUPLICATE_EMAIL);
        }
        if (others.stream().anyMatch(person -> person.getTelegramHandle().equals(updated.getTelegramHandle()))) {
            throw new CommandException(AddCommand.MESSAGE_DUPLICATE_TELEGRAM);
        }
        if (updated.equals(target)) {
            return new CommandResult("Student unchanged: " + target.getName() + ".", target);
        }
        assert updated.getAttendance().equals(target.getAttendance());
        model.setPerson(target, updated);
        return new CommandResult("Student updated: " + updated.getName() + ".", updated);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof UpdateCommand command && index.equals(command.index)
                && name.equals(command.name) && telegram.equals(command.telegram) && email.equals(command.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, name, telegram, email);
    }
}

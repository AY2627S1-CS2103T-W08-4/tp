package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.MarkCommand;
import seedu.address.logic.commands.UpdateCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    public static final String ADD_SAVE_ERROR = "Unable to save changes. No changes were applied. "
            + "Check available disk space and file permissions, then try again.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        if (command instanceof FindCommand || command instanceof ListCommand) {
            // Queries are read-only and must also work when the data directory is not writable.
            return command.execute(model);
        }
        if (command instanceof DeleteCommand delete) {
            List<Person> targets = delete.getTargets(model);
            Model candidate = new ModelManager(model.getAddressBook(), model.getUserPrefs());
            List<Person> displayed = List.copyOf(model.getFilteredPersonList());
            candidate.updateFilteredPersonList(displayed::contains);
            commandResult = delete.execute(candidate);
            try {
                storage.saveAddressBook(candidate.getAddressBook());
            } catch (IOException e) {
                throw new CommandException(ADD_SAVE_ERROR, e);
            }
            targets.forEach(model::deletePerson);
            return commandResult;
        }
        if (command instanceof MarkCommand mark) {
            return executeStudentReplacement(mark);
        }
        if (command instanceof UpdateCommand update) {
            return executeStudentReplacement(update);
        }
        if (command instanceof AddCommand) {
            // Execute against a detached candidate: failures leave the live list, predicate and selection untouched.
            Model candidate = new ModelManager(model.getAddressBook(), model.getUserPrefs());
            commandResult = command.execute(candidate);
            try {
                storage.saveAddressBook(candidate.getAddressBook());
            } catch (IOException e) {
                throw new CommandException(ADD_SAVE_ERROR, e);
            }
            model.addPerson(commandResult.getSelectedPerson().orElseThrow());
            return commandResult;
        }
        commandResult = command.execute(model);

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            if (command instanceof ExitCommand) {
                throw new CommandException(ExitCommand.MESSAGE_SAVE_FAILURE, ioe);
            }
            if (ioe instanceof AccessDeniedException) {
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    /**
     * Saves all detached replacements before publishing them to the live roster.
     * Mark and update replace students in place: roster size and ordering remain stable.
     */
    private CommandResult executeStudentReplacement(Command command) throws CommandException {
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        Model candidate = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        candidate.updateFilteredPersonList(displayed::contains);
        CommandResult result = command.execute(candidate);
        List<Person> proposed = List.copyOf(candidate.getAddressBook().getPersonList());
        if (!proposed.equals(original)) {
            try {
                storage.saveAddressBook(candidate.getAddressBook());
            } catch (IOException e) {
                logger.warning("Student change could not be saved; live data was retained.");
                throw new CommandException(ADD_SAVE_ERROR, e);
            }
            for (int i = 0; i < original.size(); i++) {
                if (!original.get(i).equals(proposed.get(i))) {
                    model.setPerson(original.get(i), proposed.get(i));
                }
            }
        }
        return result;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}

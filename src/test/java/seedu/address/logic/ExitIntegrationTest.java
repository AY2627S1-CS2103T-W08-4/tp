package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class ExitIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private FailingStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(AMY);
        model.addPerson(BOB);
        model.updateFilteredPersonList(AMY::equals);
        storage = new FailingStorage(temporaryFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_ioFailure_preservesDataViewAndFile() throws Exception {
        assertFailedExitPreservesState(new IOException("Simulated disk failure"));
    }

    @Test
    public void execute_permissionFailure_preservesDataViewAndFile() throws Exception {
        assertFailedExitPreservesState(new AccessDeniedException("Simulated permission failure"));
    }

    @Test
    public void execute_invalidExit_neverAttemptsSave() throws Exception {
        storage.failure = new IOException("Saving must not be attempted");
        String savedData = Files.readString(storage.getAddressBookFilePath());
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExitCommand.MESSAGE_USAGE);

        ParseException error = assertThrows(ParseException.class, () -> logic.execute("exit anything"));

        assertEquals(expectedMessage, error.getMessage());
        assertEquals(1, storage.saveCount);
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_failedExitThenRetry_returnsExitOnlyAfterSaveSucceeds() throws Exception {
        storage.failure = new IOException("Simulated disk failure");
        assertThrows(CommandException.class, () -> logic.execute("exit"));
        storage.failure = null;

        assertTrue(logic.execute("exit").isExit());
        assertEquals(3, storage.saveCount);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_exitAndReload_preservesChangesToExistingContacts() throws Exception {
        logic.execute("edit 1 n/Amy Revised");
        logic.execute("list");
        logic.execute("delete 2");
        assertTrue(logic.execute("exit").isExit());

        JsonAddressBookStorage reopenedStorage = new JsonAddressBookStorage(storage.getAddressBookFilePath());
        Model reopenedModel = new ModelManager(reopenedStorage.readAddressBook().orElseThrow(), new UserPrefs());

        assertEquals(model.getAddressBook(), reopenedModel.getAddressBook());
        assertEquals(1, reopenedModel.getFilteredPersonList().size());
        assertEquals("Amy Revised", reopenedModel.getFilteredPersonList().get(0).getName().fullName);
    }

    private void assertFailedExitPreservesState(IOException failure) throws Exception {
        Model expectedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        expectedModel.updateFilteredPersonList(AMY::equals);
        String savedData = Files.readString(storage.getAddressBookFilePath());
        storage.failure = failure;

        CommandException error = assertThrows(CommandException.class, () -> logic.execute("exit"));

        assertEquals(ExitCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
        assertSame(failure, error.getCause());
        assertEquals(expectedModel, model);
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
    }

    private static class FailingStorage extends JsonAddressBookStorage {
        private IOException failure;
        private int saveCount;

        FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveCount++;
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}

package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies legacy edit recovery and access to Help during a storage failure.
 */
public class LegacyRecoveryIntegrationTest {
    @TempDir
    public Path directory;

    private ModelManager model;
    private FailingStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Legacy Alice").withPhone("91234567").build());
        storage = new FailingStorage(directory.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        storage.saves = 0;
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void edit_failedSave_preservesLiveDataAndDiskWithoutNotifications() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        List<String> events = new ArrayList<>();
        model.getFilteredPersonList().addListener((ListChangeListener<Person>) change -> events.add("changed"));
        storage.fail = true;
        assertThrows(CommandException.class, LogicManager.ADD_SAVE_ERROR, () -> logic.execute("edit 1 p/92345678"));
        assertEquals(before, model.getAddressBook());
        assertTrue(events.isEmpty());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        storage.fail = false;
        logic.execute("exit");
        assertEquals("91234567", storage.readAddressBook().orElseThrow().getPersonList().getFirst().getPhone().value);
    }

    @Test
    public void edit_failedSaveThenRetry_persistsOnlyTheSuccessfulChange() throws Exception {
        storage.fail = true;
        assertThrows(CommandException.class, () -> logic.execute("edit 1 p/92345678"));
        storage.fail = false;
        logic.execute("edit 1 p/93456789");
        assertEquals("93456789", model.getFilteredPersonList().getFirst().getPhone().value);
        assertEquals("93456789", storage.readAddressBook().orElseThrow().getPersonList().getFirst().getPhone().value);
    }

    @Test
    public void edit_filteredLegacyIndex_targetsDisplayedContactAndRestoresFullRoster() throws Exception {
        Person second = new PersonBuilder().withName("Legacy Bob").withEmail("bob@example.com").build();
        model.addPerson(second);
        model.updateFilteredPersonList(second::equals);
        assertTrue(logic.execute("edit 1 p/93456789").isClearSelection());
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals("91234567", model.getFilteredPersonList().getFirst().getPhone().value);
        assertEquals("93456789", model.getFilteredPersonList().getLast().getPhone().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void help_duringSaveFailure_remainsReadOnlyAndAvailable() throws Exception {
        storage.fail = true;
        assertTrue(logic.execute("help").isShowHelp());
        assertEquals(0, storage.saves);
    }

    /**
     * Rejects selected saves before writing, while using real JSON storage otherwise.
     */
    private static class FailingStorage extends JsonAddressBookStorage {
        private boolean fail;
        private int saves;

        FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            saves++;
            if (fail) {
                throw new IOException("Injected recovery save failure");
            }
            super.saveAddressBook(data);
        }
    }
}

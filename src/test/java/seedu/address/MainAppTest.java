package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {
    @TempDir
    public Path directory;

    @Test
    public void initialize_invalidData_stopsWithoutOpeningOrOverwritingRoster() throws Exception {
        Path data = directory.resolve("students.json");
        for (String invalid : new String[]{"not json", "null", "{}", "{\"persons\":null}",
            "{\"persons\":[null]}", "{\"persons\":[{\"name\":\"Incomplete\"}]}"}) {
            Files.writeString(data, invalid);
            TrackingStorage studentStorage = new TrackingStorage(data);
            MainApp app = initialize(studentStorage);

            assertNull(app.model);
            assertNull(app.logic);
            assertNull(app.ui);
            app.stop();
            assertEquals(invalid, Files.readString(data));
            assertEquals(0, studentStorage.saves);
        }
    }

    @Test
    public void initialize_unreadableData_preservesFileAndCanRecoverOnNextStart() throws Exception {
        Path data = directory.resolve("students.json");
        TrackingStorage studentStorage = new TrackingStorage(data);
        studentStorage.saveAddressBook(getTypicalAddressBook());
        studentStorage.saves = 0;
        String saved = Files.readString(data);
        studentStorage.denyRead = true;

        MainApp failed = initialize(studentStorage);
        assertNull(failed.model);
        failed.stop();
        assertEquals(saved, Files.readString(data));
        assertEquals(0, studentStorage.saves);

        studentStorage.denyRead = false;
        MainApp reopened = initialize(studentStorage);
        assertEquals(getTypicalAddressBook(), reopened.model.getAddressBook());
        assertTrue(reopened.logic.execute("exit").isExit());
        reopened.stop();
    }

    @Test
    public void initialize_directoryInsteadOfFile_stopsWithoutReplacingDirectory() throws Exception {
        Path data = Files.createDirectory(directory.resolve("students.json"));
        Path original = data.resolve("keep.txt");
        Files.writeString(original, "keep");
        MainApp app = initialize(new TrackingStorage(data));
        assertNull(app.model);
        app.stop();
        assertTrue(Files.isDirectory(data));
        assertEquals("keep", Files.readString(original));
    }

    @Test
    public void initialize_missingData_usesSamplesOnlyForMissingFile() throws Exception {
        Path data = directory.resolve("missing.json");
        MainApp app = initialize(new TrackingStorage(data));
        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
        assertFalse(Files.exists(data));
        app.stop();
        assertFalse(Files.exists(data));
    }

    @Test
    public void initialize_savedEmptyRoster_doesNotRestoreSamples() throws Exception {
        Path data = directory.resolve("empty.json");
        Files.writeString(data, "{\"persons\":[]}");
        MainApp app = initialize(new TrackingStorage(data));
        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        app.stop();
        assertEquals("{\"persons\":[]}", Files.readString(data));
    }

    @Test
    public void initialize_repairedData_loadsWithoutDiscardingRecords() throws Exception {
        Path data = directory.resolve("students.json");
        Files.writeString(data, "broken");
        MainApp failed = initialize(new TrackingStorage(data));
        failed.stop();
        new JsonAddressBookStorage(data).saveAddressBook(getTypicalAddressBook());

        MainApp reopened = initialize(new TrackingStorage(data));
        assertEquals(getTypicalAddressBook(), reopened.model.getAddressBook());
        reopened.stop();
    }

    private MainApp initialize(TrackingStorage studentStorage) {
        MainApp app = new MainApp();
        app.initialize(new StorageManager(studentStorage, new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        return app;
    }

    private static class TrackingStorage extends JsonAddressBookStorage {
        private boolean denyRead;
        private int saves;

        TrackingStorage(Path path) {
            super(path);
        }

        @Override
        public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
            if (denyRead) {
                throw new DataLoadingException(new AccessDeniedException(getAddressBookFilePath().toString()));
            }
            return super.readAddressBook();
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saves++;
            super.saveAddressBook(addressBook);
        }
    }
}

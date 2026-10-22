package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class PersistenceWorkflowTest {
    @TempDir
    public Path directory;

    @Test
    public void studentChanges_exitAndNewStartup_preserveProfilesAttendanceAndPreferences() throws Exception {
        Files.writeString(directory.resolve("students.json"), "{\"persons\":[]}");
        MainApp app = initialize();
        app.logic.execute("add n/Alice p/@alice_1 e/alice@example.com c/CS2103T:T04 c/MA2001:T01");
        app.logic.execute("add n/Bobby p/@bobby_1 e/bob@example.com c/CS2103T:T05");
        app.logic.execute("add n/Carol p/@carol_1 e/carol@example.com c/CS2103T:T04");
        app.logic.execute("find c/CS2103T:T04 w/1");
        app.logic.execute("mark 1 m/CS2103T w/1 s/present");
        app.logic.execute("mark 1 m/CS2103T w/1 s/absent");
        app.logic.execute("mark 1 m/CS2103T w/2 s/present");
        app.logic.execute("mark 1 m/CS2103T w/2 s/unrecorded");
        app.logic.execute("mark 1 m/MA2001 w/53 s/present");
        app.logic.execute("update 1 n/Alice Tan p/@alice_new e/alice.new@example.com");
        app.logic.execute("delete 2");
        List<Person> saved = List.copyOf(app.model.getAddressBook().getPersonList());
        GuiSettings settings = new GuiSettings(1000, 700, 50, 60);
        app.logic.setGuiSettings(settings);
        assertTrue(app.logic.execute("exit").isExit());
        app.stop();

        MainApp reopened = initialize();
        List<Person> restored = reopened.model.getAddressBook().getPersonList();
        assertEquals(saved, restored);
        assertEquals(2, restored.size());
        Person alice = restored.getFirst();
        assertEquals("Alice Tan", alice.getName().fullName);
        assertEquals("@alice_new", alice.getTelegramHandle().orElseThrow().value);
        assertEquals("alice.new@example.com", alice.getEmail().value);
        assertEquals(2, alice.getMemberships().size());
        assertEquals(2, alice.getAttendance().size());
        assertEquals(AttendanceStatus.ABSENT, alice.getAttendanceStatus("CS2103T", new AttendanceWeek("1")));
        assertEquals(AttendanceStatus.UNRECORDED, alice.getAttendanceStatus("CS2103T", new AttendanceWeek("2")));
        assertEquals(AttendanceStatus.PRESENT, alice.getAttendanceStatus("MA2001", new AttendanceWeek("53")));
        assertEquals("Bobby", restored.getLast().getName().fullName);
        assertTrue(restored.getLast().getAttendance().isEmpty());
        assertEquals(settings, reopened.model.getGuiSettings());
        reopened.stop();
    }

    private MainApp initialize() {
        MainApp app = new MainApp();
        app.initialize(new StorageManager(new JsonAddressBookStorage(directory.resolve("students.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        return app;
    }
}

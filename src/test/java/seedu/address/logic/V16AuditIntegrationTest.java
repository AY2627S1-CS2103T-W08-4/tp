package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises independent state oracles and real filesystem failures across command families. */
public class V16AuditIntegrationTest {
    @TempDir
    public Path directory;

    @Test
    public void mutations_realReplacementFailure_preserveMemoryDiskAndFilterThenRetry() throws Exception {
        for (String command : List.of(
                "add n/Carol p/@carol_one e/carol@example.com c/CS2103T:T04",
                "update 1 e/new@example.com", "mark 2 1 m/CS2103T w/53 s/present",
                "delete 2 1 2", "clear", "exit", "edit 3 p/93456789")) {
            Path folder = Files.createTempDirectory(directory, "failure-");
            ModelManager model = fixture();
            JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("students.json"));
            storage.saveAddressBook(model.getAddressBook());
            LogicManager logic = logic(model, storage);
            if (!command.startsWith("edit")) {
                logic.execute("find c/CS2103T:T04 w/53");
            }
            AddressBook before = new AddressBook(model.getAddressBook());
            List<Person> displayed = List.copyOf(model.getFilteredPersonList());
            String saved = Files.readString(storage.getAddressBookFilePath());
            Path backup = folder.resolve("backup.json");
            Files.move(storage.getAddressBookFilePath(), backup);
            Files.createDirectory(storage.getAddressBookFilePath());
            Path obstruction = storage.getAddressBookFilePath().resolve("keep.txt");
            Files.writeString(obstruction, "keep");

            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(displayed, model.getFilteredPersonList(), command);
            assertEquals(saved, Files.readString(backup), command);
            assertEquals("keep", Files.readString(obstruction), command);
            try (var files = Files.list(folder)) {
                assertFalse(files.anyMatch(path -> path.getFileName().toString().startsWith(".tutortrack-")));
            }

            Files.delete(obstruction);
            Files.delete(storage.getAddressBookFilePath());
            Files.move(backup, storage.getAddressBookFilePath());
            logic.execute(command);
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow(), command);
        }
    }

    @Test
    public void queriesAndNormalizedNoOps_realBlockedTarget_doNotWrite() throws Exception {
        ModelManager model = fixture();
        Path target = Files.createDirectory(directory.resolve("students.json"));
        Files.writeString(target.resolve("keep.txt"), "keep");
        LogicManager logic = logic(model, new JsonAddressBookStorage(target));
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : List.of("list", "select 2", "help", "find c/CS2103T:T04 w/53",
                "mark 2 1 2 m/CS2103T w/53 s/unrecorded", "update 1 e/ALICE@EXAMPLE.COM")) {
            logic.execute(command);
            assertEquals(before, model.getAddressBook(), command);
        }
        assertEquals("keep", Files.readString(target.resolve("keep.txt")));
    }

    @Test
    public void invalidCommands_preserveFilteredRosterAndAllSavedBytes() throws Exception {
        ModelManager model = fixture();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        LogicManager logic = logic(model, storage);
        logic.execute("find c/CS2103T:T04 w/1");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String saved = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("delete 1 3", "mark 1 3 m/CS2103T w/1 s/present",
                "mark 1 2 m/MA2001 w/1 s/present", "update 1 e/legacy@example.com",
                "update 1 p/@bobby_one", "select 3", "clear 1", "list\t", "exit\n",
                "find c/CS2103T:T04 w/01", "mark 1 m/CS2103T w/54 s/present",
                "add n/Carol p/@carol_one e/carol@example.com c/CS2103T:T04 c/cs2103t:t05")) {
            try {
                logic.execute(command);
                throw new AssertionError("Invalid command succeeded: " + command);
            } catch (ParseException | CommandException expected) {
                assertFalse(expected.getMessage().isBlank(), command);
            }
            assertEquals(before, model.getAddressBook(), command);
            assertEquals(displayed, model.getFilteredPersonList(), command);
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()), command);
        }
    }

    @Test
    public void generatedContactAttendanceAndDeletionSequences_matchIndependentOracleAndReload() throws Exception {
        for (long seed : List.of(2103L, 5301L, 1606L)) {
            Path folder = Files.createTempDirectory(directory, "sequence-");
            ModelManager model = new ModelManager();
            JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("students.json"));
            LogicManager logic = logic(model, storage);
            List<ExpectedStudent> expected = new ArrayList<>();
            Random random = new Random(seed);
            int nextId = 0;
            String group = null;
            for (int step = 0; step < 160; step++) {
                String context = "seed=" + seed + ", step=" + step;
                List<ExpectedStudent> visible = visible(expected, group);
                int action = random.nextInt(7);
                if (expected.isEmpty() || action == 0) {
                    ExpectedStudent added = new ExpectedStudent(nextId++);
                    logic.execute("add n/" + added.name + " p/" + added.telegram + " e/" + added.email
                            + " c/CS2103T:" + added.group + " c/MA2001:T01");
                    expected.add(added);
                    group = null;
                } else if (action == 1) {
                    group = random.nextBoolean() ? "T04" : "T05";
                    logic.execute("find c/CS2103T:" + group + " w/" + (random.nextBoolean() ? 1 : 53));
                } else if (action == 2) {
                    logic.execute("list");
                    group = null;
                } else if (!visible.isEmpty()) {
                    int index = random.nextInt(visible.size());
                    ExpectedStudent target = visible.get(index);
                    if (action == 3) {
                        String course = random.nextBoolean() ? "CS2103T" : "MA2001";
                        int week = random.nextBoolean() ? 1 : 53;
                        String status = List.of("Present", "Absent", "Unrecorded").get(random.nextInt(3));
                        logic.execute("mark " + (index + 1) + " " + (index + 1) + " m/" + course
                                + " w/" + week + " s/" + status);
                        String key = course + ":" + week;
                        if (status.equals("Unrecorded")) {
                            target.attendance.remove(key);
                        } else {
                            target.attendance.put(key, status);
                        }
                    } else if (action == 4) {
                        target.email = "changed." + seed + "." + step + "@example.com";
                        logic.execute("update " + (index + 1) + " e/"
                                + target.email.toUpperCase(java.util.Locale.ROOT));
                    } else if (action == 5) {
                        logic.execute("delete " + (index + 1) + " " + (index + 1));
                        expected.remove(target);
                    } else {
                        assertEquals(target.name, logic.execute("select " + (index + 1))
                                .getSelectedPerson().orElseThrow().getName().fullName, context);
                    }
                }
                assertStudents(expected, model.getAddressBook().getPersonList(), context);
                assertStudents(visible(expected, group), model.getFilteredPersonList(), context);
                assertStudents(expected, storage.readAddressBook().orElseThrow().getPersonList(), context);
            }
            assertTrue(logic.execute("exit").isExit());
            ModelManager reopened = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
            assertStudents(expected, reopened.getFilteredPersonList(), "restart seed=" + seed);
        }
    }

    private static ModelManager fixture() {
        ModelManager model = new ModelManager();
        var parser = new seedu.address.logic.parser.AddressBookParser();
        try {
            parser.parseCommand("add n/Alice p/@alice_one e/alice@example.com c/CS2103T:T04 c/MA2001:T01")
                    .execute(model);
            parser.parseCommand("add n/Bobby p/@bobby_one e/bobby@example.com c/CS2103T:T04").execute(model);
        } catch (ParseException | CommandException e) {
            throw new AssertionError(e);
        }
        model.addPerson(new PersonBuilder().withName("Legacy").withEmail("legacy@example.com").build());
        return model;
    }

    private LogicManager logic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    private static List<ExpectedStudent> visible(List<ExpectedStudent> students, String group) {
        return students.stream().filter(student -> group == null || group.equals(student.group)).toList();
    }

    private static void assertStudents(List<ExpectedStudent> expected, List<Person> actual, String context) {
        assertEquals(expected.size(), actual.size(), context);
        for (int i = 0; i < expected.size(); i++) {
            ExpectedStudent wanted = expected.get(i);
            Person found = actual.get(i);
            assertEquals(wanted.name, found.getName().fullName, context);
            assertEquals(wanted.email, found.getEmail().value, context);
            assertEquals(wanted.telegram, found.getTelegramHandle().orElseThrow().value, context);
            assertEquals(List.of("CS2103T:" + wanted.group, "MA2001:T01"),
                    found.getMemberships().stream().map(Object::toString).toList(), context);
            Map<String, String> decisions = new TreeMap<>();
            found.getAttendance().forEach(record ->
                    decisions.put(record.course + ":" + record.week, "" + record.status));
            assertEquals(wanted.attendance, decisions, context);
        }
    }

    private static class ExpectedStudent {
        private final String name;
        private final String telegram;
        private final String group;
        private String email;
        private final Map<String, String> attendance = new LinkedHashMap<>();

        ExpectedStudent(int id) {
            String suffix = "" + (char) ('a' + id / 26) + (char) ('a' + id % 26);
            name = "Student " + suffix;
            telegram = "@audit_" + suffix;
            email = "audit." + suffix + "@example.com";
            group = id % 2 == 0 ? "T04" : "T05";
        }
    }
}

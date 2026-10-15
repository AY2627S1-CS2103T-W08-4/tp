package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.StudentBuilder.typicalStudent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.testutil.PersonBuilder;

public class StudentStorageTest {
    @TempDir
    public Path folder;

    private JsonAdaptedPerson adapted(String name, String telegram, String email, List<String> memberships) {
        return new JsonAdaptedPerson(name, null, email, null, null, telegram, memberships);
    }

    @Test
    public void student_roundTrip_retainsCompleteNormalizedProfile() throws Exception {
        assertEquals(typicalStudent(), new JsonAdaptedPerson(typicalStudent()).toModelType());
        assertEquals(typicalStudent(), adapted(" Anne-Marie   Tan ", "@Anne_Tan", "Anne@Example.com",
                List.of("cs2103t:t04")).toModelType());
    }

    @Test
    public void student_missingOrInvalidFields_neverLoadsAsLegacy() {
        for (JsonAdaptedPerson invalid : List.of(
                adapted(null, "@anne_tan", "anne@example.com", List.of("CS2103T:T04")),
                adapted("Anne", null, "anne@example.com", List.of("CS2103T:T04")),
                adapted("Anne", "@anne_tan", null, List.of("CS2103T:T04")),
                adapted("Anne", "@anne_tan", "anne@example.com", null),
                adapted("Anne", "@anne_tan", "anne@example.com", List.of()),
                adapted("Anne2", "@anne_tan", "anne@example.com", List.of("CS2103T:T04")),
                adapted("Anne", "@bad", "anne@example.com", List.of("CS2103T:T04")),
                adapted("Anne", "@anne_tan", "anne@localhost", List.of("CS2103T:T04")),
                adapted("Anne", "@anne_tan", "anne@example.com", List.of("T04")),
                adapted("Anne", "@anne_tan", "anne@example.com", List.of("CS2103T:T04", "cs2103t:t04")),
                adapted("Anne", "@anne_tan", "anne@example.com", List.of("CS2103T:T04", "CS2103T:T05")))) {
            assertThrows(IllegalValueException.class, invalid::toModelType);
        }
        JsonAdaptedPerson hybrid = new JsonAdaptedPerson("Anne", "98765432", "anne@example.com", "Road", null,
                "@anne_tan", List.of("CS2103T:T04"));
        assertThrows(IllegalValueException.class, hybrid::toModelType);
    }

    @Test
    public void legacy_roundTrip_preservesOriginalValuesWithoutInventingStudentFields() throws Exception {
        var legacy = new PersonBuilder().withName("Agent 007").withEmail("UPPER@example.com").build();
        var reloaded = new JsonAdaptedPerson(legacy).toModelType();
        assertEquals(legacy, reloaded);
        assertFalse(reloaded.isStudent());
        assertEquals("UPPER@example.com", reloaded.getEmail().value);
    }

    @Test
    public void load_duplicateNormalizedContacts_rejectsEntireDataset() throws Exception {
        String student = "{\"name\":\"Anne\",\"telegram\":\"@anne_tan\","
                + "\"email\":\"anne@example.com\",\"memberships\":[\"CS2103T:T04\"]}";
        Path path = folder.resolve("duplicate.json");
        Files.writeString(path, "{\"persons\":[" + student + ","
                + student.replace("anne@example.com", "ANNE@EXAMPLE.COM").replace("@anne_tan", "@other_one") + "]}");
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(path).readAddressBook());
        Files.writeString(path, "{\"persons\":[" + student + ","
                + student.replace("anne@example.com", "other@example.com").replace("@anne_tan", "@ANNE_TAN") + "]}");
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(path).readAddressBook());
    }

    @Test
    public void save_serializationFailure_preservesExistingFileAndCleansTemporaryFile() throws Exception {
        Path path = folder.resolve("students.json");
        Files.writeString(path, "previous file contents");
        AddressBook unreadable = new AddressBook() {
            @Override
            public javafx.collections.ObservableList<seedu.address.model.person.Person> getPersonList() {
                throw new IllegalStateException("Unable to serialize");
            }
        };
        assertThrows(IllegalStateException.class, () -> new JsonAddressBookStorage(path).saveAddressBook(unreadable));
        assertEquals("previous file contents", Files.readString(path));
        try (var files = Files.list(folder)) {
            assertEquals(List.of(path), files.toList());
        }
    }

    @Test
    public void save_failedReplacement_preservesTargetAndCleansTemporaryFile() throws Exception {
        Path target = Files.createDirectory(folder.resolve("existing-directory"));
        Path original = target.resolve("keep.txt");
        Files.writeString(original, "keep");
        assertThrows(IOException.class, () -> new JsonAddressBookStorage(target).saveAddressBook(new AddressBook()));
        assertEquals("keep", Files.readString(original));
        try (var files = Files.list(folder)) {
            assertEquals(List.of(target), files.toList());
        }
    }
}

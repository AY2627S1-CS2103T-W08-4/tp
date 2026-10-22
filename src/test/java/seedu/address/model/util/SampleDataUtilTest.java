package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class SampleDataUtilTest {
    @TempDir
    public Path directory;

    @Test
    public void samples_haveUniqueStudentContactsAndUnrecordedAttendance() {
        Set<String> emails = new HashSet<>();
        Set<String> handles = new HashSet<>();
        Set<String> courses = new HashSet<>();
        Set<String> classes = new HashSet<>();
        boolean hasMultipleCourses = false;
        for (Person student : SampleDataUtil.getSamplePersons()) {
            assertTrue(student.isStudent());
            assertTrue(emails.add(student.getEmail().value));
            assertTrue(handles.add(student.getTelegramHandle().orElseThrow().toString()));
            assertTrue(student.getAttendance().isEmpty());
            hasMultipleCourses |= student.getMemberships().size() > 1;
            student.getMemberships().forEach(membership -> {
                courses.add(membership.courseCode);
                classes.add(membership.toString());
                for (int week = 1; week <= 53; week++) {
                    assertEquals(AttendanceStatus.UNRECORDED, student.getAttendanceStatus(
                            membership.courseCode, new AttendanceWeek(Integer.toString(week))));
                }
            });
        }
        assertTrue(hasMultipleCourses);
        assertTrue(courses.size() > 1);
        assertTrue(classes.size() > courses.size());
    }

    @Test
    public void samples_supportDocumentedClassMarkAndReloadWorkflow() throws Exception {
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("students.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        logic.execute("find c/CS2103T:T04 w/3");
        assertEquals(2, model.getFilteredPersonList().size());
        logic.execute("mark 1 m/CS2103T w/3 s/present");
        Person marked = model.getFilteredPersonList().getFirst();
        assertEquals(AttendanceStatus.PRESENT,
                marked.getAttendanceStatus("CS2103T", new AttendanceWeek("3")));
        assertEquals(AttendanceStatus.UNRECORDED,
                marked.getAttendanceStatus("CS1101S", new AttendanceWeek("3")));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertTrue(SampleDataUtil.getSamplePersons()[0].getAttendance().isEmpty());
    }
}

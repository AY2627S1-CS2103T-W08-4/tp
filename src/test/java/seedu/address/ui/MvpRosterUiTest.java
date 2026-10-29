package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.initializeToolkit;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.AttendanceStatus;
import seedu.address.model.person.AttendanceWeek;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Verifies roster context, selection and details in a real JavaFX window.
 */
public class MvpRosterUiTest {
    @TempDir
    public Path directory;
    private MainWindow window;
    private Stage stage;
    private FailingStorage storage;

    @BeforeAll
    public static void initialize() throws Exception {
        initializeToolkit();
    }

    @BeforeEach
    public void setUp() throws Exception {
        storage = new FailingStorage(directory.resolve("students.json"));
        LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        runOnFxThread(() -> {
            stage = new Stage();
            window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            enter("add n/Alice p/@alice_1 e/alice@example.com c/CS2103T:T04 c/MA2001:T01");
            enter("add n/Bobby p/@bobby_1 e/bobby@example.com c/CS2103T:T04");
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        runOnFxThread(() -> stage.hide());
    }

    @Test
    public void roster_weekChangesRefreshBadgesAndQueriesClearSelection() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText());
            assertNull(list().getSelectionModel().getSelectedItem());
            enter("mark 1 m/CS2103T w/3 s/present");
            assertEquals(0, list().getSelectionModel().getSelectedIndex());
            assertTrue(label("studentDetails").getText().contains("Present"));
            assertTrue(hasBadge("CS2103T | Week 3 | Present"));
            enter("find c/CS2103T:T04 w/4");
            assertTrue(hasBadge("CS2103T | Week 4 | Unrecorded"));
            assertFalse(hasBadge("CS2103T | Week 3 | Present"));
            assertNull(list().getSelectionModel().getSelectedItem());
            enter("find c/MA2001:T09 w/4");
            assertEquals("MA2001:T09 | Week 4 | 0 students", label("rosterHeading").getText());
            assertEquals("No students found in MA2001:T09. Use list to view all students.", placeholderText());
            enter("list");
            assertEquals("All students | 2 students", label("rosterHeading").getText());
            assertFalse(stage.getScene().lookup("#studentDetailsPane").isVisible());
            enter("delete 1 2");
            assertEquals("All students | 0 students", label("rosterHeading").getText());
            assertEquals("No students saved. Use add to create a student.", placeholderText());
        });
    }

    @Test
    public void clear_invalidInputAndSaveFailure_preserveSelectionUntilSuccessfulRetry() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            enter("mark 1 m/CS2103T w/3 s/present");
            Object selected = list().getSelectionModel().getSelectedItem();
            String details = label("studentDetails").getText();
            enter("clear 1");
            assertEquals(selected, list().getSelectionModel().getSelectedItem());
            assertEquals(details, label("studentDetails").getText());
            assertEquals("clear 1", input().getText());
            storage.fail = true;
            enter("clear");
            assertEquals(selected, list().getSelectionModel().getSelectedItem());
            assertEquals(details, label("studentDetails").getText());
            assertEquals("clear", input().getText());
            assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText());
            storage.fail = false;
            enter("clear");
            assertNull(list().getSelectionModel().getSelectedItem());
            assertFalse(stage.getScene().lookup("#studentDetailsPane").isVisible());
            assertEquals("CS2103T:T04 | Week 3 | 0 students", label("rosterHeading").getText());
            assertEquals("", input().getText());
        });
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void failedDelete_preservesSelectionDetailsInputAndContext() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            enter("mark 1 m/CS2103T w/3 s/present");
            Object selected = list().getSelectionModel().getSelectedItem();
            String details = label("studentDetails").getText();
            storage.fail = true;
            enter("delete 1 2");
            assertEquals(selected, list().getSelectionModel().getSelectedItem());
            assertEquals(details, label("studentDetails").getText());
            assertEquals("delete 1 2", input().getText());
            assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText());
            storage.fail = false;
            enter("delete 1");
            assertNull(list().getSelectionModel().getSelectedItem());
            assertFalse(stage.getScene().lookup("#studentDetailsPane").isVisible());
            assertEquals("CS2103T:T04 | Week 3 | 1 student", label("rosterHeading").getText());
            enter("add n/Carol p/@carol_1 e/carol@example.com c/CS1101S:T01");
            assertEquals("All students | 2 students", label("rosterHeading").getText());
            assertEquals(1, list().getSelectionModel().getSelectedIndex());
        });
    }

    @Test
    public void update_refreshesDetailsAndPreservesContext_failureRetainsSelectionAndInput() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            enter("mark 1 m/CS2103T w/3 s/present");
            enter("update 1 n/Alice Tan e/new@example.com");
            assertEquals(0, list().getSelectionModel().getSelectedIndex());
            assertTrue(label("studentDetails").getText().contains("new@example.com"));
            assertTrue(label("studentDetails").getText().contains("Present"));
            assertTrue(hasBadge("CS2103T | Week 3 | Present"));
            assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText());
            Object selected = list().getSelectionModel().getSelectedItem();
            String details = label("studentDetails").getText();
            storage.fail = true;
            enter("update 1 n/Unsaved");
            assertEquals(selected, list().getSelectionModel().getSelectedItem());
            assertEquals(details, label("studentDetails").getText());
            assertEquals("update 1 n/Unsaved", input().getText());
            storage.fail = false;
            enter("update 1 n/Retry");
            assertTrue(label("studentDetails").getText().contains("Retry"));
        });
    }

    @Test
    public void mark_failedSaveOnAnotherStudent_retainsViewAndRetriesSuccessfully() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            enter("mark 1 m/CS2103T w/3 s/present");
            Object selected = list().getSelectionModel().getSelectedItem();
            String details = label("studentDetails").getText();
            String heading = label("rosterHeading").getText();
            List<Person> before = displayedStudents();
            List<String> badges = attendanceBadges();
            String command = "mark 2 m/CS2103T w/3 s/absent";

            storage.fail = true;
            enter(command);
            assertEquals(LogicManager.ADD_SAVE_ERROR, feedback());
            assertEquals(selected, list().getSelectionModel().getSelectedItem());
            assertEquals(details, label("studentDetails").getText());
            assertEquals(heading, label("rosterHeading").getText());
            assertEquals(before, displayedStudents());
            assertEquals(badges, attendanceBadges());
            assertEquals(command, input().getText());

            storage.fail = false;
            enter(command);
            assertEquals("Attendance set: Bobby | CS2103T | Week 3 | Absent.", feedback());
            assertEquals(1, list().getSelectionModel().getSelectedIndex());
            assertEquals("", input().getText());
            assertEquals(heading, label("rosterHeading").getText());
            assertEquals(before.getFirst(), displayedStudents().getFirst());
            assertEquals(List.of("Alice", "Bobby"), displayedStudents().stream()
                    .map(person -> person.getName().fullName).toList());
            assertEquals(AttendanceStatus.ABSENT, displayedStudents().get(1)
                    .getAttendanceStatus("CS2103T", new AttendanceWeek("3")));
            assertTrue(label("studentDetails").getText().contains("Bobby"));
            assertTrue(label("studentDetails").getText().contains("Absent"));
            assertEquals(List.of("CS2103T | Week 3 | Absent", "CS2103T | Week 3 | Present"), attendanceBadges());
        });
    }

    @Test
    public void mark_invalidInput_retainsAnotherStudentsSelectionAndView() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            enter("mark 1 m/CS2103T w/3 s/present");
            Object selected = list().getSelectionModel().getSelectedItem();
            String details = label("studentDetails").getText();
            List<Person> before = displayedStudents();
            List<String> badges = attendanceBadges();
            for (String command : List.of("mark 2 m/CS2103T w/54 s/present",
                    "mark 3 m/CS2103T w/3 s/present", "mark 2 m/MA2001 w/3 s/present")) {
                enter(command);
                assertFalse(feedback().startsWith("Attendance set:"), command);
                assertFalse(feedback().isEmpty(), command);
                assertEquals(selected, list().getSelectionModel().getSelectedItem(), command);
                assertEquals(details, label("studentDetails").getText(), command);
                assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText(), command);
                assertEquals(before, displayedStudents(), command);
                assertEquals(badges, attendanceBadges(), command);
                assertEquals(command, input().getText(), command);
            }
        });
    }

    private List<Person> displayedStudents() {
        return list().getItems().stream().map(Person.class::cast).toList();
    }

    private List<String> attendanceBadges() {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        return stage.getScene().getRoot().lookupAll(".label").stream()
                .filter(node -> node instanceof Label)
                .map(node -> ((Label) node).getText())
                .filter(text -> text.startsWith("CS2103T | Week 3 | "))
                .sorted().toList();
    }

    private String feedback() {
        return ((TextArea) stage.getScene().lookup("#resultDisplay")).getText();
    }

    private boolean hasBadge(String text) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        return stage.getScene().getRoot().lookupAll(".label").stream()
                .anyMatch(node -> node instanceof Label label && label.getText().equals(text));
    }

    private String placeholderText() {
        return ((Label) list().getPlaceholder()).getText();
    }

    private Label label(String id) {
        return (Label) stage.getScene().lookup("#" + id);
    }

    private ListView<?> list() {
        return (ListView<?>) stage.getScene().lookup("#personListView");
    }

    private TextField input() {
        return (TextField) stage.getScene().lookup("#commandTextField");
    }

    private void enter(String command) {
        input().setText(command);
        input().fireEvent(new ActionEvent());
    }

    private static class FailingStorage extends JsonAddressBookStorage {
        private boolean fail;

        FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
            if (fail) {
                throw new IOException("Injected save failure");
            }
            super.saveAddressBook(data);
        }
    }
}

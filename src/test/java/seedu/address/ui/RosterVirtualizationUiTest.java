package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.initializeToolkit;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ClassMembership;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.TelegramHandle;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks actual recycled list cells across large-roster selection, deletion and filtering. */
public class RosterVirtualizationUiTest {
    @TempDir
    public Path directory;
    private Stage stage;

    @BeforeAll
    public static void initialize() throws Exception {
        initializeToolkit();
    }

    @AfterEach
    public void tearDown() throws Exception {
        runOnFxThread(() -> stage.hide());
    }

    @Test
    public void recycledCells_afterDeletionAndFiltering_keepCorrectIndicesContactsAndAttendance() throws Exception {
        ModelManager model = new ModelManager();
        for (int i = 0; i < 250; i++) {
            String suffix = "" + (char) ('a' + i / 26) + (char) ('a' + i % 26);
            model.addPerson(new Person(Name.forStudent("Virtual " + suffix),
                    new TelegramHandle("@virtual_" + suffix), Email.forStudent("virtual." + suffix + "@example.com"),
                    Set.of(new ClassMembership("CS2103T:" + (i % 2 == 0 ? "T04" : "T05")))));
        }
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        CompletableFuture<Void> finished = new CompletableFuture<>();
        runOnFxThread(() -> {
            LogicManager logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
            stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            enter("select 250");
            Runnable afterInitialSelection = () -> {
                assertVisibleCells();
                assertEquals("Virtual jp", list().getSelectionModel().getSelectedItem().getName().fullName);
                enter("delete 1");
                enter("select 249");
            };
            Runnable afterDeletion = () -> {
                assertVisibleCells();
                assertEquals("Virtual jp", list().getSelectionModel().getSelectedItem().getName().fullName);
                enter("find c/CS2103T:T04 w/53");
                enter("mark 124 m/CS2103T w/53 s/present");
            };
            Runnable afterFiltering = () -> {
                assertVisibleCells();
                assertEquals(124, list().getItems().size());
                assertEquals("Virtual jo", list().getSelectionModel().getSelectedItem().getName().fullName);
                assertTrue(((Label) stage.getScene().lookup("#studentDetails")).getText()
                        .contains("CS2103T | Week 53 | Present"));
                enter("list");
                enter("select 1");
            };
            Runnable afterListing = () -> {
                assertVisibleCells();
                assertEquals(249, list().getItems().size());
                assertEquals("Virtual ab", list().getSelectionModel().getSelectedItem().getName().fullName);
            };
            advance(List.of(afterInitialSelection, afterDeletion, afterFiltering, afterListing), 0, finished);
        });
        finished.get(10, TimeUnit.SECONDS);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private void advance(List<Runnable> steps, int index, CompletableFuture<Void> finished) {
        PauseTransition pulse = new PauseTransition(Duration.millis(200));
        pulse.setOnFinished(event -> {
            try {
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();
                steps.get(index).run();
                if (index + 1 == steps.size()) {
                    finished.complete(null);
                } else {
                    advance(steps, index + 1, finished);
                }
            } catch (Throwable failure) {
                finished.completeExceptionally(failure);
            }
        });
        pulse.play();
    }

    private void assertVisibleCells() {
        int checked = 0;
        for (var node : list().lookupAll(".list-cell")) {
            if (node instanceof ListCell<?> cell && !cell.isEmpty() && cell.getGraphic() != null) {
                Person person = (Person) cell.getItem();
                assertEquals((cell.getIndex() + 1) + ". ", ((Label) cell.getGraphic().lookup("#id")).getText());
                assertEquals(person.getName().fullName, ((Label) cell.getGraphic().lookup("#name")).getText());
                assertEquals(person.getEmail().value, ((Label) cell.getGraphic().lookup("#email")).getText());
                checked++;
            }
        }
        assertTrue(checked > 0, "Expected visible recycled cells");
    }

    private void enter(String command) {
        TextField input = (TextField) stage.getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        assertEquals("", input.getText(), command);
    }

    @SuppressWarnings("unchecked")
    private ListView<Person> list() {
        return (ListView<Person>) stage.getScene().lookup("#personListView");
    }
}

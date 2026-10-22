package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.initializeToolkit;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Real-window regression tests for the class view ({@code find c/COURSE:GROUP w/WEEK}): exact class matching,
 * badges and details after attendance changes outside the viewed context, and view retention on failed queries.
 */
public class ClassViewUiTest {
    @TempDir
    public Path directory;
    private MainWindow window;
    private Stage stage;

    @BeforeAll
    public static void initialize() throws Exception {
        initializeToolkit();
    }

    @BeforeEach
    public void setUp() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("students.json"));
        LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        runOnFxThread(() -> {
            stage = new Stage();
            window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
            window.fillInnerParts();
            window.show();
            enter("add n/Alice p/@alice_1 e/alice@example.com c/CS2103T:T04 c/MA2001:T01");
            enter("add n/Bobby p/@bobby_1 e/bobby@example.com c/CS2103T:T05");
            enter("add n/Carol p/@carol_1 e/carol@example.com c/CS2101:T04");
            enter("add n/David p/@david_1 e/david@example.com c/CS2103T:T04");
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        runOnFxThread(() -> stage.hide());
    }

    @Test
    public void find_matchesCourseAndGroupExactly() throws Exception {
        runOnFxThread(() -> {
            enter("find c/cs2103t:t04 w/3");
            assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText());
            assertEquals("Alice", displayedName(0)); // roster order is retained
            assertEquals("David", displayedName(1));
            // Same course in another group (Bobby) and same group in another course (Carol) are excluded
            assertTrue(hasBadge("CS2103T | Week 3 | Unrecorded"));
        });
    }

    @Test
    public void mark_otherWeekOrCourse_keepsViewedBadgeAndUpdatesDetails() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            enter("mark 1 m/CS2103T w/4 s/present");
            assertEquals(0, list().getSelectionModel().getSelectedIndex());
            assertTrue(label("studentDetails").getText().contains("Week 4"));
            assertFalse(hasBadge("CS2103T | Week 3 | Present")); // week 3 is still undecided
            assertEquals(2, countBadges("CS2103T | Week 3 | Unrecorded"));

            enter("mark 1 m/MA2001 w/3 s/absent");
            assertTrue(label("studentDetails").getText().contains("MA2001"));
            assertEquals(2, countBadges("CS2103T | Week 3 | Unrecorded")); // another course's decision is not shown
            assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText());
        });
    }

    @Test
    public void find_invalidQuery_retainsViewSelectionAndInput() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            enter("mark 2 m/CS2103T w/3 s/present");
            Object selected = list().getSelectionModel().getSelectedItem();
            String details = label("studentDetails").getText();

            for (String invalid : new String[] {"find c/T04 w/3", "find c/CS2103T:T04 w/0", "find c/CS2103T:T04",
                "find John", "find c/CS2103T:T04 w/3 w/4"}) {
                enter(invalid);
                assertEquals(invalid, input().getText(), invalid);
                assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText(), invalid);
                assertEquals(selected, list().getSelectionModel().getSelectedItem(), invalid);
                assertEquals(details, label("studentDetails").getText(), invalid);
                assertTrue(hasBadge("CS2103T | Week 3 | Present"), invalid);
            }
        });
    }

    @Test
    public void find_repeatedQuery_sameResultAndClearsSelection() throws Exception {
        runOnFxThread(() -> {
            enter("find c/CS2103T:T04 w/3");
            enter("mark 1 m/CS2103T w/3 s/absent");
            enter("find c/CS2103T:T04 w/3");
            assertEquals("CS2103T:T04 | Week 3 | 2 students", label("rosterHeading").getText());
            assertNull(list().getSelectionModel().getSelectedItem());
            assertFalse(stage.getScene().lookup("#studentDetailsPane").isVisible());
            assertTrue(hasBadge("CS2103T | Week 3 | Absent"));
        });
    }

    @Test
    public void summary_shownOnlyInNonEmptyClassViewAndUpdatesAfterMark() throws Exception {
        runOnFxThread(() -> {
            assertFalse(label("rosterSummary").isVisible()); // full roster has no class/week context

            enter("find c/CS2103T:T04 w/3");
            assertTrue(label("rosterSummary").isVisible());
            assertEquals("0 present, 0 absent, 2 unrecorded", label("rosterSummary").getText());

            enter("mark 1 m/CS2103T w/3 s/present");
            enter("mark 2 m/CS2103T w/3 s/absent");
            assertEquals("1 present, 1 absent, 0 unrecorded", label("rosterSummary").getText());

            enter("mark 1 m/CS2103T w/4 s/absent"); // another week does not change this week's totals
            assertEquals("1 present, 1 absent, 0 unrecorded", label("rosterSummary").getText());

            enter("find c/CS2103T:T04 w/4");
            assertEquals("0 present, 1 absent, 1 unrecorded", label("rosterSummary").getText());

            enter("find c/CS2103T:T04 w/0"); // a failed query keeps the previous summary
            assertEquals("0 present, 1 absent, 1 unrecorded", label("rosterSummary").getText());

            enter("find c/MA2001:T09 w/4");
            assertFalse(label("rosterSummary").isVisible()); // empty class shows only the empty-state text

            enter("list");
            assertFalse(label("rosterSummary").isVisible());
        });
    }

    private String displayedName(int index) {
        return ((Person) list().getItems().get(index)).getName().fullName;
    }

    private boolean hasBadge(String text) {
        return countBadges(text) > 0;
    }

    private long countBadges(String text) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        return stage.getScene().getRoot().lookupAll(".label").stream()
                .filter(node -> node instanceof Label label && label.getText().equals(text)).count();
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
}

package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.initializeToolkit;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises the logical viewport corresponding to the course's smallest scaled screen. */
public class SmallWindowUiTest {
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
    public void classViewWithSelectedStudent_smallViewport_keepsCommandsRosterAndDetailsUsable() throws Exception {
        CompletableFuture<double[]> measured = new CompletableFuture<>();
        runOnFxThread(() -> {
            ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
            LogicManager logic = new LogicManager(model, new StorageManager(
                    new JsonAddressBookStorage(directory.resolve("students.json")),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
            stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, directory.resolve("students.json"));
            window.fillInnerParts();
            window.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.setText("find c/CS2103T:T04 w/53");
            input.fireEvent(new ActionEvent());
            input.setText("select 1");
            input.fireEvent(new ActionEvent());
            stage.setWidth(853);
            stage.setHeight(480);
            PauseTransition settled = new PauseTransition(Duration.millis(200));
            settled.setOnFinished(event -> {
                try {
                    stage.getScene().getRoot().applyCss();
                    stage.getScene().getRoot().layout();
                    ListView<?> roster = (ListView<?>) stage.getScene().lookup("#personListView");
                    Node details = stage.getScene().lookup("#studentDetailsPane");
                    measured.complete(new double[]{stage.getHeight(), roster.getHeight(),
                        input.localToScene(input.getBoundsInLocal()).getMinY(),
                        details.localToScene(details.getBoundsInLocal()).getMaxY(), stage.getScene().getHeight()});
                } catch (Throwable failure) {
                    measured.completeExceptionally(failure);
                }
            });
            settled.play();
        });
        double[] layout = measured.get(10, TimeUnit.SECONDS);
        assertTrue(layout[0] <= 480.5, "Window minimum height exceeds the 480px logical viewport: " + layout[0]);
        assertTrue(layout[1] >= 60, "Roster must retain a usable scrolling area: " + layout[1]);
        assertTrue(layout[2] >= 0, "Command input is above the visible scene");
        assertTrue(layout[3] <= layout[4] + 1, "Selected details extend below the visible scene");
    }
}

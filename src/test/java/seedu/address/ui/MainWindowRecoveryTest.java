package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Real-window recovery checks for disconnected monitors and minimized Help. */
public class MainWindowRecoveryTest {
    @TempDir
    public Path directory;

    private Stage stage;
    private Stage help;
    private MainWindow window;

    @BeforeAll
    public static void initialize() throws Exception {
        initializeToolkit();
    }

    @AfterEach
    public void hideWindows() throws Exception {
        runOnFxThread(() -> {
            if (help != null) {
                help.hide();
            }
            if (stage != null) {
                stage.hide();
            }
        });
    }

    @Test
    public void startup_savedPositionOutsideAllScreens_recoversVisibleTitleBar() throws Exception {
        runOnFxThread(() -> {
            open(new GuiSettings(740, 600, 100000, 100000));
            assertTrue(Screen.getScreens().stream().anyMatch(screen -> screen.getVisualBounds()
                    .intersects(stage.getX(), stage.getY(), stage.getWidth(), 30)));
        });
    }

    @Test
    public void startup_savedTitleBarAboveScreens_recoversEvenWhenWindowBodyIntersects() throws Exception {
        runOnFxThread(() -> {
            int x = (int) Screen.getPrimary().getVisualBounds().getMinX() + 50;
            int y = (int) Screen.getScreens().stream().mapToDouble(screen ->
                    screen.getVisualBounds().getMinY()).min().orElseThrow() - 100;
            open(new GuiSettings(740, 600, x, y));
            assertTrue(Screen.getScreens().stream().anyMatch(screen -> screen.getVisualBounds()
                    .intersects(stage.getX(), stage.getY(), stage.getWidth(), 30)));
        });
    }

    @Test
    public void startup_visibleSavedPosition_isRetained() throws Exception {
        runOnFxThread(() -> {
            int x = (int) Screen.getPrimary().getVisualBounds().getMinX() + 50;
            int y = (int) Screen.getPrimary().getVisualBounds().getMinY() + 50;
            open(new GuiSettings(740, 600, x, y));
            assertEquals(x, stage.getX());
            assertEquals(y, stage.getY());
        });
    }

    @Test
    public void helpCommand_restoresMinimizedHelp() throws Exception {
        openMinimizedHelp();
        runOnFxThread(() -> {
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.setText("help");
            input.fireEvent(new ActionEvent());
        });
        assertRestoredHelp();
    }

    @Test
    public void helpMenu_restoresMinimizedHelp() throws Exception {
        openMinimizedHelp();
        runOnFxThread(() -> {
            MenuBar menuBar = (MenuBar) stage.getScene().lookup("#menuBar");
            menuBar.getMenus().get(1).getItems().getFirst().fire();
        });
        assertRestoredHelp();
    }

    @Test
    public void help_reopensHiddenMinimizedWindow() throws Exception {
        openMinimizedHelp();
        runOnFxThread(() -> {
            help.hide();
            window.handleHelp();
        });
        assertRestoredHelp();
    }

    private void openMinimizedHelp() throws Exception {
        runOnFxThread(() -> {
            open(new GuiSettings());
            window.handleHelp();
            help = (Stage) Window.getWindows().stream().filter(candidate -> candidate != stage
                    && candidate instanceof Stage child && "Help".equals(child.getTitle())).findFirst().orElseThrow();
        });
        settleWindowEvents();
        runOnFxThread(() -> help.setIconified(true));
        settleWindowEvents();
        runOnFxThread(() -> assertTrue(help.isIconified()));
    }

    private void assertRestoredHelp() throws Exception {
        settleWindowEvents();
        runOnFxThread(() -> {
            assertFalse(help.isIconified());
            assertTrue(help.isShowing());
        });
    }

    private void settleWindowEvents() throws Exception {
        CompletableFuture<Void> settled = new CompletableFuture<>();
        runOnFxThread(() -> {
            PauseTransition pause = new PauseTransition(Duration.millis(250));
            pause.setOnFinished(event -> settled.complete(null));
            pause.play();
        });
        settled.get(10, TimeUnit.SECONDS);
    }

    private void open(GuiSettings settings) {
        UserPrefs prefs = new UserPrefs();
        prefs.setGuiSettings(settings);
        ModelManager model = new ModelManager(new AddressBook(), prefs);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(directory.resolve("students.json")),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        stage = new Stage();
        window = new MainWindow(stage, new LogicManager(model, storage), storage.getAddressBookFilePath());
        window.fillInnerParts();
        window.show();
    }
}

package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TelegramHandleTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TelegramHandle(null));
    }

    @Test
    public void isValidTelegramHandle_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> TelegramHandle.isValidTelegramHandle(null));
    }

    @Test
    public void constructor_supportedHandles_storesNormalizedValue() {
        String[][] examples = {
            {"@abcde", "@abcde"}, // minimum length excludes @
            {"@" + "A".repeat(32), "@" + "a".repeat(32)}, // maximum length
            {"@Alice_1", "@alice_1"},
            {"   @Alice_1   ", "@alice_1"},
            {"@a123_", "@a123_"},
            {"@Z____", "@z____"}
        };
        for (String[] example : examples) {
            assertTrue(TelegramHandle.isValidTelegramHandle(example[0]), example[0]);
            TelegramHandle handle = new TelegramHandle(example[0]);
            assertEquals(example[1], handle.value);
            assertEquals(example[1], handle.toString());
        }
    }

    @Test
    public void constructor_invalidHandles_rejectsWithSpecifiedMessage() {
        String[] invalidHandles = {
            "", "   ", "@", "@abcd", "@" + "a".repeat(33),
            "alice_1", "@@alice_1", "@1alice", "@_alice",
            "@alice bob", "@alice-bob", "@alice.bob", "@alice/bob",
            "@alice@bob", "@ál ice", "@álice", "@alice中", "@alice😀",
            "@Ａlice", "@alice١", "@İlice", "@alıce",
            "\t@alice", "@alice\t", "\n@alice", "@alice\n", "@alice\r",
            "@al\tice", "@alice\u0000", "\u00a0@alice", "@alice\u2003",
            " @alice\n "
        };
        String expectedMessage = "Telegram handle must start with @, followed by 5-32 letters, digits or underscores, "
                + "starting with a letter.";
        for (String invalidHandle : invalidHandles) {
            assertFalse(TelegramHandle.isValidTelegramHandle(invalidHandle), invalidHandle);
            assertThrows(IllegalArgumentException.class, expectedMessage, () -> new TelegramHandle(invalidHandle));
        }
    }

    @Test
    public void constructor_turkishDefaultLocale_usesAsciiLowercase() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("@ilker", new TelegramHandle("@ILKER").value);
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void equals_normalizedHandles_comparesByValue() {
        TelegramHandle handle = new TelegramHandle("@Alice_1");
        TelegramHandle equivalent = new TelegramHandle("  @ALICE_1  ");

        assertTrue(handle.equals(handle));
        assertTrue(handle.equals(equivalent));
        assertTrue(equivalent.equals(handle));
        assertFalse(handle.equals(null));
        assertFalse(handle.equals("@alice_1"));
        assertFalse(handle.equals(new TelegramHandle("@alice_2")));
        assertEquals(handle.hashCode(), equivalent.hashCode());

        Set<TelegramHandle> handles = new HashSet<>();
        handles.add(handle);
        handles.add(equivalent);
        assertEquals(1, handles.size());
    }
}

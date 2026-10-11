package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_emptyAndUnicodeText_areAccepted() {
        assertEquals("", new Remark("").value);
        assertEquals("备注", new Remark("备注").toString());
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void equals_comparesText() {
        Remark remark = new Remark("Note");
        assertTrue(remark.equals(remark));
        assertEquals(remark, new Remark("Note"));
        assertEquals(remark.hashCode(), new Remark("Note").hashCode());
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Note"));
        assertFalse(remark.equals(new Remark("Different")));
    }
}

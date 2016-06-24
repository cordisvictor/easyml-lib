package net.sourceforge.easyml.marshalling.dtd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JLabel;
import net.sourceforge.easyml.EasyML;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author victor
 */
public class Base64StrategyTest {

    private final EasyML easyml = new EasyML();

    /**
     * Test of marshal method, of class Base64Strategy.
     */
    @Test
    public void testMarshalUnmarshal() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(new JLabel("TXT"));

        final String xml = easyml.serialize(out.toByteArray());

        final byte[] base64 = (byte[]) easyml.deserialize(xml);
        assertEquals("TXT", ((JLabel) new ObjectInputStream(new ByteArrayInputStream(base64)).readObject()).getText());
    }
}

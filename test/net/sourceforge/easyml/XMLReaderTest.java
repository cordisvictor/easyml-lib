package net.sourceforge.easyml;

import java.io.ByteArrayInputStream;
import net.sourceforge.easyml.marshalling.dtd.IntStrategy;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class XMLReaderTest {

    @Test
    public void testPutValidation() {
        final XMLReader xr = new XMLReader(new ByteArrayInputStream(new byte[0]));
        try {
            xr.getSimpleStrategies().put(null, IntStrategy.INSTANCE);
            fail("reader.strat.put: did not throw illegal strat name");
        } catch (IllegalArgumentException iax) {
        }
        try {
            xr.getSimpleStrategies().put("", IntStrategy.INSTANCE);
            fail("reader.strat.put: did not throw illegal strat name");
        } catch (IllegalArgumentException iax) {
        }
        try {
            xr.getSimpleStrategies().put("int<3", IntStrategy.INSTANCE);
            fail("reader.strat.put: did not throw illegal strat name");
        } catch (IllegalArgumentException iax) {
        }
    }
}

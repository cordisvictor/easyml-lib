package net.sourceforge.easyml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.sourceforge.easyml.marshalling.dtd.IntStrategy;
import static org.hamcrest.core.Is.is;
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

    @Test
    public void testHasMore() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLWriter xw = new XMLWriter(out);
        xw.writeInt(1);
        xw.write("ha ha");
        xw.writeDouble(3.0);
        xw.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        final XMLReader xr = new XMLReader(in);
        assertThat(xr.hasMore(), is(true));
        assertThat(xr.readInt(), is(1));
        assertThat(xr.hasMore(), is(true));
        assertThat(xr.readString(), is("ha ha"));
        assertThat(xr.hasMore(), is(true));
        assertThat(xr.readDouble(), is(3.0));
        assertThat(xr.hasMore(), is(false));
        xr.close();
    }
}

/*
 * Copyright 2012 Victor Cordis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Please contact the author ( cordis.victor@gmail.com ) if you need additional
 * information or have any questions.
 */
package net.sourceforge.easyml;

import net.sourceforge.easyml.marshalling.dtd.IntStrategy;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
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

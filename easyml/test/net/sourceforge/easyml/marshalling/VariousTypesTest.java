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
package net.sourceforge.easyml.marshalling;

import net.sourceforge.easyml.XMLReader;
import net.sourceforge.easyml.XMLWriter;
import net.sourceforge.easyml.marshalling.java.lang.RecordStrategy;
import net.sourceforge.easyml.marshalling.java.time.InstantStrategy;
import net.sourceforge.easyml.marshalling.java.time.LocalDateTimeStrategy;
import net.sourceforge.easyml.marshalling.java.time.ZoneIdStrategy;
import net.sourceforge.easyml.marshalling.java.util.*;
import net.sourceforge.easyml.marshalling.java.util.concurrent.atomic.AtomicReferenceStrategy;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

/**
 * @author Victor Cordis ( cordis.victor at gmail.com)
 */
public class VariousTypesTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(256);

    @After
    public void tearDown() {
        this.out.reset();
    }

    @Test
    public void testAtomicRefStrategy() {
        final String expected = "expected";

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(AtomicReferenceStrategy.INSTANCE);
        xos.write(new AtomicReference(expected));
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(AtomicReferenceStrategy.INSTANCE.name(), AtomicReferenceStrategy.INSTANCE);
        assertEquals(expected, ((AtomicReference) xis.read()).get());
        xis.close();
    }

    @Test
    public void testOptionalStrategy() {
        final Optional<String> expectedEmpty = Optional.empty();
        final Optional<String> expectedNonEmpty1 = Optional.of("something");
        final Optional<Integer> expectedNonEmpty2 = Optional.of(2);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(OptionalStrategy.INSTANCE);
        xos.write(expectedEmpty);
        xos.write(expectedNonEmpty1);
        xos.write(expectedNonEmpty2);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(OptionalStrategy.INSTANCE.name(), OptionalStrategy.INSTANCE);
        assertEquals(expectedEmpty, xis.read());
        assertEquals(expectedNonEmpty1, xis.read());
        assertEquals(expectedNonEmpty2, xis.read());
        xis.close();
    }

    @Test
    public void testOptionalIntStrategy() {
        final OptionalInt expectedEmpty = OptionalInt.empty();
        final OptionalInt expectedNonEmpty = OptionalInt.of(2);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(OptionalIntStrategy.INSTANCE);
        xos.write(expectedEmpty);
        xos.write(expectedNonEmpty);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(OptionalIntStrategy.INSTANCE.name(), OptionalIntStrategy.INSTANCE);
        assertEquals(expectedEmpty, xis.read());
        assertEquals(expectedNonEmpty, xis.read());
        xis.close();
    }

    @Test
    public void testOptionalLongStrategy() {
        final OptionalLong expectedEmpty = OptionalLong.empty();
        final OptionalLong expectedNonEmpty = OptionalLong.of(2);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(OptionalLongStrategy.INSTANCE);
        xos.write(expectedEmpty);
        xos.write(expectedNonEmpty);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(OptionalLongStrategy.INSTANCE.name(), OptionalLongStrategy.INSTANCE);
        assertEquals(expectedEmpty, xis.read());
        assertEquals(expectedNonEmpty, xis.read());
        xis.close();
    }

    @Test
    public void testOptionalDoubleStrategy() {
        final OptionalDouble expectedEmpty = OptionalDouble.empty();
        final OptionalDouble expectedNonEmpty = OptionalDouble.of(2.5);

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(OptionalDoubleStrategy.INSTANCE);
        xos.write(expectedEmpty);
        xos.write(expectedNonEmpty);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(OptionalDoubleStrategy.INSTANCE.name(), OptionalDoubleStrategy.INSTANCE);
        assertEquals(expectedEmpty, xis.read());
        assertEquals(expectedNonEmpty, xis.read());
        xis.close();
    }

    @Test
    public void testHexFormatStrategy() {
        final HexFormat hf = HexFormat.of();
        final HexFormat hfDPS = HexFormat.ofDelimiter(",").withPrefix("p").withSuffix("s");
        final HexFormat hfDPSU = hfDPS.withUpperCase();

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(HexFormatStrategy.INSTANCE);
        xos.write(hf);
        xos.write(hfDPS);
        xos.write(hfDPSU);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(HexFormatStrategy.INSTANCE.name(), HexFormatStrategy.INSTANCE);
        assertEquals(hf, xis.read());
        assertEquals(hfDPS, xis.read());
        assertEquals(hfDPSU, xis.read());
        xis.close();
    }

    @Test
    public void testCalendarStrategy() {
        final Calendar expected = Calendar.getInstance();

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(CalendarStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(CalendarStrategy.INSTANCE.name(), CalendarStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testInstantStrategy() {
        final Instant expected = Instant.now();

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getSimpleStrategies().add(InstantStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getSimpleStrategies().put(InstantStrategy.INSTANCE.name(), InstantStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testLocalDateTimeStrategy() {
        final LocalDateTime expected = LocalDateTime.now();

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getSimpleStrategies().add(LocalDateTimeStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getSimpleStrategies().put(LocalDateTimeStrategy.INSTANCE.name(), LocalDateTimeStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testZoneIdStrategy() {
        final ZoneId expected = ZoneId.systemDefault();

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getSimpleStrategies().add(ZoneIdStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getSimpleStrategies().put(ZoneIdStrategy.INSTANCE.name(), ZoneIdStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testTimeZoneStrategy() {
        final TimeZone expected = TimeZone.getDefault();

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getSimpleStrategies().add(TimeZoneStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getSimpleStrategies().put(TimeZoneStrategy.INSTANCE.name(), TimeZoneStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    @Test
    public void testRecordStrategy() {
        final MyRecord expected = new MyRecord(Optional.of("fn ln"), new Date());

        final XMLWriter xos = new XMLWriter(this.out);
        xos.getCompositeStrategies().add(OptionalStrategy.INSTANCE);
        xos.getCompositeStrategies().add(RecordStrategy.INSTANCE);
        xos.write(expected);
        xos.close();

        System.out.println(this.out);

        final XMLReader xis = new XMLReader(new ByteArrayInputStream(this.out.toByteArray()));
        xis.getCompositeStrategies().put(OptionalStrategy.INSTANCE.name(), OptionalStrategy.INSTANCE);
        xis.getCompositeStrategies().put(RecordStrategy.INSTANCE.name(), RecordStrategy.INSTANCE);
        assertEquals(expected, xis.read());
        xis.close();
    }

    public record MyRecord(
            Optional<String> name,
            Date theDate) {
    }
}

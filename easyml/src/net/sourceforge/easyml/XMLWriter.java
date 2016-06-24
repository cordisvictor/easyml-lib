/*
 * Copyright (c) 2011, Victor Cordis. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of EasyML library.
 *
 * EasyML library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License (LGPL) as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * EasyML library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with EasyML library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Please contact the author ( cordis.victor@gmail.com ) if you need additional
 * information or have any questions.
 */
package net.sourceforge.easyml;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.CompositeWriter;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.Strategy;
import net.sourceforge.easyml.marshalling.dtd.*;
import net.sourceforge.easyml.marshalling.java.lang.ByteStrategy;
import net.sourceforge.easyml.marshalling.java.lang.CharacterStrategy;
import net.sourceforge.easyml.marshalling.java.lang.FloatStrategy;
import net.sourceforge.easyml.marshalling.java.lang.LongStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ShortStrategy;
import net.sourceforge.easyml.util.*;
import org.w3c.dom.Document;

/**
 * XMLWriter class is responsible for writing object graphs output streams, the
 * target reader being the {@linkplain XMLReader}. The objects inside input
 * graphs must be of DTD-types, otherwise both the writer instance and the
 * target reader must be configured with proper strategies.
 * <p>
 * Configuring a writer instance means adding strategies, which apply to the
 * intended input, to the writer's {@linkplain StrategyRegistry}. Strategy
 * priority and class aliasing are also available.
 * <p>
 * Usage example:
 * <pre>
 * final XMLWriter w = new XMLWriter(new FileOutputStream(FILE_NAME));
 * w.writeInt(3);
 * w.writeInt(2);
 * w.writeInt(1);
 * w.write("easy!");
 * w.close();
 * </pre>
 *
 * <b>Note:</b> this implementation is NOT thread-safe, but instances with
 * shared configuration can be created, via constructors.
 *
 * @see XMLReader
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.7
 */
public class XMLWriter implements Flushable, Closeable {

    /**
     * StrategyRegistry class is a {@linkplain Strategy} container used to add,
     * prioritize, and look-up strategies for given {@linkplain Class}es.
     *
     * @param <S> strategy class
     */
    public static final class StrategyRegistry<S extends Strategy> {

        private final Map<Class, S> strict;
        private final List<S> range;
        private List<S> backup;

        private StrategyRegistry() {
            this.strict = new IdentityHashMap<Class, S>();
            this.range = new LinkedList<S>();
            this.backup = null;
        }

        /**
         * Gets the {@linkplain #backup} property. This is the list of
         * strategies to use as last-resort, i.e. when no registered strategy is
         * applicable.
         * <b>Note:</b> The order of the backup list is important as applicable
         * strategies will be searched starting from first to last.
         *
         * @return the value
         */
        public List<S> getBackup() {
            if (this.backup == null) {
                this.backup = new ArrayList<S>();
            }
            return this.backup;
        }

        /**
         * Returns the size of this instance.
         *
         * @return the size
         */
        public int size() {
            return this.strict.size() + this.range.size() + (this.backup != null ? this.backup.size() : 0);
        }

        /**
         * Returns true if this instance is empty, i.e. contains no strategies.
         *
         * @return true if empty, false otherwise
         */
        public boolean isEmpty() {
            return this.strict.isEmpty() && this.range.isEmpty() && (this.backup == null || this.backup.isEmpty());
        }

        private boolean containsName(List<S> source, String strategyName) {
            for (S s : source) {
                if (s.name().equals(strategyName)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns <code>true</code> if the given strategy is contained within
         * this instance, <code>false</code> otherwise.
         *
         * @param s to search
         *
         * @return true if strategy has been found, false otherwise
         */
        public boolean contains(S s) {
            // if s is strict then search in strict:
            if (s.strict() && this.strict.containsKey(s.target())) {
                return true;
            }
            // else search in range:
            if (this.containsName(this.range, s.name())) {
                return true;
            }
            // search for it in backup, if any:
            if (this.backup != null) {
                if (this.containsName(this.backup, s.name())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Adds the given strategy. If <code>s.strict()</code> and this instance
         * already contains a strict strategy with the exact same target then
         * <code>s</code> will replace the contained strategy.
         *
         * @param s to add
         *
         * @return the replaced strict strategy, if any, or null
         */
        public S add(S s) {
            if (s.strict()) {
                return this.strict.put(s.target(), s);
            } else {
                final ListIterator<S> it = this.range.listIterator();
                while (it.hasNext()) {
                    final S crt = it.next();
                    if (crt.target().isAssignableFrom(s.target())) {
                        it.set(s);
                        it.add(crt);
                        return null;
                    }
                }
                this.range.add(s);
                return null;
            }
        }

        /**
         * Removes the given strategy and returns true if it was found and
         * removed. If <code>s</code> is strict then it will be searched by
         * target, else it will be searched by name.
         *
         * @param s to remove
         *
         * @return true if strategy has been removed, false otherwise
         */
        public boolean remove(S s) {
            if (s.strict()) {
                return this.strict.remove(s.target()) != null;
            } else {
                final ListIterator<S> it = this.range.listIterator();
                while (it.hasNext()) {
                    if (it.next().name().equals(s.name())) {
                        it.remove();
                        return true;
                    }
                }
                return false;
            }
        }

        /**
         * Clears this instance of all strategies.
         */
        public void clear() {
            this.strict.clear();
            this.range.clear();
            if (this.backup != null) {
                this.backup.clear();
            }
        }

        private void validateForPrioritize(S s) {
            if (s.strict()) {
                throw new IllegalArgumentException(s.name() + ": not prioritizable: is strict");
            }
            if (!this.range.contains(s)) {
                throw new IllegalArgumentException(s.name() + ": not contained or is backup");
            }
        }

        /**
         * Prioritizes the given <code>high</code> strategy over the given
         * <code>low</code> strategy, iff <code>high</code> has a lower priority
         * than <code>low</code> else nothing is done. Both <code>high</code>
         * and <code>low</code> must be contained, <b>not</b>
         * as backup strategies, by this instance and must be
         * non-{@linkplain Strategy#strict()}.
         *
         * @param high the non-strict strategy to ensure higher priority for
         * @param low the non-strict strategy to be shadowed by high
         *
         * @throws IllegalArgumentException if high and/or low is strict or not
         * contained as non-backup by this instance
         */
        public void prioritize(S high, S low) {
            //validate:
            this.validateForPrioritize(high);
            this.validateForPrioritize(low);
            // prioritize:
            final ListIterator<S> rangeItr = this.range.listIterator();
            while (rangeItr.hasNext()) {
                S crt = rangeItr.next();
                if (crt.equals(high)) {
                    return;
                }
                if (crt.equals(low)) {
                    final int lowIdx = rangeItr.nextIndex() - 1;
                    while (rangeItr.hasNext()) {
                        crt = rangeItr.next();
                        if (crt.equals(high)) {
                            rangeItr.remove();
                            this.range.add(lowIdx, crt);
                            return;
                        }
                    }
                }
            }
        }

        /**
         * Returns the strategy applicable for the given class or
         * <code>null</code> if no such strategy is found. A strategy that
         * applies strictly to the given class is always preferable. If no such
         * strategy is available then the range strategies are checked for
         * applicability. Backup strategies, if applicable, are returned as a
         * last resort.
         *
         * @param target to lookup strategy for
         *
         * @return the applicable strategy or null
         */
        public S lookup(Class target) {
            final S ss = this.strict.get(target);
            if (ss != null) {
                return ss; // found strict.
            }
            for (S rs : this.range) {
                if (rs.appliesTo(target)) {
                    return rs; // found range.
                }
            }
            if (this.backup != null) {
                for (S bs : this.backup) {
                    if (bs.appliesTo(target)) {
                        return bs; // found backup.
                    }
                }
            }
            return null;
        }

        /**
         * {@inheritDoc }
         *
         * @return the string representation of this instance and the contained
         * strategies
         */
        @Override
        public String toString() {
            return new StringBuilder("strict=").append(this.strict.values())
                    .append(", range=").append(this.range)
                    .append(", backup=").append(this.backup)
                    .toString();
        }
    }//class StrategyRegistry.

    /**
     * Driver class is an abstraction layer, separating the XMLWriter encoding
     * process from the actual XML outputting, which can vary form writing
     * characters to appending DOM elements.
     */
    public static abstract class Driver implements CompositeWriter, Flushable, Closeable {

        protected static final int STATE_INITIAL = 0;
        protected static final int STATE_START = 1;
        protected static final int STATE_VALUE = 2;
        protected static final int STATE_VALUE_END = 3;
        private final XMLWriter target;
        protected int state;

        /**
         * Creates a new instance to be used by the <code>target</code>
         * XMLWriter.
         *
         * @param target to use and be used by
         */
        protected Driver(XMLWriter target) {
            this.target = target;
            this.state = Driver.STATE_INITIAL;
        }

        /**
         * Returns <code>true</code> if pretty printing is enabled,
         * <code>false</code> otherwise.
         *
         * @return true if pretty print, false otherwise
         */
        public final boolean isPrettyPrint() {
            return this.target.prettyPrint;
        }

        /**
         * Returns <code>true</code> if a one-time only unique id is available,
         * <code>false</code> otherwise.
         *
         * @return true if unique id available, false otherwise
         */
        public final boolean isOneTimeUniqueId() {
            return this.target.encodedOneTimeUniqueId != null;
        }

        /**
         * Returns and clears the current one-time only unique id, if available,
         * or returns <code>null</code> if {@linkplain #isOneTimeUniqueId() } is
         * false.
         *
         * @return one-time only unique or null
         */
        public final String oneTimeUniqueId() {
            final String result = this.target.encodedOneTimeUniqueId;
            this.target.encodedOneTimeUniqueId = null;
            return result;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeBoolean(boolean b) {
            this.target.writeBoolean(b);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeChar(char c) {
            this.target.writeChar(c);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeByte(byte b) {
            this.target.writeByte(b);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeShort(short s) {
            this.target.writeShort(s);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeDouble(double d) {
            this.target.writeDouble(d);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeFloat(float f) {
            this.target.writeFloat(f);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeInt(int i) {
            this.target.writeInt(i);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeLong(long l) {
            this.target.writeLong(l);
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final void writeString(String s) {
            this.target.writeString(s);
        }

        /**
         * Writes the given object recursively.
         *
         * @param o to write
         */
        @Override
        public final void write(Object o) {
            if (this.state != Driver.STATE_START && this.state != Driver.STATE_VALUE) {
                throw new IllegalStateException("cannot write o");
            }
            try {
                this.target.write0(o);
            } catch (NoSuchMethodException nDC) {
                throw new IllegalArgumentException("value: no default constructor: " + nDC.getMessage(), nDC);
            } catch (InvocationTargetException iDC) {
                throw new IllegalArgumentException("value: invalid default constructor: " + iDC.getMessage(), iDC);
            } catch (InstantiationException iDC) {
                throw new IllegalArgumentException("value: invalid default constructor: " + iDC.getMessage(), iDC);
            } catch (IllegalAccessException iDCM) {
                throw new IllegalArgumentException("value: invalid default constructor modifier: " + iDCM.getMessage(), iDCM);
            }
        }

        /**
         * Should flush the so far encoded data.
         */
        @Override
        public void flush() {
            this.state = Driver.STATE_INITIAL;
        }

        /**
         * Should release any defined resources.
         */
        @Override
        public void close() {
            this.flush();
        }
    }//(+)class Driver.

    private Driver driver;
    private Map<Object, Integer> encoded;
    private String encodedOneTimeUniqueId;
    private boolean sharedConfiguration;
    /* default*/ boolean skipDefaults;
    /* default*/ boolean prettyPrint;
    /* default*/ SimpleDateFormat dateFormat;
    private MarshalContextImpl context;
    private Map<Object, String> aliasing;
    private Set<Field> exclusions;
    private StrategyRegistry<SimpleStrategy> simpleStrategies;
    private StrategyRegistry<CompositeStrategy> compositeStrategies;

    /**
     * Creates a new instance. To be used by {@linkplain EasyML} only.
     */
    /* default*/ XMLWriter() {
        this.driver = null;
        this.init();
    }

    /**
     * Creates a new instance. To be used by {@linkplain EasyML} only.
     *
     * @param configured writer prototype
     */
    /* default*/ XMLWriter(XMLWriter configured) {
        this.driver = null;
        this.initIdentically(configured);
    }

    /**
     * Creates a new instance with the given <code>writer</code>.
     *
     * @param writer to write output with
     */
    public XMLWriter(Writer writer) {
        this.driver = new XMLWriterTextDriver(this, writer);
        this.init();
    }

    /**
     * Creates a new shared-configuration instance with the given
     * <code>writer</code>.
     *
     * @param writer to write output with
     * @param configured xml writer prototype
     */
    public XMLWriter(Writer writer, XMLWriter configured) {
        this.driver = new XMLWriterTextDriver(this, writer);
        this.initIdentically(configured);
    }

    /**
     * Creates a new instance with the given <code>out</code> stream to write
     * to.
     *
     * @param out stream to output to
     */
    public XMLWriter(OutputStream out) {
        this.driver = new XMLWriterTextDriver(this, new OutputStreamWriter(out));
        this.init();
    }

    /**
     * Creates a new shared-configuration instance with the given
     * <code>out</code> stream to write to.
     *
     * @param out stream to output to
     * @param configured xml writer prototype
     */
    public XMLWriter(OutputStream out, XMLWriter configured) {
        this.driver = new XMLWriterTextDriver(this, new OutputStreamWriter(out));
        this.initIdentically(configured);
    }

    /**
     * Creates a new instance with the given <code>out</code> DOM to append to.
     *
     * @param out empty DOM to append to
     */
    public XMLWriter(Document out) {
        this.driver = new XMLWriterDOMDriver(this, out);
        this.init();
    }

    /**
     * Creates a new shared-configuration instance with the given
     * <code>out</code> DOM to append to.
     *
     * @param out empty DOM to append to
     * @param configured xml writer prototype
     */
    public XMLWriter(Document out, XMLWriter configured) {
        this.driver = new XMLWriterDOMDriver(this, out);
        this.initIdentically(configured);
    }

    private void init() {
        this.encoded = new IdentityHashMap<Object, Integer>();
        this.encodedOneTimeUniqueId = null;
        this.sharedConfiguration = false;
        this.context = new MarshalContextImpl();
        this.aliasing = new HashMap<Object, String>();
        this.exclusions = new HashSet<Field>();
        this.skipDefaults = true;
        this.prettyPrint = false;
        this.dateFormat = new SimpleDateFormat(DTD.FORMAT_DATE);
        this.simpleStrategies = new StrategyRegistry<SimpleStrategy>();
        this.compositeStrategies = new StrategyRegistry<CompositeStrategy>();
        // add DTD strategies by default:
        this.simpleStrategies.add(Base64Strategy.INSTANCE);
        this.simpleStrategies.add(BooleanStrategy.INSTANCE);
        this.simpleStrategies.add(DateStrategy.INSTANCE);
        this.simpleStrategies.add(DoubleStrategy.INSTANCE);
        this.simpleStrategies.add(IntStrategy.INSTANCE);
        this.simpleStrategies.add(StringStrategy.INSTANCE);
        // add NON-DTD strategies for primitives, since we need to support the primitive API:
        this.simpleStrategies.add(ByteStrategy.INSTANCE);
        this.simpleStrategies.add(CharacterStrategy.INSTANCE);
        this.simpleStrategies.add(FloatStrategy.INSTANCE);
        this.simpleStrategies.add(LongStrategy.INSTANCE);
        this.simpleStrategies.add(ShortStrategy.INSTANCE);
    }

    private void initIdentically(XMLWriter other) {
        this.encoded = new IdentityHashMap<Object, Integer>();
        this.encodedOneTimeUniqueId = null;
        this.sharedConfiguration = true;
        this.context = new MarshalContextImpl();
        this.aliasing = other.aliasing;
        this.exclusions = other.exclusions;
        this.skipDefaults = other.skipDefaults;
        this.prettyPrint = other.prettyPrint;
        this.dateFormat = new SimpleDateFormat(other.dateFormat.toPattern());
        this.simpleStrategies = other.simpleStrategies;
        this.compositeStrategies = other.compositeStrategies;
    }

    private void checkSharedConfiguration() {
        if (this.sharedConfiguration) {
            throw new IllegalStateException("modifying this writer's shared configuration not allowed");
        }
    }

    /**
     * Gets the {@linkplain #sharedConfiguration} property.<br/>
     * If <code>true</code> then this instance may not have it's configuration
     * altered.
     *
     * @return the property value
     */
    public boolean isSharedConfiguration() {
        return this.sharedConfiguration;
    }

    /**
     * Gets the {@linkplain #skipDefaults} property.
     *
     * @return the property value
     */
    public boolean isSkipDefaults() {
        return this.skipDefaults;
    }

    /**
     * Sets the {@linkplain #skipDefaults} property. This setting should be
     * honored by all composite strategies doing default-value checks that are
     * being used by this instance.
     *
     * @param skipDefaults true if skip default-value objects, false otherwise
     *
     * @throws IllegalStateException if shared configuration
     */
    public void setSkipDefaults(boolean skipDefaults) {
        this.checkSharedConfiguration();
        this.skipDefaults = skipDefaults;
    }

    /**
     * Gets the {@linkplain #prettyPrint} property.
     *
     * @return the property value
     */
    public boolean isPrettyPrint() {
        return this.prettyPrint;
    }

    /**
     * Sets the {@linkplain #prettyPrint} property.
     *
     * @param prettyPrint true if format the XML pretty, false otherwise
     *
     * @throws IllegalStateException if shared configuration
     */
    public void setPrettyPrint(boolean prettyPrint) {
        this.checkSharedConfiguration();
        this.prettyPrint = prettyPrint;
    }

    /**
     * Sets the {@linkplain #dateFormat} property.
     *
     * @param dateFormat to be used by strategies at formatting dates
     *
     * @throws IllegalArgumentException if null dateFormat
     * @throws IllegalStateException if shared configuration
     */
    public void setDateFormat(String dateFormat) {
        if (dateFormat == null) {
            throw new IllegalArgumentException("dateFormat: null");
        }
        this.checkSharedConfiguration();
        this.dateFormat = new SimpleDateFormat(dateFormat);
    }

    /**
     * Gets the {@linkplain #simpleStrategies} property.
     *
     * @return the simple strategy registry
     *
     * @throws IllegalStateException if shared configuration
     */
    public StrategyRegistry<SimpleStrategy> getSimpleStrategies() {
        this.checkSharedConfiguration();
        return this.simpleStrategies;
    }

    /**
     * Gets the {@linkplain #compositeStrategies} property.
     *
     * @return the composite strategy registry
     *
     * @throws IllegalStateException if shared configuration
     */
    public StrategyRegistry<CompositeStrategy> getCompositeStrategies() {
        this.checkSharedConfiguration();
        return this.compositeStrategies;
    }

    /**
     * Aliases the given class' name with the given <code>alias</code>. The
     * given alias must not be non-null, not-empty and must not contain illegal
     * characters w.r.t. the XML format.
     *
     * @param c to alias
     * @param alias the alias to set
     *
     * @return the previous alias, if any, or null
     *
     * @throws IllegalArgumentException if alias contains invalid XML chars
     * @throws IllegalStateException if shared configuration
     */
    public String alias(Class c, String alias) {
        XMLUtil.validateAlias(alias);
        this.checkSharedConfiguration();
        return this.aliasing.put(c, alias);
    }

    /**
     * Aliases the given field's name with the given <code>alias</code>. The
     * given alias must not be non-null, not-empty and must not contain illegal
     * characters w.r.t. the XML format.
     *
     * @param f to alias
     * @param alias the alias to set
     *
     * @return the previous alias, if any, or null
     *
     * @throws IllegalArgumentException if alias contains invalid XML chars
     * @throws IllegalStateException if shared configuration
     */
    public String alias(Field f, String alias) {
        XMLUtil.validateAlias(alias);
        this.checkSharedConfiguration();
        return this.aliasing.put(f, alias);
    }

    /**
     * Excludes the given field.
     *
     * @param f to exclude
     *
     * @throws IllegalStateException if shared configuration
     */
    public void exclude(Field f) {
        this.checkSharedConfiguration();
        this.exclusions.add(f);
    }

    /**
     * Writes the given boolean in XML format.
     *
     * @param b to write
     */
    public void writeBoolean(boolean b) {
        this.writeValue(DTD.TYPE_BOOLEAN, Boolean.toString(b));
    }

    /**
     * Writes the given char in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param c to write
     */
    public void writeChar(char c) {
        this.writeValue(CharacterStrategy.NAME, Character.toString(c));
    }

    /**
     * Writes the given byte in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param b to write
     */
    public void writeByte(byte b) {
        this.writeValue(ByteStrategy.NAME, Byte.toString(b));
    }

    /**
     * Writes the given short in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param s to write
     */
    public void writeShort(short s) {
        this.writeValue(ShortStrategy.NAME, Short.toString(s));
    }

    /**
     * Writes the given double in XML format.
     *
     * @param d to write
     */
    public void writeDouble(double d) {
        this.writeValue(DTD.TYPE_DOUBLE, Double.toString(d));
    }

    /**
     * Writes the given float in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param f to write
     */
    public void writeFloat(float f) {
        this.writeValue(FloatStrategy.NAME, Float.toString(f));
    }

    /**
     * Writes the given int in XML format.
     *
     * @param i to write
     */
    public void writeInt(int i) {
        this.writeValue(DTD.TYPE_INT, Integer.toString(i));
    }

    /**
     * Writes the given long in XML format.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @param l to write
     */
    public void writeLong(long l) {
        this.writeValue(LongStrategy.NAME, Long.toString(l));
    }

    /**
     * Writes the given String in XML format.
     *
     * @param s to write
     */
    public void writeString(String s) {
        this.writeValue(DTD.TYPE_STRING, s);
    }

    // write values for the above api:
    private void writeValue(String element, String value) {
        this.ensureRootWritten();
        this.driver.startElement(element);
        this.driver.writeValue(value);
        this.driver.endElement();
    }

    // array: the array of unknown class to write as XML
    private void writeArray(Object array)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, SecurityException {
        final int length = Array.getLength(array);
        this.driver.startElement(DTD.ELEMENT_ARRAY);
        this.driver.setAttribute(DTD.ATTRIBUTE_LENGTH, Integer.toString(length));
        final ValueType pvt = ValueType.ofPrimitive(array.getClass().getComponentType());
        if (pvt != null) {// primitives array:
            for (int i = 0; i < length; i++) {
                pvt.getWriteArrayItem(this.driver, array, i, false);
            }
        } else { // objects array:
            final Object[] objArray = (Object[]) array;
            for (int i = 0; i < length; i++) {
                this.write0(objArray[i]);
            }
        }
        this.driver.endElement();
    }

    // obj: "object with properties" to write
    private void writeObject(Object obj)
            throws NoSuchMethodException, InstantiationException,
            InvocationTargetException, IllegalAccessException, SecurityException {
        // begin bean encoding:
        this.driver.startElement(DTD.ELEMENT_OBJECT);
        Class cls = obj.getClass();
        final String mappedName = this.context.aliasFor(cls);
        this.driver.setAttribute(DTD.ATTRIBUTE_CLASS, (mappedName != null ? mappedName : cls.getName()));
        // encode properties:
        while (cls != Object.class) { // process inheritance:
            for (Field f : cls.getDeclaredFields()) { // process composition:
                if (Modifier.isStatic(f.getModifiers()) || !ReflectionUtil.hasClassFieldProperty(cls, f) || this.context.excluded(f)) {
                    continue; // skip static or non-property or excluded field.
                }
                // get property field:
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                // write property value:
                final String aliasedFieldName = this.context.aliasFor(f, f.getName());
                this.driver.startElement(aliasedFieldName);
                this.write0(f.get(obj));
                this.driver.endElement();
            }
            cls = cls.getSuperclass();
        }
        // end bean encoding:
        this.driver.endElement();
    }

    private void write0(Object data)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, SecurityException {
        // simple strategy:
        // nil:
        if (data == null) {
            this.driver.startElement(DTD.ELEMENT_NIL);
            this.driver.endElement();
            return;
        }
        // non-nil:
        final Class cls = data.getClass();
        SimpleStrategy ss = this.simpleStrategies.lookup(cls);
        if (ss != null) {
            this.driver.startElement(ss.name());
            this.driver.writeValue(ss.marshal(data, this.context));
            this.driver.endElement();
            return;
        }
        // composite strategy:
        // check if data was already visited in the object graph:
        final Integer idRef = this.encoded.get(data);
        if (idRef != null) {
            // write object idref for already-visited data:
            this.driver.startElement(DTD.ELEMENT_OBJECT);
            this.driver.setAttribute(DTD.ATTRIBUTE_IDREF, idRef.toString());
            this.driver.endElement();
        } else {
            // mark data as visited:
            final int nextUniqueId = this.encoded.size() + 1;
            this.encoded.put(data, nextUniqueId);
            this.encodedOneTimeUniqueId = String.valueOf(nextUniqueId);
            // visit data:
            final CompositeStrategy cs = this.compositeStrategies.lookup(cls);
            if (cs != null) {
                cs.marshal(data, this.driver, this.context);
            } else if (cls.isArray()) {
                this.writeArray(data);
            } else {
                this.writeObject(data);
            }
        }
    }

    /**
     * Writes the object-graph containing values (primitives and wrappers),
     * objects, and arrays, starting from the given <code>o</code> node.
     *
     * @param o the write start-point node
     *
     * @throws IllegalArgumentException if an illegal (non values/bean/array) is
     * encountered
     */
    public final void write(Object o) {
        this.ensureRootWritten();
        try {
            this.write0(o);
        } catch (NoSuchMethodException nDC) {
            throw new IllegalArgumentException("o: no default constructor: " + nDC.getMessage(), nDC);
        } catch (InvocationTargetException iDC) {
            throw new IllegalArgumentException("o: invalid default constructor: " + iDC.getMessage(), iDC);
        } catch (InstantiationException iDC) {
            throw new IllegalArgumentException("o: invalid default constructor: " + iDC.getMessage(), iDC);
        } catch (IllegalAccessException iDCM) {
            throw new IllegalArgumentException("o: invalid default constructor modifier: " + iDCM.getMessage(), iDCM);
        }
    }

    private void ensureRootWritten() {
        if (this.driver.state == XMLWriter.Driver.STATE_INITIAL) {
            this.driver.startElement(DTD.ELEMENT_EASYML);
        }
    }

    /**
     * Flushes the written objects to the output and writes the easyml root end
     * tag.
     */
    @Override
    public final void flush() {
        // this.driver==null: when this is instantiated by EasyML constructor and first write:
        if (this.driver != null && this.driver.state != XMLWriter.Driver.STATE_INITIAL) {
            this.driver.endElement(); // DTD.ELEMENT_EASYML.
            this.driver.flush();
            this.driver.state = XMLWriter.Driver.STATE_INITIAL;
            this.encoded.clear();
        }
    }

    /**
     * Resets this instance, flushing if necessary, and setting it to the new
     * <code>writer</code>.
     *
     * @param writer to use from now on
     */
    public void reset(Writer writer) {
        this.flush();
        this.driver = new XMLWriterTextDriver(this, writer);
    }

    /**
     * Resets this instance, flushing if necessary, and setting it to the new
     * <code>out</code> stream.
     *
     * @param out to use from now on
     */
    public void reset(OutputStream out) {
        this.reset(new OutputStreamWriter(out));
    }

    /**
     * Resets this instance, flushing if necessary, and setting it to the new
     * <code>out</code> DOM.
     *
     * @param out to use from now on
     */
    public void reset(Document out) {
        this.flush();
        this.driver = new XMLWriterDOMDriver(this, out);
    }

    /**
     * Closes this instance by releasing all resources.
     */
    @Override
    public final void close() {
        this.flush();
        if (this.driver != null) {
            this.driver.close();
        }
        this.encoded = null;
        this.encodedOneTimeUniqueId = null;
        this.context = null;
        this.aliasing = null;
        this.exclusions = null;
        this.compositeStrategies = null;
        this.simpleStrategies = null;
    }

    private final class MarshalContextImpl implements MarshalContext {

        @Override
        public String aliasFor(Class c) {
            return aliasing.get(c);
        }

        @Override
        public String aliasFor(Class c, String defValue) {
            final String value = aliasing.get(c);
            return value != null ? value : defValue;
        }

        @Override
        public String aliasFor(Field f) {
            return aliasing.get(f);
        }

        @Override
        public String aliasFor(Field f, String defValue) {
            final String value = aliasing.get(f);
            return value != null ? value : defValue;
        }

        @Override
        public boolean excluded(Field f) {
            return exclusions.contains(f);
        }

        @Override
        public boolean prettyPrinting() {
            return prettyPrint;
        }

        @Override
        public boolean skipDefaults() {
            return skipDefaults;
        }

        @Override
        public String formatDate(Date d) {
            return dateFormat.format(d);
        }
    }//(+)class MarshalContextImpl.
}

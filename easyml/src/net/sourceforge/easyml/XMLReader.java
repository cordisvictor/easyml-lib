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

import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.marshalling.dtd.*;
import net.sourceforge.easyml.marshalling.java.lang.*;
import net.sourceforge.easyml.util.ReflectionUtil;
import net.sourceforge.easyml.util.ReflectionUtil.ValueType;
import net.sourceforge.easyml.util.XMLUtil;
import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;

import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * XMLReader class is responsible for reading XML written by the
 * {@linkplain XMLWriter} from input streams and recreating object graphs. The
 * objects inside input EasyML XML format must be of DTD-types, otherwise the
 * reader instance must be configured with proper strategies.
 * <p>
 * Configuring a reader instance means mapping strategies that apply to the
 * intended input to the reader's strategy map. Class aliasing and black-listing
 * features are also available.
 * <p>
 * Usage example:
 * <pre>
 * final XMLReader r = new XMLReader(new FileInputStream(FILE_NAME));
 * int i3 = r.readInt();
 * int i2 = r.readInt();
 * int i1 = r.readInt();
 * String easy = r.readString();
 * r.close();
 * </pre>
 * <b>Note:</b> this implementation is NOT thread-safe, but instances with
 * shared configuration can be created, via constructors.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.7.3
 * @see XMLWriter
 * @since 1.0
 */
public class XMLReader implements Closeable, Iterable, Supplier, BooleanSupplier, IntSupplier, LongSupplier, DoubleSupplier {

    /**
     * SecurityPolicy class is a {@linkplain Class} container used, for security
     * reasons, to define whitelists or blacklists of classes that are outputted
     * by the XMLReader. By default, the security policy is in <b>blacklist</b>
     * mode.
     */
    public static final class SecurityPolicy {

        private final Set<Class> strict;
        private final List<Class> inheritance;
        private boolean whitelist;

        private SecurityPolicy() {
            this.strict = new HashSet<>();
            this.inheritance = new ArrayList<>();
            this.whitelist = false;
        }

        SecurityPolicy(boolean whitelist, Class[] classes, Class[] classHierarchies) {
            this.strict = new HashSet<>(Arrays.asList(classes));
            this.inheritance = new ArrayList<>(Arrays.asList(classHierarchies));
            this.whitelist = whitelist;
        }

        /**
         * Returns true if this instance is a whitelist, i.e. allows only
         * classes contained by this.
         *
         * @return true if whitelist, false otherwise
         */
        public boolean isWhitelist() {
            return this.whitelist;
        }

        /**
         * Returns true if this instance is a blacklist, i.e. allows only
         * classes <b>not</b> contained by this.
         *
         * @return true if blacklist, false otherwise
         */
        public boolean isBlacklist() {
            return !this.whitelist;
        }

        /**
         * Sets this instance to be a whitelist, i.e. allow only classes
         * contained by this policy.
         */
        public void setWhitelistMode() {
            this.whitelist = true;
        }

        /**
         * Sets this instance to be a blacklist, i.e. disallow classes contained
         * by this policy.
         */
        public void setBlacklistMode() {
            this.whitelist = false;
        }

        /**
         * Adds a class to this policy list.
         *
         * @param c to add
         * @return true
         * @throws IllegalArgumentException when c is null or represents an abstract class or interface
         */
        public boolean add(Class c) {
            if (c == null) {
                throw new IllegalArgumentException("c: null");
            }
            final int modifiers = c.getModifiers();
            if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
                throw new IllegalArgumentException("c: is abstract or an interface");
            }
            // we add c even if it's contained hierarchically, for fast contains check:
            return this.strict.add(c);
        }

        /**
         * Adds a class hierarchy to this policy list, having <code>root</code>
         * as hierarchy root.
         *
         * @param root of hierarchy to add
         * @return true if added, false if root is part of an already added hierarchy
         * @throws IllegalArgumentException when c is null or represents a final class
         */
        public boolean addHierarchy(Class root) {
            if (root == null) {
                throw new IllegalArgumentException("root: null");
            }
            if (Modifier.isFinal(root.getModifiers()) && !root.isArray()) {
                throw new IllegalArgumentException("root: is final and cannot be a hierarchy root: " + root.getName());
            }
            // add root:
            if (!this.containsHierarchically(root)) {
                // squash possible sub-hierarchies of root
                // by removing them..
                final Iterator<Class> it = this.inheritance.iterator();
                while (it.hasNext()) {
                    if (root.isAssignableFrom(it.next())) {
                        it.remove();
                    }
                }
                // ..and adding root hierarchy:
                return this.inheritance.add(root);
            }
            return false;
        }

        private boolean containsHierarchically(Class c) {
            for (Class root : this.inheritance) {
                if (root.isAssignableFrom(c)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns <code>true</code> if the given class is contained within this
         * policy, <code>false</code> otherwise.
         *
         * @param c to search
         * @return true if class has been found, false otherwise
         */
        public boolean contains(Class c) {
            return this.strict.contains(c) || this.containsHierarchically(c);
        }

        /**
         * Checks if the given class is allowed w.r.t this policy.
         *
         * @param c to check
         * @throws IllegalClassException when the given class is contained in this blacklist or not contained in this whitelist
         */
        public void check(Class c) {
            final boolean found = this.contains(c);
            if (found != this.whitelist) {
                throw new IllegalClassException(c);
            }
        }

        /**
         * Clears this instance of all classes.
         */
        public void clear() {
            this.strict.clear();
            this.inheritance.clear();
            this.whitelist = false; // otherwise this empty policy will reject everything after clear.
        }

        /**
         * {@inheritDoc }
         *
         * @return the string representation of this instance and the contained classes
         */
        @Override
        public String toString() {
            return new StringBuilder(this.whitelist ? "whitelist:" : "blacklist:")
                    .append("strict=").append(this.strict)
                    .append(", inheritance=").append(this.inheritance)
                    .toString();
        }
    }

    /**
     * Driver class is an abstraction layer, separating the XMLReader decoding
     * process from the actual XML parsing, which can vary form pull-parsing
     * characters to traversal of DOM elements.
     */
    public static abstract class Driver implements CompositeReader, Closeable {

        private final XMLReader target;

        /**
         * Creates a new instance to be used by the <code>target</code>
         * XMLReader.
         *
         * @param target to use and be used by
         */
        protected Driver(XMLReader target) {
            this.target = target;
        }

        /**
         * Returns this readers root tag setting.
         *
         * @return root tag name
         */
        public String rootTag() {
            return this.target.rootTag;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final boolean readBoolean() {
            return this.target.readBoolean();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final char readChar() {
            return this.target.readChar();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final byte readByte() {
            return this.target.readByte();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final short readShort() {
            return this.target.readShort();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final double readDouble() {
            return this.target.readDouble();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final float readFloat() {
            return this.target.readFloat();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final int readInt() {
            return this.target.readInt();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final long readLong() {
            return this.target.readLong();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public final String readString() {
            return this.target.readString();
        }

        /**
         * Reads from the current position, recursively.
         *
         * @return the read object
         */
        @Override
        public final Object read() {
            if (!this.atElementStart()) {
                throw new InvalidFormatException(this.positionDescriptor(), "element start expected");
            }
            return this.target.read0(null);
        }

        /**
         * Reads from the current position, recursively.
         *
         * @param componentType array componentType, if the expected result is an array with no class attribute,
         *                      null otherwise
         * @return the read object
         */
        @Override
        public final Object readArray(Class componentType) {
            if (!this.atElementStart()) {
                throw new InvalidFormatException(this.positionDescriptor(), "array element start expected");
            }
            return this.target.read0(componentType);
        }

        /**
         * Consumes the current XML element, stopping {@linkplain #atElementEnd()}.
         * Must be {@linkplain #atElementStart()}.
         */
        @Override
        public abstract void consume();

        /**
         * Consumes the XML until the entire content is read.
         */
        protected abstract void consumeFully();

        /**
         * Should release any defined resources.
         */
        @Override
        public abstract void close();
    }

    private static final class StrategyHashMap<S> extends HashMap<String, S> {

        @Override
        public S put(String key, S value) {
            if (!XMLUtil.isLegalXMLTag(key)) {
                throw new IllegalArgumentException("key: not a valid XML tag name: " + key);
            }
            if (value == null) {
                throw new IllegalArgumentException("value: null");
            }
            return super.put(key, value);
        }
    }

    @FunctionalInterface
    interface AliasingReflectionCacheSupplier {

        AliasingReflectionCacheSupplier DEFAULT = HashMap::new;

        Map<String, Object> get();
    }

    private static final char FIELD_FQN_SEPARATOR = '#';
    private Driver driver;
    private boolean beforeRoot;
    /* default*/ String rootTag;
    private Map<String, Object> decoded;
    private boolean sharedConfiguration;
    private UnmarshalContextImpl context;
    /* default*/ Map<String, Object> cachedAliasingReflection;
    private Set<String> maybeExclusions;
    private Map<String, SimpleStrategy> simpleStrategies;
    private Map<String, CompositeStrategy> compositeStrategies;
    /* default*/ SimpleDateFormat dateFormat;
    /* default*/ SecurityPolicy maybeSecurityPolicy;

    /**
     * Creates a new configuration prototype instance.
     * To be used by {@linkplain EasyML} only.
     */
    XMLReader(AliasingReflectionCacheSupplier getAliasingReflectionCache) {
        this.driver = null;
        this.init(getAliasingReflectionCache);
    }

    private void init(AliasingReflectionCacheSupplier getAliasingReflectionCache) {
        this.beforeRoot = true;
        this.rootTag = DTD.ELEMENT_EASYML;
        this.decoded = new HashMap<>();
        this.sharedConfiguration = false;
        this.context = new UnmarshalContextImpl();
        this.cachedAliasingReflection = getAliasingReflectionCache.get();
        this.maybeExclusions = null; // lazy.
        this.simpleStrategies = new StrategyHashMap<>();
        this.compositeStrategies = new StrategyHashMap<>();
        this.dateFormat = new SimpleDateFormat(DTD.FORMAT_DATE);
        this.maybeSecurityPolicy = null; // lazy.
        // add DTD strategies by default:
        this.simpleStrategies.put(DTD.TYPE_BASE64, Base64Strategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_BOOLEAN, BooleanStrategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_DATE, DateStrategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_DOUBLE, DoubleStrategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_INT, IntStrategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_STRING, StringStrategy.INSTANCE);
        // add NON-DTD strategies to support the primitives API:
        this.simpleStrategies.put(ByteStrategy.NAME, ByteStrategy.INSTANCE);
        this.simpleStrategies.put(CharacterStrategy.NAME, CharacterStrategy.INSTANCE);
        this.simpleStrategies.put(FloatStrategy.NAME, FloatStrategy.INSTANCE);
        this.simpleStrategies.put(LongStrategy.NAME, LongStrategy.INSTANCE);
        this.simpleStrategies.put(ShortStrategy.NAME, ShortStrategy.INSTANCE);
    }

    /**
     * Creates a new shared-configuration instance.
     * To be used by {@linkplain EasyML} only.
     */
    XMLReader(XMLReader prototype) {
        this.driver = null;
        this.initIdentically(prototype);
    }

    private void initIdentically(XMLReader other) {
        this.beforeRoot = true;
        this.rootTag = other.rootTag;
        this.decoded = new HashMap<>();
        this.sharedConfiguration = true;
        this.context = new UnmarshalContextImpl();
        this.cachedAliasingReflection = other.cachedAliasingReflection;
        this.maybeExclusions = other.maybeExclusions;
        this.simpleStrategies = other.simpleStrategies;
        this.compositeStrategies = other.compositeStrategies;
        this.dateFormat = new SimpleDateFormat(other.dateFormat.toPattern());
        this.maybeSecurityPolicy = other.maybeSecurityPolicy;
    }

    /**
     * Creates a new instance with the given <code>reader</code> to use and the
     * <code>kXML2</code> parser as default. The parser is set to the given
     * reader.
     *
     * @param reader to read input with
     */
    public XMLReader(Reader reader) {
        this.driver = new XMLReaderTextDriver(this, reader);
        this.init(AliasingReflectionCacheSupplier.DEFAULT);
    }

    /**
     * Creates a new instance with the given <code>in</code> stream to read from
     * and the <code>kXML2</code> parser as default. The parser is set to the
     * given stream.
     *
     * @param in stream from which to read
     */
    public XMLReader(InputStream in) {
        this(new InputStreamReader(in));
    }

    /**
     * Creates a new instance with the given <code>reader</code> to use and the
     * <code>parser</code> to process XML with. The parser is set (or reset) to
     * the given reader.
     *
     * @param reader to read input with
     * @param parser to process the in XML with
     */
    public XMLReader(Reader reader, XmlPullParser parser) {
        this.driver = new XMLReaderTextDriver(this, reader, parser);
        this.init(AliasingReflectionCacheSupplier.DEFAULT);
    }

    /**
     * Creates a new instance with the given <code>in</code> stream to read from
     * and the <code>parser</code> to process XML with. The parser is set to the
     * given stream.
     *
     * @param in     stream from which to read
     * @param parser to process the in XML with
     */
    public XMLReader(InputStream in, XmlPullParser parser) {
        this(new InputStreamReader(in), parser);
    }

    /**
     * Creates a new instance with the given <code>in</code> DOM document to
     * read from.
     *
     * @param in stream from which to read
     */
    public XMLReader(Document in) {
        this.driver = new XMLReaderDOMDriver(this, in);
        this.init(AliasingReflectionCacheSupplier.DEFAULT);
    }

    /**
     * Creates a new instance with the given <code>driver</code>.
     * For generic formats, other than XML.
     *
     * @param driver for generic formats
     */
    public XMLReader(XMLReader.Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("driver: null");
        }
        this.driver = driver;
        this.init(AliasingReflectionCacheSupplier.DEFAULT);
    }

    /**
     * Gets the {@linkplain #sharedConfiguration} property.<br/>
     * If <code>true</code> then this instance may not have its configuration
     * altered.
     *
     * @return the property value
     */
    public boolean isSharedConfiguration() {
        return this.sharedConfiguration;
    }

    /**
     * Gets the {@linkplain #rootTag} property.
     *
     * @return the root tag name
     */
    public String getRootTag() {
        return this.rootTag;
    }

    /**
     * Gets the {@linkplain #simpleStrategies} property.
     *
     * @return the simple strategies
     * @throws IllegalStateException if shared configuration
     */
    public Map<String, SimpleStrategy> getSimpleStrategies() {
        this.checkNotSharedConfiguration();
        return this.simpleStrategies;
    }

    private void checkNotSharedConfiguration() {
        if (this.sharedConfiguration) {
            throw new IllegalStateException("modifying this reader's shared configuration not allowed");
        }
    }

    /**
     * Gets the {@linkplain #compositeStrategies} property.
     *
     * @return the composite strategies
     * @throws IllegalStateException if shared configuration
     */
    public Map<String, CompositeStrategy> getCompositeStrategies() {
        this.checkNotSharedConfiguration();
        return this.compositeStrategies;
    }

    /**
     * Gets the SecurityPolicy property, which is used to create,
     * for security reasons, black- or whitelists of classes to be checked when
     * reading objects. If a security policy is defined and an illegal class is
     * found at read time then the read will halt and throw a
     * {@linkplain IllegalClassException}. The remaining XML structure is
     * consumed and ignored so that subsequent reads can be done.
     * <br/>
     * <br/>
     * <b>Note:</b> after an {@linkplain IllegalClassException} is thrown,
     * subsequent reads can fail if they contain references to the object
     * rejected by the previously failed read.
     * <br/>
     * <br/>
     * <b>Note:</b> the readXXX methods used to read primitives directly,
     * avoiding auto-boxing, do not take into account this setting as they are
     * not vulnerable to XML injection.
     *
     * @return the security policy
     * @throws IllegalStateException if shared configuration
     */
    public SecurityPolicy getSecurityPolicy() {
        this.checkNotSharedConfiguration();
        if (this.maybeSecurityPolicy == null) {
            this.maybeSecurityPolicy = new SecurityPolicy();
        }
        return this.maybeSecurityPolicy;
    }

    /**
     * Sets the {@linkplain #rootTag} property.
     *
     * @param rootTag to be used as XML root tag
     * @throws IllegalArgumentException if tag is null or empty
     * @throws IllegalStateException    if reader isn't in initial state
     */
    public void setRootTag(String rootTag) {
        if (!XMLUtil.isLegalXMLTag(rootTag)) {
            throw new IllegalArgumentException("rootTag: illegal: " + rootTag);
        }
        if (!this.beforeRoot) {
            throw new IllegalStateException("reader read in progress");
        }
        this.checkNotSharedConfiguration();
        this.rootTag = rootTag;
    }

    /**
     * Sets the {@linkplain #dateFormat} property.
     *
     * @param dateFormat to be used by strategies at parsing dates
     * @throws IllegalStateException if shared configuration
     */
    public void setDateFormat(String dateFormat) {
        if (dateFormat == null) {
            throw new IllegalArgumentException("dateFormat: null");
        }
        this.checkNotSharedConfiguration();
        this.dateFormat = new SimpleDateFormat(dateFormat);
    }

    /**
     * Aliases the given class' name with the given <code>alias</code>. The
     * given alias must not be non-null, not-empty and must not contain illegal
     * characters w.r.t. the XML format.
     *
     * @param c     to alias
     * @param alias the alias to set
     * @throws IllegalStateException if shared configuration
     */
    public void alias(Class c, String alias) {
        alias0(c, alias);
    }

    private void alias0(Object aliased, String alias) {
        if (alias == null || alias.isEmpty() || !XMLUtil.isLegalXMLText(alias)) {
            throw new IllegalArgumentException("alias: null, empty, or contains illegal XML chars: " + alias);
        }
        this.checkNotSharedConfiguration();
        this.cachedAliasingReflection.put(alias, aliased);
    }

    /**
     * Aliases the given field's name with the given <code>alias</code>. The
     * given alias must not be non-null, not-empty and must not contain illegal
     * characters w.r.t. the XML format.
     *
     * @param f     to alias
     * @param alias the alias to set
     * @throws IllegalStateException if shared configuration
     */
    public void alias(Field f, String alias) {
        alias0(f, qualifiedFieldKey(f.getDeclaringClass(), alias));
    }

    private static String qualifiedFieldKey(Class declaring, String field) {
        return declaring.getName() + FIELD_FQN_SEPARATOR + field;
    }

    /**
     * Excludes the given field, by name or alias.
     *
     * @param declaring class
     * @param field     name or alias to exclude
     * @throws IllegalStateException if shared configuration
     */
    public void exclude(Class declaring, String field) {
        this.checkNotSharedConfiguration();
        if (maybeExclusions == null) {
            maybeExclusions = new HashSet<>();
        }
        this.maybeExclusions.add(qualifiedFieldKey(declaring, field));
    }

    /**
     * Returns {@code true} if there is more to be read from the current input
     * or {@code false} if the document end tag was reached.
     *
     * @return {@code true} if there are more objects to be read
     */
    public boolean hasMore() {
        return !isRootEnd();
    }

    /**
     * Reads a boolean from XML.
     *
     * @return boolean read
     */
    public boolean readBoolean() {
        return Boolean.parseBoolean(this.readValue(DTD.TYPE_BOOLEAN));
    }

    // reads values for the above api:
    private String readValue(String element) {
        this.ensureRootStartPos();
        if (!this.driver.elementName().equals(element)) {
            throw new InvalidFormatException(this.driver.positionDescriptor(),
                    "expected: " + element + ", found: " + this.driver.elementName());
        }
        final String ret = this.driver.readValue();
        this.driver.next(); // consume element end.
        this.ensureRootEndClear();
        return ret;
    }

    /**
     * Reads a char from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return char read
     */
    public char readChar() {
        final String read = this.readValue(CharacterStrategy.NAME);
        if (read.length() != 1) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "string not a char: " + read);
        }
        return read.charAt(0);
    }

    /**
     * Reads a byte from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return byte read
     */
    public byte readByte() {
        try {
            return Byte.parseByte(this.readValue(ByteStrategy.NAME));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), nfx);
        }
    }

    /**
     * Reads a short from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return short read
     */
    public short readShort() {
        try {
            return Short.parseShort(this.readValue(ShortStrategy.NAME));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), nfx);
        }
    }

    /**
     * Reads a double from XML.
     *
     * @return double read
     */
    public double readDouble() {
        try {
            return Double.parseDouble(this.readValue(DTD.TYPE_DOUBLE));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), nfx);
        }
    }

    /**
     * Reads a float from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return float read
     */
    public float readFloat() {
        try {
            return Float.parseFloat(this.readValue(FloatStrategy.NAME));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), nfx);
        }
    }

    /**
     * Reads an int from XML.
     *
     * @return int read
     */
    public int readInt() {
        try {
            return Integer.parseInt(this.readValue(DTD.TYPE_INT));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), nfx);
        }
    }

    /**
     * Reads a long from XML.
     * <br/>
     * <b>Note: Java specific type</b>
     *
     * @return long read
     */
    public long readLong() {
        try {
            return Long.parseLong(this.readValue(LongStrategy.NAME));
        } catch (NumberFormatException nfx) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), nfx);
        }
    }

    /**
     * Reads a String from XML.
     *
     * @return String read
     */
    public String readString() {
        return this.readValue(DTD.TYPE_STRING);
    }

    /**
     * Reads the object-graph containing values (primitives and wrappers),
     * objects collections, and arrays and returns the graph node which was the
     * starting point of the writing.
     *
     * @return the read start node reference
     * @throws InvalidFormatException when XML structure is invalid
     * @throws IllegalClassException  when XML contains an illegal class w.r.t
     *                                the security policy
     */
    public Object read() {
        return this.readArray(null);
    }

    /**
     * Reads the object-graph containing an array as root node and returns the
     * reference to the read array.
     *
     * @param componentType the class of the array components
     * @return the decoded array with the specified component type
     * @throws InvalidFormatException if XML structure is invalid
     * @throws IllegalClassException  when XML contains an illegal class w.r.t
     *                                the security policy
     */
    public Object readArray(Class componentType) {
        this.ensureRootStartPos();
        try {
            final Object ret = this.read0(componentType);
            return ret;
        } catch (RuntimeException ex) {
            this.driver.consumeFully();
            throw ex;
        } finally {
            this.ensureRootEndClear();
        }
    }

    // reads, always starting at the element start and ending after the element end:
    private Object read0(Class componentType) {
        final String localPartName = this.driver.elementName();
        // simple strategy:
        // non-nil:
        SimpleStrategy ss = this.simpleStrategies.get(localPartName);
        if (ss != null) {
            final Object ret = ss.unmarshal(this.driver.readValue(), this.context);
            // security check:
            this.ensureSecurityPolicy(ret);
            this.driver.next(); // consume element end.
            return ret;
        }
        // nil:
        if (localPartName.equals(DTD.ELEMENT_NIL)) {
            this.driver.next(); // consume nil element start.
            this.driver.next(); // consume nil element end.
            return null;
        }
        // composite strategy:
        // custom:
        try {
            final CompositeStrategy cs = this.compositeStrategies.get(localPartName);
            if (cs != null) {
                // read id attr as the unmarshalNew might move the reader:
                final String idAttr = this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_ID);
                // instantiate, secure and register composite:
                final Object newed = cs.unmarshalNew(this.driver, this.context);
                this.ensureSecurityPolicy(newed);
                this.decoded.put(idAttr, newed);
                // init composite:
                final Object inited = cs.unmarshalInit(newed, this.driver, this.context);
                if (newed != inited) { // for features such as Java IO Serializable readResolve:
                    this.ensureSecurityPolicy(inited);
                    this.decoded.put(idAttr, inited);
                }
                this.driver.next();// consume composite element end.
                return inited;
            }
            // object:
            if (localPartName.equals(DTD.ELEMENT_OBJECT)) {
                final String idRef = this.driver.elementAttribute(DTD.ATTRIBUTE_IDREF);
                if (idRef != null) { // idref-ed object:
                    final Object refed = this.decoded.get(idRef);
                    if (refed == null) {
                        throw new InvalidFormatException(this.driver.positionDescriptor(), "invalid idref: " + idRef);
                    }
                    this.driver.next(); // consume idref-ed object element start.
                    this.driver.next(); // consume idref-ed object element end.
                    return refed;
                }
                return this.readObject(); // also consumes element end.
            }
            // array:
            if (localPartName.equals(DTD.ELEMENT_ARRAY)) {
                return this.readArray0(componentType); // also consumes element end.
            }
            throw new InvalidFormatException(this.driver.positionDescriptor(), "invalid element start: " + localPartName);
        } catch (ClassNotFoundException ex) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "unknown element class: " + ex.getMessage(), ex);
        } catch (NoSuchMethodException ex) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "invalid element class: " + ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "invalid element class: " + ex.getMessage(), ex);
        } catch (InstantiationException ex) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "failed element class instantiation: " + ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "failed element class invocation: " + ex.getMessage(), ex);
        }
    }

    // read0: readObj:
    private Object readObject()
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // read object attributes and create instance and mark it as visited:
        Class cls = this.context.classFor(this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS));
        final Object ret = ReflectionUtil.instantiate(cls);
        // security check:
        this.ensureSecurityPolicy(ret);
        this.decoded.put(this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_ID), ret);
        // read object properties:
        while (this.driver.next()) {
            if (this.driver.atElementStart()) {
                final String localPartName = this.driver.elementName();
                // search the class for the specified property:
                Field f = null;
                boolean skipF = false;
                while (cls != Object.class) {

                    if (this.context.excluded(cls, localPartName)) {
                        skipF = true;
                        break; // skip excluded field search.
                    }

                    try {
                        f = this.context.fieldFor(cls, localPartName);
                        if (!Modifier.isStatic(f.getModifiers())) {
                            break; // field found.
                        }
                    } catch (NoSuchFieldException searchInSuperclass) {
                    }

                    cls = cls.getSuperclass();
                }
                if (skipF) {
                    this.driver.consume();
                    continue; // skip excluded field.
                }
                if (f == null) {
                    throw new InvalidFormatException(this.driver.positionDescriptor(), "undefined property: " + cls.getName() + '.' + localPartName);
                }
                // check if field is indeed an instance property:
                final ReflectionUtil.FieldInfo fi = ReflectionUtil.fieldInfoForWrite(f);
                if (!fi.isProperty) {
                    throw new InvalidFormatException(this.driver.positionDescriptor(), "not a property: " + cls.getName() + '.' + localPartName);
                }
                // move down in the property value and read it:
                if (!this.driver.next() || !this.driver.atElementStart()) {
                    throw new InvalidFormatException(this.driver.positionDescriptor(), "expected element start");
                }
                ReflectionUtil.writeProperty(ret, this.read0(f.getType().getComponentType()), f, fi.accessor);
            } else if (this.driver.atElementEnd() && this.driver.elementName().equals(DTD.ELEMENT_OBJECT)) {
                this.driver.next(); // consume object element end.
                return ret;
            }
        }// while.
        throw new InvalidFormatException(this.driver.positionDescriptor(), "missing element end: " + DTD.ELEMENT_OBJECT);
    }

    // read0: array: componentType can be null if not specified
    private Object readArray0(Class componentType) {
        final Class compType = componentType != null ? componentType : Object.class;
        // read array attributes to create instance nd mark it as visited:
        final String idAttrVal = this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_ID);
        final Object ret = Array.newInstance(compType, Integer.parseInt(this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_LENGTH)));
        // security check:
        this.ensureSecurityPolicy(ret);
        this.decoded.put(idAttrVal, ret);
        this.driver.next(); // consumed array element start.
        // read array items:
        if (compType.isPrimitive()) {
            final ValueType vt = ValueType.of(compType);
            int i = 0;
            while (true) {
                if (this.driver.atElementEnd() && this.driver.elementName().equals(DTD.ELEMENT_ARRAY)) {
                    this.driver.next(); // consumed array element end.
                    return ret;
                }
                vt.setReadArrayItem(this.driver, ret, i);
                i++;
            }
        } else {
            final Object[] arrayRet = (Object[]) ret;
            final Class subCompType = compType.getComponentType();
            int i = 0;
            while (true) {
                if (this.driver.atElementEnd() && this.driver.elementName().equals(DTD.ELEMENT_ARRAY)) {
                    this.driver.next(); // consumed array element end.
                    return ret; // == arrayRet.
                }
                arrayRet[i] = this.read0(subCompType);
                i++;
            }
        }
    }

    private void ensureSecurityPolicy(Object o) {
        if (this.maybeSecurityPolicy != null) {
            this.maybeSecurityPolicy.check(o.getClass());
        }
    }

    private void ensureRootStartPos() {
        if (this.beforeRoot) {
            while (this.driver.next()) {
                if (this.driver.atElementStart()) {
                    if (this.driver.elementName().equals(this.rootTag)) {
                        this.beforeRoot = false;
                        this.driver.next(); // consumed easyml start tag.
                        return;
                    }
                    throw new InvalidFormatException(this.driver.positionDescriptor(), "unexpected element start: " + this.driver.elementName());
                }
            }
        }
    }

    private void ensureRootEndClear() {
        if (isRootEnd()) {
            this.decoded.clear();
            this.beforeRoot = true;
        }
    }

    private boolean isRootEnd() {
        return this.driver.atElementEnd() && this.driver.elementName().equals(this.rootTag);
    }

    /**
     * Resets this instance, setting it to the new <code>reader</code>.
     *
     * @param reader to use from now on
     * @param parser to use, null if default
     */
    public void reset(Reader reader, XmlPullParser parser) {
        if (this.driver != null && this.driver.getClass() == XMLReaderTextDriver.class) {
            ((XMLReaderTextDriver) this.driver).reset(reader, parser);
        } else {
            this.driver = parser != null ?
                    new XMLReaderTextDriver(this, reader, parser) :
                    new XMLReaderTextDriver(this, reader);
        }
        this.decoded.clear();
        this.beforeRoot = true;
    }

    /**
     * Resets this instance, setting it to the new <code>in</code> stream.
     *
     * @param in     to use from now on
     * @param parser to use, null if default
     */
    public void reset(InputStream in, XmlPullParser parser) {
        this.reset(new InputStreamReader(in), parser);
    }

    /**
     * Resets this instance, setting it to the new <code>in</code> DOM. No
     * default pull-parser can be provided as an in-memory DOM will be parsed,
     * not stream text.
     *
     * @param in to use from now on
     */
    public void reset(Document in) {
        this.driver = new XMLReaderDOMDriver(this, in);
        this.decoded.clear();
        this.beforeRoot = true;
    }

    /**
     * Clears the so-far-filled cache of this instance, decreasing memory
     * consumption as well as time performance. If this instance is a prototype
     * for other XMLReaders, it will affect those as well, but in a thread-safe
     * manner.
     * <br>
     * <b>Note:</b> this is an advanced feature and should be used only if the
     * caller knows that this instance (as well as related ones) won't be doing
     * de-serialization for a considerable time interval or will be doing that
     * but on an XML containing a totally different set of object classes.
     *
     * @throws IllegalStateException if shared configuration
     */
    public void clearCache() {
        this.checkNotSharedConfiguration();
        // clear class and field cache:
        final Iterator<Map.Entry<String, Object>> iter = this.cachedAliasingReflection.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<String, Object> crt = iter.next();
            String key = crt.getKey();
            Object val = crt.getValue();
            String name;
            if (val.getClass() == Field.class) {
                key = fieldNameFrom(key);
                name = ((Field) val).getName();
            } else {
                name = ((Class) val).getName();
            }
            if (isCacheEntry(key, name)) {
                iter.remove(); // removed non-alias entry.
            }
        }
        // clear strategies cache:
        this.simpleStrategies.values().forEach(XMLReader::clearCache);
        this.compositeStrategies.values().forEach(XMLReader::clearCache);
    }

    private static String fieldNameFrom(String fieldFQN) {
        return fieldFQN.substring(fieldFQN.indexOf(FIELD_FQN_SEPARATOR) + 1);
    }

    private static boolean isCacheEntry(String key, String name) {
        return key.equals(name);
    }

    private static void clearCache(Strategy s) {
        if (s instanceof Caching) {
            ((Caching) s).clearCache();
        }
    }

    /**
     * Closes this instance by releasing all resources.
     */
    @Override
    public void close() {
        if (this.driver != null) {
            this.driver.close();
        }
        this.decoded.clear();
        this.context = null;
        this.cachedAliasingReflection = null;
        this.maybeExclusions = null;
        this.compositeStrategies = null;
        this.simpleStrategies = null;
        this.maybeSecurityPolicy = null;
    }

    /**
     * Returns an iterator over this reader.
     *
     * @return a lazy iterator
     */
    @Override
    public Iterator iterator() {
        return new Iterator() {

            @Override
            public boolean hasNext() {
                return hasMore();
            }

            @Override
            public Object next() {
                return read();
            }
        };
    }

    /**
     * Returns a stream of over this reader.
     * <b>Note:</b> after execution of the terminal stream operation there are no
     * guarantees that the underlying reader will be at a specific
     * position from which to read the next object.
     *
     * @return a lazy stream of objects
     */
    public Stream stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    /**
     * Returns a typed stream of over this reader.
     * The stream will only contain the objects of the given <code>streamElements</code>
     * type, including nulls.
     * <b>Note:</b> after execution of the terminal stream operation there are no
     * guarantees that the underlying reader will be at a specific
     * position from which to read the next object.
     *
     * @return a lazy typed stream of objects
     */
    public <T> Stream<T> stream(Class<T> streamElements) {
        return stream()
                .filter(o -> o == null || streamElements.isAssignableFrom(o.getClass()));
    }

    /**
     * Gets an object by reading it.
     *
     * @return the read object
     */
    @Override
    public Object get() {
        return this.read();
    }

    /**
     * Gets a boolean by reading it.
     *
     * @return the read boolean
     */
    @Override
    public boolean getAsBoolean() {
        return this.readBoolean();
    }

    /**
     * Gets an int by reading it.
     *
     * @return the read int
     */
    @Override
    public int getAsInt() {
        return this.readInt();
    }

    /**
     * Gets a long by reading it.
     *
     * @return the read long
     */
    @Override
    public long getAsLong() {
        return this.readLong();
    }

    /**
     * Gets a double by reading it.
     *
     * @return the read double
     */
    @Override
    public double getAsDouble() {
        return this.readDouble();
    }

    private final class UnmarshalContextImpl implements UnmarshalContext {

        @Override
        public Class classFor(String aliasOrName) throws ClassNotFoundException {
            final Object cached = cachedAliasingReflection.get(aliasOrName);
            if (cached != null && cached.getClass() == Class.class) {
                return (Class) cached;
            }
            // else cache class:
            final Class ret = ReflectionUtil.classForName(aliasOrName);
            cachedAliasingReflection.putIfAbsent(aliasOrName, ret);
            return ret;
        }

        @Override
        public Field fieldFor(Class declaring, String aliasOrName) throws NoSuchFieldException {
            final String fieldFQN = qualifiedFieldKey(declaring, aliasOrName);
            final Object cached = cachedAliasingReflection.get(fieldFQN);
            if (cached != null && cached.getClass() == Field.class) {
                return (Field) cached;
            }
            // else cache field:
            final Field ret = declaring.getDeclaredField(aliasOrName);
            cachedAliasingReflection.putIfAbsent(fieldFQN, ret);
            return ret;
        }

        @Override
        public boolean excluded(Class declaring, String aliasOrName) {
            return maybeExclusions != null && maybeExclusions.contains(qualifiedFieldKey(declaring, aliasOrName));
        }

        @Override
        public Date parseDate(String date) throws ParseException {
            return dateFormat.parse(date);
        }

        @Override
        public String rootTag() {
            return rootTag;
        }

        @Override
        public String readerPositionDescriptor() {
            return driver.positionDescriptor();
        }
    }
}

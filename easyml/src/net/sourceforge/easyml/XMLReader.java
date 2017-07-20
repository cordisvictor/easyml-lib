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

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.easyml.marshalling.CompositeReader;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.UnmarshalContext;
import net.sourceforge.easyml.marshalling.dtd.*;
import net.sourceforge.easyml.marshalling.java.lang.ByteStrategy;
import net.sourceforge.easyml.marshalling.java.lang.CharacterStrategy;
import net.sourceforge.easyml.marshalling.java.lang.FloatStrategy;
import net.sourceforge.easyml.marshalling.java.lang.LongStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ShortStrategy;
import net.sourceforge.easyml.util.*;
import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
 * String easy =(String) r.read();
 * r.close();
 * </pre>
 * <b>Note:</b> this implementation is NOT thread-safe, but instances with
 * shared configuration can be created, via constructors.
 *
 * @see XMLWriter
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.4.5
 */
public class XMLReader implements Closeable {

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

        /*default*/ SecurityPolicy(boolean whitelist, Class[] classes, Class[] classHierarchies) {
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

        private boolean containsHierarchically(Class c) {
            for (Class root : this.inheritance) {
                if (root.isAssignableFrom(c)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Adds a class to this policy list.
         *
         * @param c to add
         *
         * @return true
         * @throws IllegalArgumentException when c is null or represents an
         * abstract class or interface
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
         *
         * @return true if added, false if root is part of an already added
         * hierarchy
         *
         * @throws IllegalArgumentException when c is null or represents a final
         * class
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

        /**
         * Returns <code>true</code> if the given class is contained within this
         * policy, <code>false</code> otherwise.
         *
         * @param c to search
         *
         * @return true if class has been found, false otherwise
         */
        public boolean contains(Class c) {
            return this.strict.contains(c) || this.containsHierarchically(c);
        }

        /**
         * Checks if the given class is allowed w.r.t this policy.
         *
         * @param c to check
         *
         * @throws IllegalClassException when the given class is contained in
         * this blacklist or not contained in this whitelist
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
         * @return the string representation of this instance and the contained
         * classes
         */
        @Override
        public String toString() {
            return new StringBuilder(this.whitelist ? "whitelist:" : "blacklist:")
                    .append("strict=").append(this.strict)
                    .append(", inheritance=").append(this.inheritance)
                    .toString();
        }
    }//class SecurityPolicy.

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
         * Calculates a descriptor detailing the current driver position inside
         * the XML. This information should be used as exception message for
         * detailing read exceptions.
         *
         * @return the position descriptor
         */
        public abstract String positionDescriptor();

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
         * @param componentType array componentType, if the expected result is
         * an array with no class attribute, null otherwise
         *
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
         * Consumes the XML until the entire subgraph is read.
         */
        protected abstract void consumeFully();

        /**
         * Should release any defined resources.
         */
        @Override
        public abstract void close();
    }//(+)class Driver.

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

    private Driver driver;
    private boolean beforeRoot;
    /* default*/ String rootTag;
    private Map<String, Object> decoded;
    private boolean sharedConfiguration;
    private UnmarshalContextImpl context;
    /* default*/ Map<String, Object> cachedAliasingReflection;
    /* default*/ Map<Class, Object> cachedDefCtors;
    private Caching.CachePutStrategy cachesPut;
    private Map<String, SimpleStrategy> simpleStrategies;
    private Map<String, CompositeStrategy> compositeStrategies;
    /* default*/ SimpleDateFormat dateFormat;
    /* default*/ SecurityPolicy securityPolicy;

    /**
     * Creates a new configuration prototype instance, to be used by
     * {@linkplain EasyML} only.
     */
    /* default*/ XMLReader(Map<Class, Object> ctorCache, Map<String, Object> aliasFieldCache, Caching.CachePutStrategy cachesPut) {
        this.driver = null;
        this.init(ctorCache, aliasFieldCache, cachesPut);
    }

    /**
     * Creates a new shared-configuration instance, to be used by
     * {@linkplain EasyML} only.
     */
    /* default*/ XMLReader(XMLReader configured) {
        this.driver = null;
        this.initIdentically(configured);
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
        this.init();
    }

    /**
     * Creates a new instance with the given <code>in</code> stream to read from
     * and the <code>kXML2</code> parser as default. The parser is set to the
     * given stream.
     *
     * @param in stream from which to read
     */
    public XMLReader(InputStream in) {
        this.driver = new XMLReaderTextDriver(this, new InputStreamReader(in));
        this.init();
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
        this.init();
    }

    /**
     * Creates a new instance with the given <code>in</code> stream to read from
     * and the <code>parser</code> to process XML with. The parser is set to the
     * given stream.
     *
     * @param in stream from which to read
     * @param parser to process the in XML with
     */
    public XMLReader(InputStream in, XmlPullParser parser) {
        this.driver = new XMLReaderTextDriver(this, new InputStreamReader(in), parser);
        this.init();
    }

    /**
     * Creates a new instance with the given <code>in</code> DOM document to
     * read from.
     *
     * @param in stream from which to read
     */
    public XMLReader(Document in) {
        this.driver = new XMLReaderDOMDriver(this, in);
        this.init();
    }

    private void init() {
        this.init(new HashMap<Class, Object>(), new HashMap<String, Object>(), Caching.STRATEGY_PUT);
    }

    private void init(Map<Class, Object> ctorCache, Map<String, Object> aliasFieldCache, Caching.CachePutStrategy cachesPut) {
        this.beforeRoot = true;
        this.rootTag = DTD.ELEMENT_EASYML;
        this.decoded = new HashMap<>();
        this.sharedConfiguration = false;
        this.context = new UnmarshalContextImpl();
        this.cachedDefCtors = ctorCache;
        this.cachedAliasingReflection = aliasFieldCache;
        this.cachesPut = cachesPut;
        this.simpleStrategies = new StrategyHashMap<>();
        this.compositeStrategies = new StrategyHashMap<>();
        this.dateFormat = new SimpleDateFormat(DTD.FORMAT_DATE);
        this.securityPolicy = null; // lazy.
        // add DTD strategies by default:
        this.simpleStrategies.put(DTD.TYPE_BASE64, Base64Strategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_BOOLEAN, BooleanStrategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_DATE, DateStrategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_DOUBLE, DoubleStrategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_INT, IntStrategy.INSTANCE);
        this.simpleStrategies.put(DTD.TYPE_STRING, StringStrategy.INSTANCE);
        // add NON-DTD strategies for primitives, since we need to support the primitive API:
        this.simpleStrategies.put(ByteStrategy.NAME, ByteStrategy.INSTANCE);
        this.simpleStrategies.put(CharacterStrategy.NAME, CharacterStrategy.INSTANCE);
        this.simpleStrategies.put(FloatStrategy.NAME, FloatStrategy.INSTANCE);
        this.simpleStrategies.put(LongStrategy.NAME, LongStrategy.INSTANCE);
        this.simpleStrategies.put(ShortStrategy.NAME, ShortStrategy.INSTANCE);
    }

    private void initIdentically(XMLReader other) {
        this.beforeRoot = true;
        this.rootTag = other.rootTag;
        this.decoded = new HashMap<>();
        this.sharedConfiguration = true;
        this.context = new UnmarshalContextImpl();
        this.cachedAliasingReflection = other.cachedAliasingReflection;
        this.cachedDefCtors = other.cachedDefCtors;
        this.cachesPut = other.cachesPut;
        this.simpleStrategies = other.simpleStrategies;
        this.compositeStrategies = other.compositeStrategies;
        this.dateFormat = new SimpleDateFormat(other.dateFormat.toPattern());
        this.securityPolicy = other.securityPolicy;
    }

    private void checkNotSharedConfiguration() {
        if (this.sharedConfiguration) {
            throw new IllegalStateException("modifying this reader's shared configuration not allowed");
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
     *
     * @throws IllegalStateException if shared configuration
     */
    public Map<String, SimpleStrategy> getSimpleStrategies() {
        this.checkNotSharedConfiguration();
        return this.simpleStrategies;
    }

    /**
     * Gets the {@linkplain #compositeStrategies} property.
     *
     * @return the composite strategies
     *
     * @throws IllegalStateException if shared configuration
     */
    public Map<String, CompositeStrategy> getCompositeStrategies() {
        this.checkNotSharedConfiguration();
        return this.compositeStrategies;
    }

    /**
     * Gets the {@linkplain #securityPolicy} property, which is used to create,
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
     *
     * @throws IllegalStateException if shared configuration
     */
    public SecurityPolicy getSecurityPolicy() {
        this.checkNotSharedConfiguration();
        if (this.securityPolicy == null) {  // lazy:
            this.securityPolicy = new SecurityPolicy();
        }
        return this.securityPolicy;
    }

    /**
     * Sets the {@linkplain #rootTag} property.
     *
     * @param rootTag to be used as XML root tag
     *
     * @throws IllegalArgumentException if tag is null or empty
     * @throws IllegalStateException if reader isn't in initial state
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
     *
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
     * @param c to alias
     * @param alias the alias to set
     *
     * @throws IllegalStateException if shared configuration
     */
    public void alias(Class c, String alias) {
        XMLUtil.validateAlias(alias);
        this.checkNotSharedConfiguration();
        this.cachedAliasingReflection.put(alias, c);
    }

    /**
     * Aliases the given field's name with the given <code>alias</code>. The
     * given alias must not be non-null, not-empty and must not contain illegal
     * characters w.r.t. the XML format.
     *
     * @param f to alias
     * @param alias the alias to set
     *
     * @throws IllegalStateException if shared configuration
     */
    public void alias(Field f, String alias) {
        XMLUtil.validateAlias(alias);
        this.checkNotSharedConfiguration();
        this.cachedAliasingReflection.put(
                ReflectionUtil.qualifiedNameFor(f.getDeclaringClass(), alias),
                f);
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

    // read0: array: componentType can be null if not specified
    private Object readArray0(Class componentType)
            throws XmlPullParserException, IOException, ClassNotFoundException, InstantiationException,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Class compType = componentType != null ? componentType : Object.class;
        // read array attributes to create instance nd mark it as visited:
        final String idAttrVal = this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_ID);
        final Object ret = Array.newInstance(compType, Integer.parseInt(this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_LENGTH)));
        // security check:
        this.ensureSecurityPolicy(ret);
        this.decoded.put(idAttrVal, ret);
        this.driver.next(); // consumed array element start.
        // read array items:
        final ValueType pvt = ValueType.ofPrimitive(compType);
        if (pvt != null) { // primitives array:
            int i = 0;
            while (true) {
                if (this.driver.atElementEnd() && this.driver.elementName().equals(DTD.ELEMENT_ARRAY)) {
                    this.driver.next(); // consumed array element end.
                    return ret;
                }
                pvt.setReadArrayItem(this.driver, ret, i);
                i++;
            }
        } else { // objects array:
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

    // read0: readObj:
    private Object readObject()
            throws XmlPullParserException, IOException, ClassNotFoundException, InstantiationException,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // read object attributes and create instance and mark it as visited:
        Class cls = this.context.classFor(this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_CLASS));
        final Object ret = this.context.defaultConstructorFor(cls).newInstance();
        // security check:
        this.ensureSecurityPolicy(ret);
        this.decoded.put(this.driver.elementRequiredAttribute(DTD.ATTRIBUTE_ID), ret);
        // read object properties:
        while (this.driver.next()) {
            if (this.driver.atElementStart()) {
                final String localPartName = this.driver.elementName();
                // search the class for the specified property:
                Field f = null;
                while (cls != Object.class) {
                    try {
                        f = this.context.fieldFor(cls, localPartName);
                        if (!Modifier.isStatic(f.getModifiers())) {
                            break; // field found.
                        }
                    } catch (NoSuchFieldException searchInSuperclass) {
                    } catch (SecurityException ex) {
                        throw new InvalidFormatException(this.driver.positionDescriptor(), ex);
                    }
                    cls = cls.getSuperclass();
                }
                // check if field is indeed an instance property:
                if (f == null || Modifier.isStatic(f.getModifiers()) || !ReflectionUtil.hasClassFieldProperty(cls, f)) {
                    throw new InvalidFormatException(this.driver.positionDescriptor(), "undefined property: " + cls.getName() + '.' + localPartName);
                }
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                // move down in the property value and read it:
                if (!this.driver.next() || !this.driver.atElementStart()) {
                    throw new InvalidFormatException(this.driver.positionDescriptor(), "expected element start");
                }
                f.set(ret, this.read0(f.getType().getComponentType()));
            } else if (this.driver.atElementEnd() && this.driver.elementName().equals(DTD.ELEMENT_OBJECT)) {
                this.driver.next(); // consume object element end.
                return ret;
            }
        }// while.
        throw new InvalidFormatException(this.driver.positionDescriptor(), "missing element end: " + DTD.ELEMENT_OBJECT);
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
        } catch (XmlPullParserException | IOException ioX) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), ioX);
        } catch (ClassNotFoundException iB) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "unknown object class: " + iB.getMessage(), iB);
        } catch (NoSuchMethodException nsmX) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "invalid object constructor", nsmX);
        } catch (InstantiationException iDC) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "invalid object constructor: " + iDC.getMessage(), iDC);
        } catch (InvocationTargetException iA) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "invalid object accessor: " + iA.getMessage(), iA);
        } catch (IllegalAccessException iDC) {
            throw new InvalidFormatException(this.driver.positionDescriptor(), "invalid object constructor modifier: " + iDC.getMessage(), iDC);
        }
    }

    /**
     * Reads the object-graph containing values (primitives and wrappers),
     * objects collections, and arrays and returns the graph node which was the
     * starting point of the writing.
     *
     * @return the read start node reference
     *
     * @throws InvalidFormatException when XML structure is invalid
     * @throws IllegalClassException when XML contains an illegal class w.r.t
     * the security policy
     */
    public Object read() {
        return this.readArray(null);
    }

    /**
     * Reads the object-graph containing an array as root node and returns the
     * reference to the read array.
     *
     * @param componentType the class of the array components
     *
     * @return the decoded array with the specified component type
     *
     * @throws InvalidFormatException if XML structure is invalid
     * @throws IllegalClassException when XML contains an illegal class w.r.t
     * the security policy
     */
    public Object readArray(Class componentType) {
        this.ensureRootStartPos();
        try {
            final Object ret = this.read0(componentType);
            return ret;
        } catch (IllegalClassException ex) {
            this.driver.consumeFully();
            throw ex;
        } finally {
            this.ensureRootEndClear();
        }
    }

    private void ensureSecurityPolicy(Object o) {
        if (this.securityPolicy != null) {
            this.securityPolicy.check(o.getClass());
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
        if (isReusableXMLReaderTextDriver()) {
            ((XMLReaderTextDriver) this.driver).reset(reader, parser);
        } else {
            this.driver = parser != null ? new XMLReaderTextDriver(this, reader, parser)
                    : new XMLReaderTextDriver(this, reader);
        }
        this.decoded.clear();
        this.beforeRoot = true;
    }

    private boolean isReusableXMLReaderTextDriver() {
        return this.driver != null && this.driver.getClass() == XMLReaderTextDriver.class;
    }

    /**
     * Resets this instance, setting it to the new <code>in</code> stream.
     *
     * @param in to use from now on
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
        this.cachedDefCtors.clear();
        final Iterator<Map.Entry<String, Object>> iter = this.cachedAliasingReflection.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<String, Object> crt = iter.next();
            final Object crtVal = crt.getValue();
            if (crtVal.getClass() == Field.class) {
                if (ReflectionUtil.fieldNameFor(crt.getKey())
                        .equals(
                                ((Field) crtVal).getName())) {
                    iter.remove(); // removed non-alias entry.
                }
            } else if (crtVal.getClass() == Class.class) {
                if (crt.getKey().equals(((Class) crtVal).getName())) {
                    iter.remove(); // removed non-alias entry.
                }
            }
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
        this.cachedDefCtors = null;
        this.compositeStrategies = null;
        this.simpleStrategies = null;
        this.securityPolicy = null;
    }

    private final class UnmarshalContextImpl implements UnmarshalContext {

        @Override
        public <T> Constructor<T> defaultConstructorFor(Class<T> c)
                throws NoSuchMethodException {
            final Object cached = cachedDefCtors.get(c);
            if (cached != null) {
                if (cached.getClass() != Constructor.class) {
                    throw (NoSuchMethodException) cached;
                }
                return (Constructor<T>) cached;
            }
            try {
                final Constructor<T> ctor = ReflectionUtil.defaultConstructor(c);
                cachesPut.put(cachedDefCtors, c, ctor);
                return ctor;
            } catch (NoSuchMethodException noDefCtorX) {
                cachesPut.put(cachedDefCtors, c, noDefCtorX);
                throw noDefCtorX;
            }
        }

        @Override
        public Class aliasedClassFor(String alias) {
            final Object cached = cachedAliasingReflection.get(alias);
            if (cached != null && cached.getClass() == Class.class) {
                final Class ret = (Class) cached;
                if (!ret.getName().equals(alias)) {
                    return ret;
                }
            }
            return null;
        }

        @Override
        public Field aliasedFieldFor(Class declaring, String alias) {
            final Object cached = cachedAliasingReflection.get(
                    ReflectionUtil.qualifiedNameFor(declaring, alias));
            if (cached != null && cached.getClass() == Field.class) {
                final Field ret = (Field) cached;
                if (!ret.getName().equals(alias)) {
                    return ret;
                }
            }
            return null;
        }

        @Override
        public Class classFor(String aliasOrName) throws ClassNotFoundException {
            final Object cached = cachedAliasingReflection.get(aliasOrName);
            if (cached != null && cached.getClass() == Class.class) {
                return (Class) cached;
            }
            // else cache class:
            final Class ret = ReflectionUtil.classForName(aliasOrName);
            cachesPut.put(cachedAliasingReflection, aliasOrName, ret);
            return ret;
        }

        @Override
        public Field fieldFor(Class declaring, String aliasOrName) throws NoSuchFieldException {
            final String fieldFQN = ReflectionUtil.qualifiedNameFor(declaring, aliasOrName);
            final Object cached = cachedAliasingReflection.get(fieldFQN);
            if (cached != null && cached.getClass() == Field.class) {
                return (Field) cached;
            }
            // else cache field:
            final Field ret = declaring.getDeclaredField(aliasOrName);
            cachesPut.put(cachedAliasingReflection, fieldFQN, ret);
            return ret;
        }

        @Override
        public String readerPositionDescriptor() {
            return driver.positionDescriptor();
        }

        @Override
        public Date parseDate(String date) throws ParseException {
            return dateFormat.parse(date);
        }

        @Override
        public String rootTag() {
            return rootTag;
        }
    }//(+)class UnmarshalContextImpl.
}

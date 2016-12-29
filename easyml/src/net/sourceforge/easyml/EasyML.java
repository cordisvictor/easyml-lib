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
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.custom.NodeListStrategy;
import net.sourceforge.easyml.marshalling.custom.NodeStrategy;
import net.sourceforge.easyml.marshalling.java.awt.ColorStrategy;
import net.sourceforge.easyml.marshalling.java.io.FileStrategy;
import net.sourceforge.easyml.marshalling.java.io.SerializableStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ArrayStrategy;
import net.sourceforge.easyml.marshalling.java.lang.CharsStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ClassStrategy;
import net.sourceforge.easyml.marshalling.java.lang.EnumStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ObjectStrategy;
import net.sourceforge.easyml.marshalling.java.lang.ObjectStrategyV1_3_4;
import net.sourceforge.easyml.marshalling.java.lang.StackTraceElementStrategy;
import net.sourceforge.easyml.marshalling.java.lang.StringBufferStrategy;
import net.sourceforge.easyml.marshalling.java.lang.StringBuilderStrategy;
import net.sourceforge.easyml.marshalling.java.math.BigDecimalStrategy;
import net.sourceforge.easyml.marshalling.java.math.BigIntegerStrategy;
import net.sourceforge.easyml.marshalling.java.net.URIStrategy;
import net.sourceforge.easyml.marshalling.java.net.URLStrategy;
import net.sourceforge.easyml.marshalling.java.util.ArrayListStrategy;
import net.sourceforge.easyml.marshalling.java.util.HashMapStrategy;
import net.sourceforge.easyml.marshalling.java.util.HashSetStrategy;
import net.sourceforge.easyml.marshalling.java.util.HashtableStrategy;
import net.sourceforge.easyml.marshalling.java.util.IdentityHashMapStrategy;
import net.sourceforge.easyml.marshalling.java.util.LinkedHashMapStrategy;
import net.sourceforge.easyml.marshalling.java.util.LinkedHashSetStrategy;
import net.sourceforge.easyml.marshalling.java.util.LinkedListStrategy;
import net.sourceforge.easyml.marshalling.java.util.LocaleStrategy;
import net.sourceforge.easyml.marshalling.java.util.PropertiesStrategy;
import net.sourceforge.easyml.marshalling.java.util.SingletonListStrategy;
import net.sourceforge.easyml.marshalling.java.util.SingletonMapStrategy;
import net.sourceforge.easyml.marshalling.java.util.SingletonSetStrategy;
import net.sourceforge.easyml.marshalling.java.util.StackStrategy;
import net.sourceforge.easyml.marshalling.java.util.TreeMapStrategy;
import net.sourceforge.easyml.marshalling.java.util.UUIDStrategy;
import net.sourceforge.easyml.marshalling.java.util.VectorStrategy;
import net.sourceforge.easyml.marshalling.java.util.regex.PatternStrategy;
import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;

/**
 * EasyML class is the top-level facade, containing general functionalities such
 * as serializing and de-serializing.
 * <br/>
 * Usage example:
 * <pre>
 * final EasyML easyml = new EasyML();
 * // object to XML string:
 * String xml= easyml.serialize(obj);
 * // object to XML file:
 * final FileWriter fw= new FileWriter(FILE_NAME);
 * easyml.serialize(obj, fw);
 * fw.close();
 * </pre>
 *
 * Not all functionalities are accessible through EasyML, such as successive
 * serializing of objects in the same output. Hence, for multiple read/writes on
 * the same stream, use the <code>newReader()</code> and
 * <code>newWriter()</code> methods, or the {@linkplain XMLReader}s and
 * {@linkplain XMLWriter}s directly, for example:
 * <br/>
 * <pre>
 * final XMLWriter w= easyml.newWriter(new FileWriter(FILE_NAME));
 * w.writeInt(1);
 * w.writeInt(2);
 * w.write(obj1);
 * //..
 * w.write(objN);
 * w.close();
 * </pre>
 *
 * <b>Note:</b> this implementation is <b>mostly(1)</b> thread-safe, by creating
 * per-thread instances of {@linkplain XMLReader} and {@linkplain XMLWriter}
 * with shared configuration, when invoking <code>serialize()</code> or
 * <code>deserialize()</code> methods.
 * <p>
 * <b>(1):</b> configuration methods (for example: setXXX, alias, exclude,
 * register, unregister, and deserializationSecurityPolicy) change the state of
 * EasyML instances they are invoked on.<br/>
 * Hence, all configuration and customization of an EasyML instance must be made
 * before concurrent threads will use that EasyML instance for
 * serializing/de-serializing objects.<br/>
 * If, for any reason, an EasyML instance is reconfigured during various stages
 * of the user-program runtime, a read-write lock must be put in place.</br/>
 * The read-write lock must be implemented as follows:
 * <ul>
 * <li>read operations - EasyML serialization/de-serialization methods</li>
 * <li>write operations - EasyML configuration methods</li>
 * </ul>
 * <br/>
 * The most frequent use-case, however, is considered to be the instantiation
 * and configuration of EasyML objects in a single-threaded context, followed by
 * concurrent usage of those instances. Example of an EasyML instance,
 * configured within a singleton class, which is initialized only once, by the
 * class loader:
 * <pre>
 * public final class PersonDAO {
 *
 *   public static final PersonDAO INSTANCE = new PersonDAO();
 *
 *   private final EasyML easyml;
 *
 *   private PersonDAO() {
 *     // init with default conf:
 *     easyml = new EasyML();
 *     // set more readable XML, if put in files:
 *     easyml.setStyle(EasyML.Style.PRETTY);
 *     // set a custom XML parser, if needed:
 *     easyml.setXmlPullParserProvider(//..);
 *     // set a shorter, more readable class name:
 *     easyml.alias(Person.class, "person");
 *     // set excludes on sensitive-data fields:
 *     try {
 *         easyml.exclude(Person.class, "password");
 *     } catch (NoSuchFieldException ignored) {}
 *     // set blacklist on exploitable classes:
 *     easyml.deserializationSecurityPolicy().setBlacklistMode();
 *     easyml.deserializationSecurityPolicy().add(java.beans.EventHandler.class);
 *     //..
 *   }
 * //..
 * }
 * </pre>
 *
 * @see XMLReader
 * @see XMLWriter
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.9
 */
public class EasyML {

    /**
     * Style enum defines standard XML outputting styles for the EasyML.
     *
     * @see XMLWriter
     *
     * @author Victor Cordis ( cordis.victor at gmail.com)
     * @since 1.0
     * @version 1.3.7
     */
    public enum Style {

        /**
         * Sacrifices readability and non-redundancy in favor of speed and low
         * memory-footprint, by disabling pretty printing and skip defaults.
         */
        FAST {
                    @Override
                    public void applyTo(XMLWriter writer) {
                        writer.setPrettyPrint(false);
                        writer.setSkipDefaults(false);
                    }
                },
        /**
         * Sacrifices readability in favor of size, by disabling pretty printing
         * and enabling skip defaults.
         */
        COMPRESSED {
                    @Override
                    public void applyTo(XMLWriter writer) {
                        writer.setPrettyPrint(false);
                        writer.setSkipDefaults(true);
                    }
                },
        /**
         * More in favor of readability: enables pretty printing but still skips
         * defaults.
         */
        PRETTY {
                    @Override
                    public void applyTo(XMLWriter writer) {
                        writer.setPrettyPrint(true);
                        writer.setSkipDefaults(true);
                    }
                },
        /**
         * Sacrifices size in favor of readability, by enabling pretty printing
         * and disabling skip defaults.
         */
        DETAILED {
                    @Override
                    public void applyTo(XMLWriter writer) {
                        writer.setPrettyPrint(true);
                        writer.setSkipDefaults(false);
                    }
                };

        /**
         * Applies to the given <code>writer</code>.
         *
         * @param writer to configure
         */
        public abstract void applyTo(XMLWriter writer);
    }

    /**
     * Profile enum defines standard configuration profiles for the EasyML
     * extension for the Java programming language. These configurations consist
     * of strategy usage and aliasing. <br/> The user can choose between
     * applying one of the standard profiles, altering the applied profile, or
     * manually configuring {@linkplain XMLReader}, {@linkplain XMLWriter}
     * instances from scratch.
     *
     * @see XMLReader
     * @see XMLWriter
     *
     * @author Victor Cordis ( cordis.victor at gmail.com)
     * @since 1.0
     * @version 1.3.5
     */
    public enum Profile {

        /**
         * Sacrifices efficiency for maximal portability, in the idea that
         * non-DTD types can be passed between languages which define similar
         * types. For example: passing StringBuilder between Java and C#.
         * <br/>The goal XML is human-readable and the contained structures
         * should be as generic and aliased as possible.
         */
        GENERIC {

                    @Override
                    public void configure(XMLWriter writer) {
                        final XMLWriter.StrategyRegistry<SimpleStrategy> simple = writer.getSimpleStrategies();
                        // are included by default, because of the primitives API:
//                        simple.add(ByteStrategy.INSTANCE);
//                        simple.add(CharacterStrategy.INSTANCE);
//                        simple.add(FloatStrategy.INSTANCE);
//                        simple.add(LongStrategy.INSTANCE);
//                        simple.add(ShortStrategy.INSTANCE);
                        simple.add(FileStrategy.INSTANCE);
                        simple.add(CharsStrategy.INSTANCE);
                        simple.add(ClassStrategy.INSTANCE);
                        simple.add(StringBufferStrategy.INSTANCE);
                        simple.add(StringBuilderStrategy.INSTANCE);
                        simple.add(BigDecimalStrategy.INSTANCE);
                        simple.add(BigIntegerStrategy.INSTANCE);
                        simple.add(URIStrategy.INSTANCE);
                        simple.add(URLStrategy.INSTANCE);
                        simple.add(LocaleStrategy.INSTANCE);
                        simple.add(UUIDStrategy.INSTANCE);
                        final XMLWriter.StrategyRegistry<CompositeStrategy> composite = writer.getCompositeStrategies();
                        composite.add(ColorStrategy.INSTANCE);
                        composite.add(ArrayListStrategy.INSTANCE);
                        composite.add(HashMapStrategy.INSTANCE);
                        composite.add(HashSetStrategy.INSTANCE);
                        composite.add(HashtableStrategy.INSTANCE);
                        composite.add(TreeMapStrategy.INSTANCE);
                        composite.add(IdentityHashMapStrategy.INSTANCE);
                        composite.add(LinkedHashMapStrategy.INSTANCE);
                        composite.add(LinkedHashSetStrategy.INSTANCE);
                        composite.add(LinkedListStrategy.INSTANCE);
                        composite.add(PropertiesStrategy.INSTANCE);
                        composite.add(VectorStrategy.INSTANCE);
                        composite.add(StackStrategy.INSTANCE);
                    }

                    @Override
                    public void configure(XMLReader reader) {
                        final Map<String, SimpleStrategy> simple = reader.getSimpleStrategies();
                        // are included by default, because of the primitives API:
//                        simple.put(ByteStrategy.NAME, ByteStrategy.INSTANCE);
//                        simple.put(CharacterStrategy.NAME, CharacterStrategy.INSTANCE);
//                        simple.put(FloatStrategy.NAME, FloatStrategy.INSTANCE);
//                        simple.put(LongStrategy.NAME, LongStrategy.INSTANCE);
//                        simple.put(ShortStrategy.NAME, ShortStrategy.INSTANCE);
                        simple.put(FileStrategy.NAME, FileStrategy.INSTANCE);
                        simple.put(CharsStrategy.NAME, CharsStrategy.INSTANCE);
                        simple.put(ClassStrategy.NAME, ClassStrategy.INSTANCE);
                        simple.put(StringBufferStrategy.NAME, StringBufferStrategy.INSTANCE);
                        simple.put(StringBuilderStrategy.NAME, StringBuilderStrategy.INSTANCE);
                        simple.put(BigDecimalStrategy.NAME, BigDecimalStrategy.INSTANCE);
                        simple.put(BigIntegerStrategy.NAME, BigIntegerStrategy.INSTANCE);
                        simple.put(URIStrategy.NAME, URIStrategy.INSTANCE);
                        simple.put(URLStrategy.NAME, URLStrategy.INSTANCE);
                        simple.put(LocaleStrategy.NAME, LocaleStrategy.INSTANCE);
                        simple.put(UUIDStrategy.NAME, UUIDStrategy.INSTANCE);
                        final Map<String, CompositeStrategy> composite = reader.getCompositeStrategies();
                        composite.put(ColorStrategy.NAME, ColorStrategy.INSTANCE);
                        composite.put(ArrayListStrategy.NAME, ArrayListStrategy.INSTANCE);
                        composite.put(HashMapStrategy.NAME, HashMapStrategy.INSTANCE);
                        composite.put(HashSetStrategy.NAME, HashSetStrategy.INSTANCE);
                        composite.put(HashtableStrategy.NAME, HashtableStrategy.INSTANCE);
                        composite.put(TreeMapStrategy.NAME, TreeMapStrategy.INSTANCE);
                        composite.put(IdentityHashMapStrategy.NAME, IdentityHashMapStrategy.INSTANCE);
                        composite.put(LinkedHashMapStrategy.NAME, LinkedHashMapStrategy.INSTANCE);
                        composite.put(LinkedHashSetStrategy.NAME, LinkedHashSetStrategy.INSTANCE);
                        composite.put(LinkedListStrategy.NAME, LinkedListStrategy.INSTANCE);
                        composite.put(PropertiesStrategy.NAME, PropertiesStrategy.INSTANCE);
                        composite.put(VectorStrategy.NAME, VectorStrategy.INSTANCE);
                        composite.put(StackStrategy.NAME, StackStrategy.INSTANCE);
                    }

                },
        /**
         * Sacrifices portability for maximal efficiency and Java-specific
         * support. The goal XML is human-readable but contains structures which
         * are Java-API dependent, such as the Java IO Serialization.
         */
        SPECIFIC {

                    @Override
                    public void configure(XMLWriter writer) {
                        final XMLWriter.StrategyRegistry<SimpleStrategy> simple = writer.getSimpleStrategies();
                        // are included by default, because of the primitives API:
//                        simple.add(ByteStrategy.INSTANCE);
//                        simple.add(CharacterStrategy.INSTANCE);
//                        simple.add(FloatStrategy.INSTANCE);
//                        simple.add(LongStrategy.INSTANCE);
//                        simple.add(ShortStrategy.INSTANCE);
                        simple.add(FileStrategy.INSTANCE);
                        simple.add(CharsStrategy.INSTANCE);
                        simple.add(ClassStrategy.INSTANCE);
                        simple.add(EnumStrategy.INSTANCE);
                        simple.add(StackTraceElementStrategy.INSTANCE);
                        simple.add(StringBufferStrategy.INSTANCE);
                        simple.add(StringBuilderStrategy.INSTANCE);
                        simple.add(BigDecimalStrategy.INSTANCE);
                        simple.add(BigIntegerStrategy.INSTANCE);
                        simple.add(URIStrategy.INSTANCE);
                        simple.add(URLStrategy.INSTANCE);
                        simple.add(LocaleStrategy.INSTANCE);
                        simple.add(UUIDStrategy.INSTANCE);
                        final XMLWriter.StrategyRegistry<CompositeStrategy> composite = writer.getCompositeStrategies();
                        composite.add(ColorStrategy.INSTANCE);
                        composite.add(ArrayStrategy.INSTANCE);
                        composite.add(ObjectStrategy.INSTANCE);
                        composite.add(SerializableStrategy.INSTANCE);
                        composite.add(ArrayListStrategy.INSTANCE);
                        composite.add(HashMapStrategy.INSTANCE);
                        composite.add(HashSetStrategy.INSTANCE);
                        composite.add(HashtableStrategy.INSTANCE);
                        composite.add(IdentityHashMapStrategy.INSTANCE);
                        // TreeMapStrategy: is not configured here; TreeMap is handled by
                        // SerializableStrategy, resulting in a more Java-specific XML,
                        // but deserialization will be faster using TreeMap.readObject(stream):
                        // composite.add(TreeMapStrategy.INSTANCE);
                        composite.add(LinkedHashMapStrategy.INSTANCE);
                        composite.add(LinkedHashSetStrategy.INSTANCE);
                        composite.add(LinkedListStrategy.INSTANCE);
                        composite.add(PropertiesStrategy.INSTANCE);
                        composite.add(VectorStrategy.INSTANCE);
                        composite.add(StackStrategy.INSTANCE);
                        composite.add(SingletonSetStrategy.INSTANCE);
                        composite.add(SingletonListStrategy.INSTANCE);
                        composite.add(SingletonMapStrategy.INSTANCE);
                        composite.add(PatternStrategy.INSTANCE);
                    }

                    @Override
                    public void configure(XMLReader reader) {
                        final Map<String, SimpleStrategy> simple = reader.getSimpleStrategies();
                        // are included by default, because of the primitives API:
//                        simple.put(ByteStrategy.NAME, ByteStrategy.INSTANCE);
//                        simple.put(CharacterStrategy.NAME, CharacterStrategy.INSTANCE);
//                        simple.put(FloatStrategy.NAME, FloatStrategy.INSTANCE);
//                        simple.put(LongStrategy.NAME, LongStrategy.INSTANCE);
//                        simple.put(ShortStrategy.NAME, ShortStrategy.INSTANCE);
                        simple.put(FileStrategy.NAME, FileStrategy.INSTANCE);
                        simple.put(CharsStrategy.NAME, CharsStrategy.INSTANCE);
                        simple.put(ClassStrategy.NAME, ClassStrategy.INSTANCE);
                        simple.put(EnumStrategy.NAME, EnumStrategy.INSTANCE);
                        simple.put(StackTraceElementStrategy.NAME, StackTraceElementStrategy.INSTANCE);
                        simple.put(StringBufferStrategy.NAME, StringBufferStrategy.INSTANCE);
                        simple.put(StringBuilderStrategy.NAME, StringBuilderStrategy.INSTANCE);
                        simple.put(BigDecimalStrategy.NAME, BigDecimalStrategy.INSTANCE);
                        simple.put(BigIntegerStrategy.NAME, BigIntegerStrategy.INSTANCE);
                        simple.put(URIStrategy.NAME, URIStrategy.INSTANCE);
                        simple.put(URLStrategy.NAME, URLStrategy.INSTANCE);
                        simple.put(LocaleStrategy.NAME, LocaleStrategy.INSTANCE);
                        simple.put(UUIDStrategy.NAME, UUIDStrategy.INSTANCE);
                        final Map<String, CompositeStrategy> composite = reader.getCompositeStrategies();
                        composite.put(ColorStrategy.NAME, ColorStrategy.INSTANCE);
                        composite.put(SerializableStrategy.NAME, SerializableStrategy.INSTANCE);
                        // backwards compatibility: EasyML 1.3.5 with 1.3.4 or less:
                        composite.put(ArrayStrategy.NAME, ArrayStrategy.INSTANCE);
                        composite.put(ArrayStrategy.NAME_1_3_4, ArrayStrategy.INSTANCE);
                        composite.put(ObjectStrategy.NAME, ObjectStrategy.INSTANCE);
                        composite.put(ObjectStrategyV1_3_4.NAME, ObjectStrategyV1_3_4.INSTANCE);
                        // backwards compatibility.
                        composite.put(ArrayListStrategy.NAME, ArrayListStrategy.INSTANCE);
                        composite.put(HashMapStrategy.NAME, HashMapStrategy.INSTANCE);
                        composite.put(HashSetStrategy.NAME, HashSetStrategy.INSTANCE);
                        composite.put(HashtableStrategy.NAME, HashtableStrategy.INSTANCE);
                        composite.put(IdentityHashMapStrategy.NAME, IdentityHashMapStrategy.INSTANCE);
                        // TreeMapStrategy: just as in the case of the writer,
                        // this strategy is not configured here; TreeMap is handled by
                        // SerializableStrategy, resulting in a more Java-specific XML,
                        // but deserialization will be faster using TreeMap.readObject(stream):
                        // composite.put(TreeMapStrategy.NAME, TreeMapStrategy.INSTANCE);
                        composite.put(LinkedHashMapStrategy.NAME, LinkedHashMapStrategy.INSTANCE);
                        composite.put(LinkedHashSetStrategy.NAME, LinkedHashSetStrategy.INSTANCE);
                        composite.put(LinkedListStrategy.NAME, LinkedListStrategy.INSTANCE);
                        composite.put(PropertiesStrategy.NAME, PropertiesStrategy.INSTANCE);
                        composite.put(VectorStrategy.NAME, VectorStrategy.INSTANCE);
                        composite.put(StackStrategy.NAME, StackStrategy.INSTANCE);
                        composite.put(SingletonSetStrategy.NAME, SingletonSetStrategy.INSTANCE);
                        composite.put(SingletonListStrategy.NAME, SingletonListStrategy.INSTANCE);
                        composite.put(SingletonMapStrategy.NAME, SingletonMapStrategy.INSTANCE);
                        composite.put(PatternStrategy.NAME, PatternStrategy.INSTANCE);
                    }

                };

        /**
         * Configures the given <code>writer</code>.
         *
         * @param writer to configure
         */
        public abstract void configure(XMLWriter writer);

        /**
         * Configures the given <code>reader</code>.
         *
         * @param reader to configure
         */
        public abstract void configure(XMLReader reader);
    }

    /**
     * XmlPullParserProvider interface can be implemented to provide a custom
     * {@linkplain XmlPullParser} to be used at deserializing XML, in text form.
     * <br/>
     * Since EasyML is thread-safe, it cannot reuse the user-specified
     * pull-parser instance, like {@linkplain XMLReader} does. This interface
     * implementation will be used by EasyML at each text XML
     * <code>deserialize()</code> invocation.<br/>
     * <br/>
     * <b>Note:</b> implementations of this interface MUST be thread-safe.
     *
     * @author Victor Cordis ( cordis.victor at gmail.com)
     * @since 1.3.0
     * @version 1.3.0
     */
    public interface XmlPullParserProvider {

        /**
         * Creates and configures a new user-specified
         * <code>XmlPullParser</code> parser, to be used at text XML parsing.
         *
         * @return new xml pull-parser instance
         */
        XmlPullParser newXmlPullParser();
    }

    /**
     * The writer configuration prototype. Is configured the same as it's reader
     * counterpart.
     */
    protected final XMLWriter writerPrototype;
    /**
     * The reader configuration prototype. Is configured the same as it's writer
     * counterpart.
     */
    protected final XMLReader readerPrototype;
    /**
     * The per-thread writer.
     */
    protected final ThreadLocal<XMLWriter> perThreadWriter;
    /**
     * The per-thread reader.
     */
    protected final ThreadLocal<XMLReader> perThreadReader;
    /**
     * The preferred parser configuration. Is optional.
     */
    protected XmlPullParserProvider xmlPullParserProvider = null;

    /**
     * Creates a new instance with the default profile of reader and writer
     * strategies.
     */
    public EasyML() {
        this(Profile.SPECIFIC);
    }

    /**
     * Creates a new instance with the he given <code>profile</code> of reader
     * and writer strategies.
     *
     * @param profile to use
     */
    public EasyML(Profile profile) {
        final ConcurrentHashMap<Class, Object> commonCtorCache = new ConcurrentHashMap<>();
        this.writerPrototype = new XMLWriter(commonCtorCache);
        this.readerPrototype = new XMLReader(commonCtorCache);
        profile.configure(this.writerPrototype);
        profile.configure(this.readerPrototype);
        this.perThreadWriter = new ThreadLocal<XMLWriter>() {

            @Override
            protected XMLWriter initialValue() {
                return new XMLWriter(writerPrototype);
            }
        };
        this.perThreadReader = new ThreadLocal<XMLReader>() {

            @Override
            protected XMLReader initialValue() {
                return new XMLReader(readerPrototype);
            }
        };
    }

    /**
     * Sets a user-defined XML pull-parser provider, to be used at text xml
     * de-serialization.
     * <br/>
     * <b>Note:</b> the custom parser instance will be ignored when
     * de-serializing from DOM documents and not from text input streams.
     *
     * @param xmlPullParserProvider implementation to be used by the reader
     */
    public void setXmlPullParserProvider(XmlPullParserProvider xmlPullParserProvider) {
        this.xmlPullParserProvider = xmlPullParserProvider;
    }

    /**
     * Sets the format to use at XML date formatting and parsing. This is done
     * by re-configuring both the XML reader and writer with the given format.
     *
     * @param format to use at date formatting and parsing
     */
    public void setDateFormat(String format) {
        this.writerPrototype.setDateFormat(format);
        this.readerPrototype.setDateFormat(format);
    }

    /**
     * Sets the given custom XML tag name to be used as the XML root tag,
     * replacing the default {@linkplain DTD#ELEMENT_EASYML}.
     *
     * @param tag to be used as XML root tag name
     *
     * @throws IllegalArgumentException if tag is null or empty
     * @throws IllegalStateException if reader or writer aren't in initial state
     */
    public void setCustomRootTag(String tag) {
        this.writerPrototype.setRootTag(tag);
        this.readerPrototype.setRootTag(tag);
    }

    /**
     * Sets the given custom XML tag name for strings.
     *
     * @param tag to be used for strings
     *
     * @return true if set, false if tag name conflicting with other strategy
     * names
     */
    public boolean setCustomStringTag(String tag) {
        if (!this.readerPrototype.getSimpleStrategies().containsKey(tag)) {
            final NodeStrategy ns = new NodeStrategy(tag);
            this.writerPrototype.getSimpleStrategies().add(ns);
            this.readerPrototype.getSimpleStrategies().put(tag, ns);
            return true;
        }
        return false;
    }

    /**
     * Sets the given custom XML tag name for arrays.
     *
     * @param tag to be used for arrays
     * @param array class of the array to use this custom tag
     *
     * @return true if set, false if tag name conflicting with other strategy
     * names
     */
    public boolean setCustomArrayTag(String tag, Class array) {
        if (!this.readerPrototype.getCompositeStrategies().containsKey(tag)) {
            final NodeListStrategy nls = new NodeListStrategy(tag, array);
            this.writerPrototype.getCompositeStrategies().add(nls);
            this.readerPrototype.getCompositeStrategies().put(tag, nls);
            return true;
        }
        return false;
    }

    /**
     * Sets the XML outputting style .
     *
     * @param style to use
     */
    public void setStyle(EasyML.Style style) {
        style.applyTo(this.writerPrototype);
    }

    /**
     * Aliases the given class with the given <code>alias</code>.
     *
     * @param c to alias
     * @param alias to use
     *
     * @return the previous alias, if any,for the given class
     */
    public String alias(Class c, String alias) {
        this.readerPrototype.alias(c, alias);
        return this.writerPrototype.alias(c, alias);
    }

    /**
     * Aliases the given field with the given <code>alias</code>.
     *
     * @param f to alias
     * @param alias to use
     *
     * @return the previous alias, if any,for the given field
     */
    public String alias(Field f, String alias) {
        this.readerPrototype.alias(f, alias);
        return this.writerPrototype.alias(f, alias);
    }

    /**
     * Aliases the given field, specified by <code>declaring</code> class and
     * <code>field</code> name, with the given <code>alias</code>.
     *
     * @param declaring class declaring the field
     * @param field the name of the field
     * @param alias to use
     *
     * @throws NoSuchFieldException if field is not found in the declaring class
     * @return the previous alias, if any,for the given field
     */
    public String alias(Class declaring, String field, String alias) throws NoSuchFieldException {
        final Field f = declaring.getDeclaredField(field);
        this.readerPrototype.alias(f, alias);
        return this.writerPrototype.alias(f, alias);
    }

    /**
     * Excludes the given field.
     *
     * @param f to exclude
     */
    public void exclude(Field f) {
        this.writerPrototype.exclude(f);
    }

    /**
     * Excludes the given field, specified by <code>declaring</code> class and
     * <code>field</code> name.
     *
     * @param declaring class declaring the field
     * @param field the name of the field to exclude
     *
     * @throws NoSuchFieldException if field is not found in the declaring class
     */
    public void exclude(Class declaring, String field) throws NoSuchFieldException {
        this.writerPrototype.exclude(declaring.getDeclaredField(field));
    }

    /**
     * Gets the deserialization security policy, which is used to create, for
     * security reasons, black- or whitelists of classes to be checked when
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
     */
    public XMLReader.SecurityPolicy deserializationSecurityPolicy() {
        return this.readerPrototype.getSecurityPolicy();
    }

    /**
     * Returns the {@linkplain SimpleStrategy} with the given
     * <code>target</code>, or <code>null</code> if not found. One should also
     * be interested in the {@linkplain SimpleStrategy#strict()} value.
     *
     * @param target class of the strategy target
     *
     * @return the found strategy, if any
     */
    public SimpleStrategy lookupSimpleStrategyBy(Class target) {
        return this.writerPrototype.getSimpleStrategies().lookup(target);
    }

    /**
     * Returns the {@linkplain SimpleStrategy} with the given <code>name</code>,
     * or <code>null</code> if not found.
     *
     * @param name the name of the strategy
     *
     * @return the found strategy, if any
     */
    public SimpleStrategy lookupSimpleStrategyBy(String name) {
        return this.readerPrototype.getSimpleStrategies().get(name);
    }

    /**
     * Returns the {@linkplain CompositeStrategy} with the given
     * <code>target</code>, or <code>null</code> if not found. One should also
     * be interested in the {@linkplain CompositeStrategy#strict()} value.
     *
     * @param target class of the strategy target
     *
     * @return the found strategy, if any
     */
    public CompositeStrategy lookupCompositeStrategyBy(Class target) {
        return this.writerPrototype.getCompositeStrategies().lookup(target);
    }

    /**
     * Returns the {@linkplain CompositeStrategy} with the given
     * <code>name</code>, or <code>null</code> if not found.
     *
     * @param name the name of the strategy
     *
     * @return the found strategy, if any
     */
    public CompositeStrategy lookupCompositeStrategyBy(String name) {
        return this.readerPrototype.getCompositeStrategies().get(name);
    }

    /**
     * Registers the given strategy to both reader and writer.
     *
     * @param s to register
     */
    public void register(SimpleStrategy s) {
        this.writerPrototype.getSimpleStrategies().add(s);
        this.readerPrototype.getSimpleStrategies().put(s.name(), s);
    }

    /**
     * Registers the given strategy to both reader and writer.
     *
     * @param c to register
     */
    public void register(CompositeStrategy c) {
        this.writerPrototype.getCompositeStrategies().add(c);
        this.readerPrototype.getCompositeStrategies().put(c.name(), c);
    }

    /**
     * Unregisters the given strategy from both reader and writer.
     *
     * @param s to unregister
     *
     * @return the unregistered simple strategy, if any
     */
    public SimpleStrategy unregister(SimpleStrategy s) {
        this.writerPrototype.getSimpleStrategies().remove(s);
        return this.readerPrototype.getSimpleStrategies().remove(s.name());
    }

    /**
     * Unregisters the given strategy from both reader and writer.
     *
     * @param c to unregister
     *
     * @return the unregistered composite strategy, if any
     */
    public CompositeStrategy unregister(CompositeStrategy c) {
        this.writerPrototype.getCompositeStrategies().remove(c);
        return this.readerPrototype.getCompositeStrategies().remove(c.name());
    }

    /**
     * Unregisters the simple strategy, identified by name, from both reader and
     * writer.
     *
     * @param strategyName name of the simple strategy
     *
     * @return the unregistered simple strategy, if any
     */
    public SimpleStrategy unregisterSimple(String strategyName) {
        final SimpleStrategy found = this.readerPrototype.getSimpleStrategies().remove(strategyName);
        if (found != null) {
            this.writerPrototype.getSimpleStrategies().remove(found);
        }
        return found;
    }

    /**
     * Unregisters the composite strategy, identified by name, from both reader
     * and writer.
     *
     * @param strategyName name of the composite strategy
     *
     * @return the unregistered composite strategy, if any
     */
    public CompositeStrategy unregisterComposite(String strategyName) {
        final CompositeStrategy found = this.readerPrototype.getCompositeStrategies().remove(strategyName);
        if (found != null) {
            this.writerPrototype.getCompositeStrategies().remove(found);
        }
        return found;
    }

    /**
     * Creates a new shared-configuration writer with the given
     * <code>out</code>. Use this method to directly access the XMLWriter API,
     * which offers features such as write-primitives and multiple writes to
     * same out. Otherwise, use
     * {@linkplain #serialize(java.lang.Object, java.io.Writer)}.
     * <br>
     * <b>Note:</b> the returned writer shall be closed by the caller.
     *
     * @param out to write to
     *
     * @return a new shared-configuration writer
     */
    public XMLWriter newWriter(Writer out) {
        return new XMLWriter(out, this.writerPrototype);
    }

    /**
     * Creates a new shared-configuration writer with the given
     * <code>out</code>. Use this method to directly access the XMLWriter API,
     * which offers features such as write-primitives and multiple writes to
     * same out. Otherwise, use
     * {@linkplain #serialize(java.lang.Object, java.io.OutputStream)}.
     * <br>
     * <b>Note:</b> the returned writer shall be closed by the caller.
     *
     * @param out to write to
     *
     * @return a new shared-configuration writer
     */
    public XMLWriter newWriter(OutputStream out) {
        return new XMLWriter(out, this.writerPrototype);
    }

    /**
     * Creates a new shared-configuration writer with the given
     * <code>out</code>. Use this method to directly access the XMLWriter API,
     * which offers features such as write-primitives and multiple writes to
     * same out. Otherwise, use
     * {@linkplain #serialize(java.lang.Object, org.w3c.dom.Document)}.
     * <br>
     * <b>Note:</b> the returned writer shall be closed by the caller.
     * <br/>
     * <b>Note: USE THIS METHOD IF AND ONLY IF THE DOM IS NEEDED IN-MEMORY FOR
     * OTHER REASONS THAN EASYML SERIALIZATION TO AN OUTPUTSTREAM</b>
     *
     * @param out to write to
     *
     * @return a new shared-configuration writer
     */
    public XMLWriter newWriter(Document out) {
        return new XMLWriter(out, this.writerPrototype);
    }

    /**
     * Creates a new shared-configuration reader with the given <code>in</code>.
     * Use this method to directly access the XMLReader API, which offers
     * features such as read-primitives and multiple reads from same in.
     * Otherwise, use {@linkplain #deserialize(java.io.Reader)}.
     * <br>
     * <b>Note:</b> the returned reader shall be closed by the caller.
     *
     * @param in to read from
     *
     * @return a new shared-configuration reader
     */
    public XMLReader newReader(Reader in) {
        return this.xmlPullParserProvider != null
                ? new XMLReader(in, this.xmlPullParserProvider.newXmlPullParser(), this.readerPrototype)
                : new XMLReader(in, this.readerPrototype);
    }

    /**
     * Creates a new shared-configuration reader with the given <code>in</code>.
     * Use this method to directly access the XMLReader API, which offers
     * features such as read-primitives and multiple reads from same in.
     * Otherwise, use {@linkplain #deserialize(java.io.InputStream)}.
     * <br>
     * <b>Note:</b> the returned reader shall be closed by the caller.
     *
     * @param in to read from
     *
     * @return a new shared-configuration reader
     */
    public XMLReader newReader(InputStream in) {
        return this.newReader(new InputStreamReader(in));
    }

    /**
     * Creates a new shared-configuration reader with the given <code>in</code>.
     * Use this method to directly access the XMLReader API, which offers
     * features such as read-primitives and multiple reads from same in.
     * Otherwise, use {@linkplain #deserialize(org.w3c.dom.Document)}.
     * <br>
     * <b>Note:</b> the returned reader shall be closed by the caller.
     * <br/>
     * <b>Note: USE THIS METHOD IF AND ONLY IF THE DOM IS NEEDED IN-MEMORY FOR
     * OTHER REASONS THAN EASYML SERIALIZATION TO AN OUTPUTSTREAM</b>
     *
     * @param in to read from
     *
     * @return a new shared-configuration reader
     */
    public XMLReader newReader(Document in) {
        return new XMLReader(in, this.readerPrototype);
    }

    /**
     * Serializes the given object, writing it with the given writer. Does not
     * support multiple writes; for that use an XML writer directly.
     * <br>
     * <b>Note:</b> the out parameter shall be closed by the caller.
     *
     * @param o to serialize
     * @param out to write with
     */
    public void serialize(Object o, Writer out) {
        final XMLWriter writer = this.initPerThreadWriter();
        writer.reset(out);
        writer.write(o);
        writer.flush();
    }

    /**
     * Serializes the given object to the given output stream. Does not support
     * multiple writes; for that use an XML writer directly.
     * <br>
     * <b>Note:</b> the out parameter shall be closed by the caller.
     *
     * @param o to serialize
     * @param out to write to
     */
    public void serialize(Object o, OutputStream out) {
        this.serialize(o, new OutputStreamWriter(out));
    }

    /**
     * Serializes the given object to it's EasyML string representation. Does
     * not support multiple writes, because the returned string is immutable.
     *
     * @param o single object to serialize
     *
     * @return the serialized object string
     */
    public String serialize(Object o) {
        final StringWriter sw = new StringWriter(32);
        this.serialize(o, sw);
        return sw.toString();
    }

    /**
     * Serializes the given object to the given DOM document. Does not support
     * multiple writes; for that use an XML writer directly.
     * <br/>
     * <b>Note: USE THIS METHOD IF AND ONLY IF THE DOM IS NEEDED IN-MEMORY FOR
     * OTHER REASONS THAN EASYML SERIALIZATION TO AN OUTPUTSTREAM</b>
     *
     * @param o to serialize
     * @param out empty DOM to populate
     */
    public void serialize(Object o, Document out) {
        final XMLWriter writer = this.initPerThreadWriter();
        writer.reset(out);
        writer.write(o);
        writer.flush();
    }

    /**
     * De-serializes using the given reader.
     * <br>
     * <b>Note:</b> the in parameter shall be closed by the caller.
     *
     * @param in to use
     *
     * @return the de-serialized object
     */
    public Object deserialize(Reader in) {
        final XMLReader reader = this.initPerThreadReader();
        reader.reset(in, this.xmlPullParserProvider != null
                ? this.xmlPullParserProvider.newXmlPullParser()
                : null
        );
        return reader.read();
    }

    /**
     * De-serializes from the given input stream.
     * <br>
     * <b>Note:</b> the in parameter shall be closed by the caller.
     *
     * @param in to read from
     *
     * @return the de-serialized object
     */
    public Object deserialize(InputStream in) {
        return this.deserialize(new InputStreamReader(in));
    }

    /**
     * De-serializes a single object from the easyml string. This method is the
     * inverse for {@linkplain #serialize(java.lang.Object)} and shall be used
     * only on output from that method.
     *
     * @param easyml format to parse, generated by
     * {@linkplain #serialize(java.lang.Object)}
     *
     * @return the de-serialized single object
     */
    public Object deserialize(String easyml) {
        return this.deserialize(new StringReader(easyml));
    }

    /**
     * De-serializes from the given DOM document.
     * <br/>
     * <b>Note: USE THIS METHOD IF AND ONLY IF THE DOM IS ALREADY IN-MEMORY FOR
     * OTHER REASONS THAN EASYML DE-SERIALIZATION</b>
     *
     * @param in to use
     *
     * @return the de-serialized object
     */
    public Object deserialize(Document in) {
        final XMLReader reader = this.initPerThreadReader();
        reader.reset(in);
        return reader.read();
    }

    private XMLWriter initPerThreadWriter() {
        final XMLWriter writer = this.perThreadWriter.get();
        // ensure value-type conf changes are made visible
        // on the per-thread writer:
        writer.skipDefaults = this.writerPrototype.skipDefaults;
        writer.prettyPrint = this.writerPrototype.prettyPrint;
        writer.rootTag = this.writerPrototype.rootTag;
        final String configuredPattern = this.writerPrototype.dateFormat.toPattern();
        if (!configuredPattern.equals(writer.dateFormat.toPattern())) {
            writer.dateFormat.applyPattern(configuredPattern);
        }
        return writer;
    }

    private XMLReader initPerThreadReader() {
        final XMLReader reader = this.perThreadReader.get();
        // ensure value-type conf changes are made visible
        // on the per-thread reader:
        reader.securityPolicy = this.readerPrototype.securityPolicy; // securityPolicy is lazy.
        reader.rootTag = this.readerPrototype.rootTag;
        final String configuredPattern = this.readerPrototype.dateFormat.toPattern();
        if (!configuredPattern.equals(reader.dateFormat.toPattern())) {
            reader.dateFormat.applyPattern(configuredPattern);
        }
        return reader;
    }

    /**
     * Releases the XML writer, if any, belonging to the current thread. If this
     * method isn't invoked, the XML writer will be released anyway at the
     * current threads death.
     * <br>
     * <b>Note:</b> this is an advanced feature and should be used only if the
     * caller knows this thread won't be using this EasyML instance for
     * serialization anymore.
     */
    public void releaseCurrentWriter() {
        this.perThreadWriter.remove();
    }

    /**
     * Releases the XML reader, if any, belonging to the current thread. If this
     * method isn't invoked, the XML reader will be released anyway at the
     * current threads death.
     * <br>
     * <b>Note:</b> this is an advanced feature and should be used only if the
     * caller knows this thread won't be using this EasyML instance for
     * de-serialization anymore.
     */
    public void releaseCurrentReader() {
        this.perThreadReader.remove();
    }

    /**
     * Clears the so-far-filled cache of this instance, decreasing memory
     * consumption as well as time performance. This method is thread-safe, but
     * it would only make sense to invoke it on a temporarily idle EasyML
     * instance.
     * <br>
     * <b>Note:</b> this is an advanced feature and should be used only if the
     * caller knows that this EasyML instance won't be doing serialization /
     * de-serialization for a considerable time interval or will be doing that
     * but on a totally different set of input / output object classes, making
     * the already cached data irrelevant.
     */
    public void clearCache() {
        // clear reader cache:
        this.readerPrototype.clearCache();
        // clear writer cache:
        this.writerPrototype.clearCache();
    }
}

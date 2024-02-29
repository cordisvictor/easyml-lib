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

import net.sourceforge.easyml.marshalling.CompositeStrategy;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.easyml.marshalling.java.awt.ColorStrategy;
import net.sourceforge.easyml.marshalling.java.io.ExternalizableStrategy;
import net.sourceforge.easyml.marshalling.java.io.FileStrategy;
import net.sourceforge.easyml.marshalling.java.io.SerializableStrategy;
import net.sourceforge.easyml.marshalling.java.lang.*;
import net.sourceforge.easyml.marshalling.java.math.BigDecimalStrategy;
import net.sourceforge.easyml.marshalling.java.math.BigIntegerStrategy;
import net.sourceforge.easyml.marshalling.java.net.URIStrategy;
import net.sourceforge.easyml.marshalling.java.net.URLStrategy;
import net.sourceforge.easyml.marshalling.java.time.*;
import net.sourceforge.easyml.marshalling.java.util.*;
import net.sourceforge.easyml.marshalling.java.util.concurrent.ConcurrentHashMapStrategy;
import net.sourceforge.easyml.marshalling.java.util.concurrent.atomic.AtomicBooleanStrategy;
import net.sourceforge.easyml.marshalling.java.util.concurrent.atomic.AtomicIntegerStrategy;
import net.sourceforge.easyml.marshalling.java.util.concurrent.atomic.AtomicLongStrategy;
import net.sourceforge.easyml.marshalling.java.util.concurrent.atomic.AtomicReferenceStrategy;
import net.sourceforge.easyml.marshalling.java.util.regex.PatternStrategy;
import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * EasyML class is the top-level facade, containing general functionality such
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
 * </pre> The entire functionality of this framework isn't accessible through
 * EasyML. Features such as successive serializing of objects in the same output
 * can be accessed by using the <code>newReader()</code> and
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
 * <b>Note:</b> this implementation is thread-safe, by creating per-thread
 * instances of {@linkplain XMLReader} and {@linkplain XMLWriter} with shared
 * configuration, when invoking <code>serialize()</code> or
 * <code>deserialize()</code> methods. All configuration and customization of an
 * EasyML is done at {@linkplain EasyMLBuilder#build()} time, before concurrent
 * threads will use the resulting EasyML instance for serializing/de-serializing
 * objects.<br/>
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.3
 * @see XMLReader
 * @see XMLWriter
 * @since 1.0
 */
public final class EasyML {

    /**
     * Style enum defines standard XML outputting styles for the EasyML.
     *
     * @author Victor Cordis ( cordis.victor at gmail.com)
     * @version 1.3.7
     * @see XMLWriter
     * @since 1.0
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
     * The writer configuration prototype, configured the same as its reader
     * counterpart.
     */
    protected final XMLWriter writerPrototype;
    /**
     * The reader configuration prototype, configured the same as its writer
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
    protected Supplier<XmlPullParser> xmlPullParserProvider = null;

    /**
     * Creates a new instance with the default settings and default reader and
     * writer strategies.
     */
    public EasyML() {
        this.writerPrototype = new XMLWriter();
        this.readerPrototype = new XMLReader(new ConcurrentHashMap<>());
        defaultConfiguration(this.writerPrototype);
        defaultConfiguration(this.readerPrototype);
        this.perThreadWriter = ThreadLocal.withInitial(() -> new XMLWriter(writerPrototype));
        this.perThreadReader = ThreadLocal.withInitial(() -> new XMLReader(readerPrototype));
    }

    /**
     * Configures all default strategies to the given writer.
     *
     * @param writer to configure
     */
    public static void defaultConfiguration(XMLWriter writer) {
        final XMLWriter.StrategyRegistry<SimpleStrategy> simple = writer.getSimpleStrategies();
        final XMLWriter.StrategyRegistry<CompositeStrategy> composite = writer.getCompositeStrategies();
        // dtd: because of the primitives API, DTD are included by default in the writer:
//        simple.add(Base64Strategy.INSTANCE);
//        simple.add(BooleanStrategy.INSTANCE);
//        simple.add(DateStrategy.INSTANCE);
//        simple.add(DoubleStrategy.INSTANCE);
//        simple.add(IntStrategy.INSTANCE);
//        simple.add(StringStrategy.INSTANCE);
        // awt:
        composite.add(ColorStrategy.INSTANCE);
        // io:
        composite.add(ExternalizableStrategy.INSTANCE);
        simple.add(FileStrategy.INSTANCE);
        composite.add(new SerializableStrategy());
        // lang: because of the primitives API, non-DTD value types are included by default in the writer:
//        simple.add(ByteStrategy.INSTANCE);
//        simple.add(CharacterStrategy.INSTANCE);
//        simple.add(FloatStrategy.INSTANCE);
//        simple.add(LongStrategy.INSTANCE);
//        simple.add(ShortStrategy.INSTANCE);
        composite.add(ArrayStrategy.INSTANCE);
        simple.add(CharsStrategy.INSTANCE);
        simple.add(ClassStrategy.INSTANCE);
        simple.add(EnumStrategy.INSTANCE);
        composite.add(ObjectStrategy.INSTANCE);
        simple.add(StackTraceElementStrategy.INSTANCE);
        simple.add(StringBufferStrategy.INSTANCE);
        simple.add(StringBuilderStrategy.INSTANCE);
        // math:
        simple.add(BigDecimalStrategy.INSTANCE);
        simple.add(BigIntegerStrategy.INSTANCE);
        // net:
        simple.add(URIStrategy.INSTANCE);
        simple.add(URLStrategy.INSTANCE);
        // time:
        simple.add(ChronologyStrategy.INSTANCE);
        simple.add(DurationStrategy.INSTANCE);
        simple.add(InstantStrategy.INSTANCE);
        simple.add(LocalDateStrategy.INSTANCE);
        simple.add(LocalDateTimeStrategy.INSTANCE);
        simple.add(LocalTimeStrategy.INSTANCE);
        simple.add(MonthDayStrategy.INSTANCE);
        simple.add(PeriodStrategy.INSTANCE);
        simple.add(YearMonthStrategy.INSTANCE);
        simple.add(YearStrategy.INSTANCE);
        simple.add(ZonedDateTimeStrategy.INSTANCE);
        simple.add(ZoneIdStrategy.INSTANCE);
        // util:
        composite.add(ArrayListStrategy.INSTANCE);
        composite.add(BitSetStrategy.INSTANCE);
        composite.add(CalendarStrategy.INSTANCE);
        composite.add(EnumMapStrategy.INSTANCE);
        composite.add(EnumSetStrategy.INSTANCE);
        composite.add(EmptyListStrategy.INSTANCE);
        composite.add(EmptyMapStrategy.INSTANCE);
        composite.add(EmptySetStrategy.INSTANCE);
        composite.add(HashMapStrategy.INSTANCE);
        composite.add(HashSetStrategy.INSTANCE);
        composite.add(HashtableStrategy.INSTANCE);
        composite.add(IdentityHashMapStrategy.INSTANCE);
        composite.add(LinkedHashMapStrategy.INSTANCE);
        composite.add(LinkedHashSetStrategy.INSTANCE);
        composite.add(LinkedListStrategy.INSTANCE);
        simple.add(LocaleStrategy.INSTANCE);
        composite.add(OptionalStrategy.INSTANCE);
        composite.add(PropertiesStrategy.INSTANCE);
        composite.add(SingletonListStrategy.INSTANCE);
        composite.add(SingletonMapStrategy.INSTANCE);
        composite.add(SingletonSetStrategy.INSTANCE);
        composite.add(StackStrategy.INSTANCE);
        composite.add(TreeMapStrategy.INSTANCE);
        composite.add(TreeSetStrategy.INSTANCE);
        simple.add(TimeZoneStrategy.INSTANCE);
        simple.add(UUIDStrategy.INSTANCE);
        composite.add(VectorStrategy.INSTANCE);
        // util.concurrent:
        composite.add(ConcurrentHashMapStrategy.INSTANCE);
        // util.concurrent.atomic:
        simple.add(AtomicBooleanStrategy.INSTANCE);
        simple.add(AtomicIntegerStrategy.INSTANCE);
        simple.add(AtomicLongStrategy.INSTANCE);
        composite.add(AtomicReferenceStrategy.INSTANCE);
        // util.regex:
        composite.add(PatternStrategy.INSTANCE);
    }

    /**
     * Configures all default strategies to the given reader.
     *
     * @param reader to configure
     */
    public static void defaultConfiguration(XMLReader reader) {
        final Map<String, SimpleStrategy> simple = reader.getSimpleStrategies();
        final Map<String, CompositeStrategy> composite = reader.getCompositeStrategies();
        // dtd: because of the primitives API, DTD are included by default in the reader:
//        simple.put(Base64Strategy.NAME, Base64Strategy.INSTANCE);
//        simple.put(BooleanStrategy.NAME, BooleanStrategy.INSTANCE);
//        simple.put(DateStrategy.NAME, DateStrategy.INSTANCE);
//        simple.put(DoubleStrategy.NAME, DoubleStrategy.INSTANCE);
//        simple.put(IntStrategy.NAME, IntStrategy.INSTANCE);
//        simple.put(StringStrategy.NAME, StringStrategy.INSTANCE);
        // awt:
        composite.put(ColorStrategy.NAME, ColorStrategy.INSTANCE);
        // io:
        composite.put(ExternalizableStrategy.NAME, ExternalizableStrategy.INSTANCE);
        simple.put(FileStrategy.NAME, FileStrategy.INSTANCE);
        composite.put(SerializableStrategy.NAME, new SerializableStrategy());
        // lang: because of the primitives API, non-DTD value types are included by default in the reader:
//        simple.put(ByteStrategy.NAME, ByteStrategy.INSTANCE);
//        simple.put(CharacterStrategy.NAME, CharacterStrategy.INSTANCE);
//        simple.put(FloatStrategy.NAME, FloatStrategy.INSTANCE);
//        simple.put(LongStrategy.NAME, LongStrategy.INSTANCE);
//        simple.put(ShortStrategy.NAME, ShortStrategy.INSTANCE);
        composite.put(ArrayStrategy.NAME, ArrayStrategy.INSTANCE);
        simple.put(CharsStrategy.NAME, CharsStrategy.INSTANCE);
        simple.put(ClassStrategy.NAME, ClassStrategy.INSTANCE);
        simple.put(EnumStrategy.NAME, EnumStrategy.INSTANCE);
        composite.put(ObjectStrategy.NAME, ObjectStrategy.INSTANCE);
        simple.put(StackTraceElementStrategy.NAME, StackTraceElementStrategy.INSTANCE);
        simple.put(StringBufferStrategy.NAME, StringBufferStrategy.INSTANCE);
        simple.put(StringBuilderStrategy.NAME, StringBuilderStrategy.INSTANCE);
        // math:
        simple.put(BigDecimalStrategy.NAME, BigDecimalStrategy.INSTANCE);
        simple.put(BigIntegerStrategy.NAME, BigIntegerStrategy.INSTANCE);
        // net:
        simple.put(URIStrategy.NAME, URIStrategy.INSTANCE);
        simple.put(URLStrategy.NAME, URLStrategy.INSTANCE);
        // time:
        simple.put(ChronologyStrategy.NAME, ChronologyStrategy.INSTANCE);
        simple.put(DurationStrategy.NAME, DurationStrategy.INSTANCE);
        simple.put(InstantStrategy.NAME, InstantStrategy.INSTANCE);
        simple.put(LocalDateStrategy.NAME, LocalDateStrategy.INSTANCE);
        simple.put(LocalDateTimeStrategy.NAME, LocalDateTimeStrategy.INSTANCE);
        simple.put(LocalTimeStrategy.NAME, LocalTimeStrategy.INSTANCE);
        simple.put(MonthDayStrategy.NAME, MonthDayStrategy.INSTANCE);
        simple.put(PeriodStrategy.NAME, PeriodStrategy.INSTANCE);
        simple.put(YearMonthStrategy.NAME, YearMonthStrategy.INSTANCE);
        simple.put(YearStrategy.NAME, YearStrategy.INSTANCE);
        simple.put(ZonedDateTimeStrategy.NAME, ZonedDateTimeStrategy.INSTANCE);
        simple.put(ZoneIdStrategy.NAME, ZoneIdStrategy.INSTANCE);
        // util:
        composite.put(ArrayListStrategy.NAME, ArrayListStrategy.INSTANCE);
        composite.put(BitSetStrategy.NAME, BitSetStrategy.INSTANCE);
        composite.put(CalendarStrategy.NAME, CalendarStrategy.INSTANCE);
        composite.put(EnumMapStrategy.NAME, EnumMapStrategy.INSTANCE);
        composite.put(EnumSetStrategy.NAME, EnumSetStrategy.INSTANCE);
        composite.put(EmptyListStrategy.NAME, EmptyListStrategy.INSTANCE);
        composite.put(EmptyMapStrategy.NAME, EmptyMapStrategy.INSTANCE);
        composite.put(EmptySetStrategy.NAME, EmptySetStrategy.INSTANCE);
        composite.put(HashMapStrategy.NAME, HashMapStrategy.INSTANCE);
        composite.put(HashSetStrategy.NAME, HashSetStrategy.INSTANCE);
        composite.put(HashtableStrategy.NAME, HashtableStrategy.INSTANCE);
        composite.put(IdentityHashMapStrategy.NAME, IdentityHashMapStrategy.INSTANCE);
        composite.put(LinkedHashMapStrategy.NAME, LinkedHashMapStrategy.INSTANCE);
        composite.put(LinkedHashSetStrategy.NAME, LinkedHashSetStrategy.INSTANCE);
        composite.put(LinkedListStrategy.NAME, LinkedListStrategy.INSTANCE);
        simple.put(LocaleStrategy.NAME, LocaleStrategy.INSTANCE);
        composite.put(OptionalStrategy.NAME, OptionalStrategy.INSTANCE);
        composite.put(PropertiesStrategy.NAME, PropertiesStrategy.INSTANCE);
        composite.put(SingletonListStrategy.NAME, SingletonListStrategy.INSTANCE);
        composite.put(SingletonMapStrategy.NAME, SingletonMapStrategy.INSTANCE);
        composite.put(SingletonSetStrategy.NAME, SingletonSetStrategy.INSTANCE);
        composite.put(StackStrategy.NAME, StackStrategy.INSTANCE);
        composite.put(TreeMapStrategy.NAME, TreeMapStrategy.INSTANCE);
        composite.put(TreeSetStrategy.NAME, TreeSetStrategy.INSTANCE);
        simple.put(TimeZoneStrategy.NAME, TimeZoneStrategy.INSTANCE);
        simple.put(UUIDStrategy.NAME, UUIDStrategy.INSTANCE);
        composite.put(VectorStrategy.NAME, VectorStrategy.INSTANCE);
        // util.concurrent:
        composite.put(ConcurrentHashMapStrategy.NAME, ConcurrentHashMapStrategy.INSTANCE);
        // util.concurrent.atomic:
        simple.put(AtomicBooleanStrategy.NAME, AtomicBooleanStrategy.INSTANCE);
        simple.put(AtomicIntegerStrategy.NAME, AtomicIntegerStrategy.INSTANCE);
        simple.put(AtomicLongStrategy.NAME, AtomicLongStrategy.INSTANCE);
        composite.put(AtomicReferenceStrategy.NAME, AtomicReferenceStrategy.INSTANCE);
        // util.regex:
        composite.put(PatternStrategy.NAME, PatternStrategy.INSTANCE);
    }

    EasyML(Style style, Supplier<XmlPullParser> xmlPullParserProvider,
           String dateFormat, String customRootTag, Map<Class, String> classToAlias, Map<Field, String> fieldToAlias,
           Set<Field> excludedFields, XMLReader.SecurityPolicy deserializationSecurityPolicy,
           Set<SimpleStrategy> registeredSimple, Set<CompositeStrategy> registeredComposite,
           Set<SimpleStrategy> unregisteredSimple, Set<CompositeStrategy> unregisteredComposite) {
        this();
        // style:
        if (style != null) {
            style.applyTo(this.writerPrototype);
        }
        // xmlPullParserProvider:
        this.xmlPullParserProvider = xmlPullParserProvider;
        // dateFormat:
        if (dateFormat != null) {
            this.writerPrototype.setDateFormat(dateFormat);
            this.readerPrototype.setDateFormat(dateFormat);
        }
        // customRootTag:
        if (customRootTag != null) {
            this.writerPrototype.setRootTag(customRootTag);
            this.readerPrototype.setRootTag(customRootTag);
        }
        // classToAlias:
        if (classToAlias != null) {
            for (Map.Entry<Class, String> alias : classToAlias.entrySet()) {
                this.readerPrototype.alias(alias.getKey(), alias.getValue());
                this.writerPrototype.alias(alias.getKey(), alias.getValue());
            }
        }
        // fieldToAlias:
        if (fieldToAlias != null) {
            for (Map.Entry<Field, String> alias : fieldToAlias.entrySet()) {
                this.readerPrototype.alias(alias.getKey(), alias.getValue());
                this.writerPrototype.alias(alias.getKey(), alias.getValue());
            }
        }
        // excludedFields:
        if (excludedFields != null) {
            for (Field excludedField : excludedFields) {
                this.writerPrototype.exclude(excludedField);
            }
        }
        // deserializationSecurityPolicy:
        if (deserializationSecurityPolicy != null) {
            this.readerPrototype.securityPolicy = deserializationSecurityPolicy;
        }
        // registeredSimple:
        if (registeredSimple != null) {
            for (SimpleStrategy s : registeredSimple) {
                this.writerPrototype.getSimpleStrategies().add(s);
                this.readerPrototype.getSimpleStrategies().put(s.name(), s);
            }
        }
        // registeredComposite:
        if (registeredComposite != null) {
            for (CompositeStrategy s : registeredComposite) {
                this.writerPrototype.getCompositeStrategies().add(s);
                this.readerPrototype.getCompositeStrategies().put(s.name(), s);
            }
        }
        // unregisteredSimple:
        if (unregisteredSimple != null) {
            for (SimpleStrategy s : unregisteredSimple) {
                this.writerPrototype.getSimpleStrategies().remove(s);
                this.readerPrototype.getSimpleStrategies().remove(s.name());
            }
        }
        // unregisteredComposite:
        if (unregisteredComposite != null) {
            for (CompositeStrategy s : unregisteredComposite) {
                this.writerPrototype.getCompositeStrategies().remove(s);
                this.readerPrototype.getCompositeStrategies().remove(s.name());
            }
        }
    }

    /**
     * Returns the {@linkplain SimpleStrategy} with the given
     * <code>target</code>, or <code>null</code> if not found. One should also
     * be interested in the {@linkplain SimpleStrategy#strict()} value.
     *
     * @param target class of the strategy target
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
     * @return the found strategy, if any
     */
    public CompositeStrategy lookupCompositeStrategyBy(String name) {
        return this.readerPrototype.getCompositeStrategies().get(name);
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
     * @return a new shared-configuration writer
     */
    public XMLWriter newWriter(Writer out) {
        final XMLWriter ret = new XMLWriter(writerPrototype);
        ret.reset(out);
        return ret;
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
     * @return a new shared-configuration writer
     */
    public XMLWriter newWriter(OutputStream out) {
        final XMLWriter ret = new XMLWriter(writerPrototype);
        ret.reset(out);
        return ret;
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
     * @return a new shared-configuration writer
     */
    public XMLWriter newWriter(Document out) {
        final XMLWriter ret = new XMLWriter(writerPrototype);
        ret.reset(out);
        return ret;
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
     * @return a new shared-configuration reader
     */
    public XMLReader newReader(Reader in) {
        final XMLReader ret = new XMLReader(readerPrototype);
        ret.reset(in, maybeProvidedXmlPullParser());
        return ret;
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
     * @return a new shared-configuration reader
     */
    public XMLReader newReader(Document in) {
        final XMLReader ret = new XMLReader(readerPrototype);
        ret.reset(in);
        return ret;
    }

    /**
     * Serializes the given object, writing it with the given writer. Does not
     * support multiple writes; for that use an XML writer directly.
     * <br>
     * <b>Note:</b> the out parameter shall be closed by the caller.
     *
     * @param o   to serialize
     * @param out to write with
     */
    public void serialize(Object o, Writer out) {
        final XMLWriter writer = this.perThreadWriter.get();
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
     * @param o   to serialize
     * @param out to write to
     */
    public void serialize(Object o, OutputStream out) {
        this.serialize(o, new OutputStreamWriter(out));
    }

    /**
     * Serializes the given object to its EasyML string representation. Does
     * not support multiple writes, because the returned string is immutable.
     *
     * @param o single object to serialize
     * @return the serialized object string
     */
    public String serialize(Object o) {
        final StringWriter sw = new StringWriter();
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
     * @param o   to serialize
     * @param out empty DOM to populate
     */
    public void serialize(Object o, Document out) {
        final XMLWriter writer = this.perThreadWriter.get();
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
     * @return the de-serialized object
     */
    public Object deserialize(Reader in) {
        final XMLReader reader = this.perThreadReader.get();
        reader.reset(in, maybeProvidedXmlPullParser());
        return reader.read();
    }

    private XmlPullParser maybeProvidedXmlPullParser() {
        return this.xmlPullParserProvider != null ? this.xmlPullParserProvider.get() : null;
    }

    /**
     * De-serializes from the given input stream.
     * <br>
     * <b>Note:</b> the in parameter shall be closed by the caller.
     *
     * @param in to read from
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
     *               {@linkplain #serialize(java.lang.Object)}
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
     * @return the de-serialized object
     */
    public Object deserialize(Document in) {
        final XMLReader reader = this.perThreadReader.get();
        reader.reset(in);
        return reader.read();
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

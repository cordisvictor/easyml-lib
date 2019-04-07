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
import net.sourceforge.easyml.marshalling.custom.NodeListStrategy;
import net.sourceforge.easyml.marshalling.custom.NodeStrategy;
import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * EasyMLBuilder class is the builder of the {@linkplain EasyML} top-level
 * facade, containing its customization features.
 * <br/>
 * Usage example:
 * <pre>
 * final EasyML easyml = new EasyMLBuilder()
 *             .withStyle(EasyML.Style.PRETTY)
 *             .withCustomRootTag("Persons")
 *             .withAlias(Person.class, "Person")
 *             .withAlias(Person.class, "name", "Name")
 *             .build();
 * </pre> Not all possible customizations are accessible through this builder.
 * Hence, in some cases, one must use the {@linkplain XMLReader}s and
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
 * <br/>
 * <b>Note:</b> this builder implementation is <b>not</b> thread-safe
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.0
 * @see EasyML
 * @see XMLReader
 * @see XMLWriter
 * @since 1.4.0
 */
public final class EasyMLBuilder {

    private EasyML.Profile profile;
    private EasyML.Style style;
    private Supplier<XmlPullParser> xmlPullParserProvider;
    private String dateFormat;
    private String customRootTag;
    private NodeListStrategy customArrayTag;
    private NodeStrategy customStringTag;
    private Map<Class, String> classToAlias;
    private Map<Field, String> fieldToAlias;
    private Set<Field> excludedFields;
    private XMLReader.SecurityPolicy deserializationSecurityPolicy;
    private Set<SimpleStrategy> registeredSimple;
    private Set<CompositeStrategy> registeredComposite;
    private Set<SimpleStrategy> unregisteredSimple;
    private Set<CompositeStrategy> unregisteredComposite;

    /**
     * Sets the EasyML profile.
     *
     * @param profile to use
     */
    public EasyMLBuilder withProfile(EasyML.Profile profile) {
        this.profile = profile;
        return this;
    }

    /**
     * Sets the XML outputting style.
     *
     * @param style to use
     */
    public EasyMLBuilder withStyle(EasyML.Style style) {
        this.style = style;
        return this;
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
    public EasyMLBuilder withXmlPullParserProvider(Supplier<XmlPullParser> xmlPullParserProvider) {
        this.xmlPullParserProvider = xmlPullParserProvider;
        return this;
    }

    /**
     * Sets the format to use at XML date formatting and parsing. This is done
     * by re-configuring both the XML reader and writer with the given format.
     *
     * @param dateFormat to use at date formatting and parsing
     */
    public EasyMLBuilder withDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * Sets the given custom XML tag name to be used as the XML root tag,
     * replacing the default {@linkplain DTD#ELEMENT_EASYML}.
     *
     * @param customRootTag to be used as XML root tag name
     */
    public EasyMLBuilder withCustomRootTag(String customRootTag) {
        this.customRootTag = customRootTag;
        return this;
    }

    /**
     * Sets the given custom XML tag name for arrays.
     *
     * @param customArrayTag to be used for arrays
     * @param arrayType      class of the array to use this custom tag
     */
    public EasyMLBuilder withCustomArrayTag(String customArrayTag, Class arrayType) {
        this.customArrayTag = new NodeListStrategy(customArrayTag, arrayType);
        return this;
    }

    /**
     * Sets the given custom XML tag name for strings.
     *
     * @param customStringTag to be used for strings
     */
    public EasyMLBuilder withCustomStringTag(String customStringTag) {
        this.customStringTag = new NodeStrategy(customStringTag);
        return this;
    }

    /**
     * Sets the given alias form the given class.
     *
     * @param c     to alias
     * @param alias to use
     */
    public EasyMLBuilder withAlias(Class c, String alias) {
        if (this.classToAlias == null) {
            this.classToAlias = new HashMap<>();
        }
        this.classToAlias.put(c, alias);
        return this;
    }

    /**
     * Sets the given alias form the given field.
     *
     * @param f     to alias
     * @param alias to use
     */
    public EasyMLBuilder withAlias(Field f, String alias) {
        if (this.fieldToAlias == null) {
            this.fieldToAlias = new HashMap<>();
        }
        this.fieldToAlias.put(f, alias);
        return this;
    }

    /**
     * Sets the given alias form the given field of the <code>declaring</code>
     * class.
     *
     * @param declaring class declaring the field
     * @param field     the name of the field
     * @param alias     to use
     * @throws NoSuchFieldException if field is not found in the declaring class
     */
    public EasyMLBuilder withAlias(Class declaring, String field, String alias) throws NoSuchFieldException {
        return withAlias(declaring.getDeclaredField(field), alias);
    }

    /**
     * Sets exclusion on the given field.
     *
     * @param f to exclude
     */
    public EasyMLBuilder withExcluded(Field f) {
        if (this.excludedFields == null) {
            this.excludedFields = new HashSet<>();
        }
        this.excludedFields.add(f);
        return this;
    }

    /**
     * Sets exclusion on the given field, of the <code>declaring</code> class.
     *
     * @param declaring class declaring the field
     * @param field     the name of the field to exclude
     * @throws NoSuchFieldException if field is not found in the declaring class
     */
    public EasyMLBuilder withExcluded(Class declaring, String field) throws NoSuchFieldException {
        return withExcluded(declaring.getDeclaredField(field));
    }

    /**
     * Sets the deserialization security policy, which is used to define black-
     * or whitelists of classes to be checked at deserialization time.
     * <br/>
     * If a security policy is defined and an illegal class is found at read
     * time then the read will halt and throw a
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
     * @param whitelist        true if whitelist policy, false if blacklist
     * @param classes          non-null classes to add to the policy
     * @param classHierarchies non-null class-hierarchies to add to the policy
     */
    public EasyMLBuilder withSecurityPolicy(boolean whitelist, Class[] classes, Class[] classHierarchies) {
        this.deserializationSecurityPolicy = new XMLReader.SecurityPolicy(whitelist, classes, classHierarchies);
        return this;
    }

    /**
     * Registers the given strategy to both reader and writer.
     *
     * @param s to register
     */
    public EasyMLBuilder withStrategy(SimpleStrategy s) {
        if (this.registeredSimple == null) {
            this.registeredSimple = new HashSet<>();
        }
        this.registeredSimple.add(s);
        return this;
    }

    /**
     * Registers the given strategy to both reader and writer.
     *
     * @param s to register
     */
    public EasyMLBuilder withStrategy(CompositeStrategy s) {
        if (this.registeredComposite == null) {
            this.registeredComposite = new HashSet<>();
        }
        this.registeredComposite.add(s);
        return this;
    }

    /**
     * Unregisters the given strategy from both reader and writer.
     *
     * @param s to unregister
     */
    public EasyMLBuilder withoutStrategy(SimpleStrategy s) {
        if (this.unregisteredSimple == null) {
            this.unregisteredSimple = new HashSet<>();
        }
        this.unregisteredSimple.add(s);
        return this;
    }

    /**
     * Unregisters the given strategy from both reader and writer.
     *
     * @param s to unregister
     */
    public EasyMLBuilder withoutStrategy(CompositeStrategy s) {
        if (this.unregisteredComposite == null) {
            this.unregisteredComposite = new HashSet<>();
        }
        this.unregisteredComposite.add(s);
        return this;
    }

    /**
     * Builds a new {@linkplain EasyML} instance, based on the current settings.
     *
     * @return a new EasyML instance
     */
    public EasyML build() {
        // optimize:
        if (registeredSimple != null && unregisteredSimple != null) {
            registeredSimple.removeAll(unregisteredSimple);
        }
        if (registeredComposite != null && unregisteredComposite != null) {
            registeredComposite.removeAll(unregisteredComposite);
        }
        // build:
        return new EasyML(
                profile,
                style,
                xmlPullParserProvider,
                dateFormat,
                customRootTag,
                customArrayTag,
                customStringTag,
                classToAlias,
                fieldToAlias,
                excludedFields,
                deserializationSecurityPolicy,
                registeredSimple,
                registeredComposite,
                unregisteredSimple,
                unregisteredComposite
        );
    }
}

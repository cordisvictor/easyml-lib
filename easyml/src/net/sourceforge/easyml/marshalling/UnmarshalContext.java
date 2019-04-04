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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Date;

/**
 * UnmarshalContext interface defines the contextual data given to
 * {@linkplain Strategy} instances at un-marshalling stage.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.4.6
 * @since 1.0
 */
public interface UnmarshalContext {

    /**
     * Returns the given class' default constructor, if any. If found but not
     * accessible, it will be set to be accessible.
     *
     * @param <T> underlying type for the given class c
     * @param c   the class to reflect the default constructor for
     * @return class' default constructor
     * @throws NoSuchMethodException if no default constructor defined for class
     */
    <T> Constructor<T> defaultConstructorFor(Class<T> c) throws NoSuchMethodException;

    /**
     * Returns the class for the given <code>aliasOrName</code>, or throws
     * ClassNotFoundException if the given <code>aliasOrName</code> is not a
     * known class alias or class name.
     *
     * @param aliasOrName a configured class alias or a class name
     * @return the class
     * @throws ClassNotFoundException
     */
    Class classFor(String aliasOrName) throws ClassNotFoundException;

    /**
     * Returns the field for the given <code>declaring</code> class and
     * <code>aliasOrName</code> pair, or throws NoSuchFieldException if the
     * given <code>aliasOrName</code> is not a known field alias or field name,
     * within the given declaring class.
     *
     * @param declaring   class declaring the field
     * @param aliasOrName a configured field alias or a field name
     * @return the field
     * @throws NoSuchFieldException
     */
    Field fieldFor(Class declaring, String aliasOrName) throws NoSuchFieldException;

    /**
     * Calculates a descriptor detailing the current reader position inside the
     * XML. This information should be used as exception message for detailing
     * read exceptions.
     *
     * @return the reader position descriptor
     */
    String readerPositionDescriptor();

    /**
     * Parses the given date using the date format context configuration.
     *
     * @param date to parse
     * @return the parsed date
     * @throws ParseException when date is invalid w.r.t. the configuration
     */
    Date parseDate(String date) throws ParseException;

    /**
     * Returns the tag name currently being used as the XML root.
     *
     * @return XML root tag
     */
    String rootTag();
}

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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * MarshalContext interface defines the contextual data given to
 * {@linkplain Strategy} instances at marshalling stage.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.9
 */
public interface MarshalContext {

    /**
     * Returns a default instance for the given class, if any.
     *
     * @param <T> underlying type for the given class c
     * @param c the class to instantiate
     *
     * @return a default instance
     */
    <T> T defaultInstanceFor(Class<T> c) throws NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException;

    /**
     * Returns the alias for the given class, or <code>null</code> if the given
     * class is not aliased.
     *
     * @param c the class who's name to alias
     *
     * @return the alias or null
     */
    String aliasFor(Class c);

    /**
     * Returns the alias for the given class, or <code>defValue</code> if the
     * given class is not aliased.
     *
     * @param c the class who's name to alias
     * @param defValue the default value to return in case of non-aliased class
     *
     * @return the alias or the given default
     */
    String aliasFor(Class c, String defValue);

    /**
     * Returns the alias for the given field, or <code>null</code> if the given
     * field is not aliased.
     *
     * @param f the field who's name to alias
     *
     * @return the alias or null
     */
    String aliasFor(Field f);

    /**
     * Returns the alias for the given field, or <code>defValue</code> if the
     * given field is not aliased.
     *
     * @param f the field who's name to alias
     * @param defValue the default value to return in case of non-aliased field
     *
     * @return the alias or the given default
     */
    String aliasFor(Field f, String defValue);

    /**
     * Returns <code>true</code> if the given field is excluded, i.e. should be
     * skipped from marshalling, <code>false</code> otherwise.
     *
     * @param f to test if excluded
     *
     * @return true if excluded, false otherwise
     */
    boolean excluded(Field f);

    /**
     * Returns <code>true</code> if pretty printing is activated,
     * <code>false</code> otherwise.
     *
     * @return true if pretty, false otherwise
     */
    boolean prettyPrinting();

    /**
     * Returns the tag name currently being used as the XML root.
     *
     * @return XML root tag
     */
    String rootTag();

    /**
     * Returns <code>true</code> if the composite strategies doing default-value
     * checking should skip default values, <code>false</code> if the
     * default-values are to taken into consideration.
     *
     * @return true if skip, false otherwise
     */
    boolean skipDefaults();

    /**
     * Formats the given date using the date format context configuration.
     *
     * @param d to format
     *
     * @return the formatted date
     */
    String formatDate(Date d);
}

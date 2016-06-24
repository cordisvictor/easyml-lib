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
package net.sourceforge.easyml.marshalling;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * MarshalContext interface defines the contextual data given to
 * {@linkplain Strategy} instances at marshalling stage.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.3.5
 */
public interface MarshalContext {

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

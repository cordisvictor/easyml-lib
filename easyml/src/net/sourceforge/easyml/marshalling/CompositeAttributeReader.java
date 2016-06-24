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

/**
 * CompositeAttributeReader interface is used by {@linkplain CompositeStrategy}
 * instances to read composite datatype XML attributes.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.0
 */
public interface CompositeAttributeReader {

    /**
     * Returns the value of the attribute with the given name of the element
     * start this instance is at. If the attribute is not found then
     * <code>null</code> is returned.
     * <br/>
     * <b>Note:</b> this reader must be at an element start tag.
     *
     * @param name the attribute name
     *
     * @return the attribute value or null
     */
    String elementAttribute(String name);

    /**
     * Returns the value of the required attribute with the given name of the
     * element start this instance is at. If the attribute is not found then an
     * exception is thrown.
     * <br/>
     * <b>Note:</b> this reader must be at an element start tag.
     *
     * @param name the attribute name
     *
     * @return the non-null attribute value
     */
    String elementRequiredAttribute(String name);
}

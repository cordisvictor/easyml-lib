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

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
 * CompositeAttributeWriter interface is used by {@linkplain CompositeStrategy}
 * instances to write composite datatype XML attributes.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.0
 * @since 1.0
 */
public interface CompositeAttributeWriter {

    /**
     * Writes an attribute-equals-value pair to the current start tag attribute
     * list.
     * <br/>
     * <b>Note:</b> this writer must be at an element start tag.
     *
     * @param attribute the attribute name
     * @param value     the attribute value
     */
    void setAttribute(String attribute, String value);
}

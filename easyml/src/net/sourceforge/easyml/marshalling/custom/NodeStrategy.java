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
package net.sourceforge.easyml.marshalling.custom;

import net.sourceforge.easyml.marshalling.*;
import net.sourceforge.easyml.util.XMLUtil;

/**
 * NodeStrategy class that implements the {@linkplain SimpleStrategy} interface
 * for custom-tagged strings. This implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0.4
 * @version 1.0.4
 */
public final class NodeStrategy extends AbstractStrategy<String> implements SimpleStrategy<String> {

    private final String name;

    /**
     * Creates a new instance with the given name.
     *
     * @param name the name of the XML element
     */
    public NodeStrategy(String name) {
        if (!XMLUtil.isLegalXMLTag(name)) {
            throw new IllegalArgumentException("name: " + name);
        }
        this.name = name;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<String> c) {
        return c == String.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean strict() {
        return true;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<String> target() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(String target, MarshalContext ctx) {
        return target;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String unmarshal(String text, UnmarshalContext ctx) {
        return text;
    }
}

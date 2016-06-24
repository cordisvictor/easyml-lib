/*
 * Copyright (c) 2015, Victor Cordis. All rights reserved.
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

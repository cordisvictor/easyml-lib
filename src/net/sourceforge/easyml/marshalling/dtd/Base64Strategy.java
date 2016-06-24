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
package net.sourceforge.easyml.marshalling.dtd;

import net.sourceforge.easyml.DTD;
import net.sourceforge.easyml.marshalling.AbstractStrategy;
import net.sourceforge.easyml.marshalling.MarshalContext;
import net.sourceforge.easyml.marshalling.UnmarshalContext;
import net.sourceforge.easyml.marshalling.SimpleStrategy;
import net.sourceforge.migbase64.util.Base64;

/**
 * Base64Strategy class that implements the {@linkplain SimpleStrategy}
 * interface for the DTD base64 datatype, i.e. for serializing byte arrays. This
 * implementation is thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.0
 * @version 1.0
 */
public final class Base64Strategy extends AbstractStrategy<byte[]> implements SimpleStrategy<byte[]> {

    /**
     * Constant defining the singleton instance.
     */
    public static final Base64Strategy INSTANCE = new Base64Strategy();

    private Base64Strategy() {
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
    public Class<byte[]> target() {
        return byte[].class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean appliesTo(Class<byte[]> c) {
        return c == byte[].class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return DTD.TYPE_BASE64;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String marshal(byte[] target, MarshalContext ctx) {
        return Base64.encodeToString(target, true);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public byte[] unmarshal(String text, UnmarshalContext ctx) {
        return Base64.decode(text);
    }
}//class Base64Strategy.

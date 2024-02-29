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
 * @version 1.0
 * @since 1.0
 */
public final class Base64Strategy extends AbstractStrategy implements SimpleStrategy<byte[]> {

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
    public Class<byte[]> target() {
        return byte[].class;
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
}

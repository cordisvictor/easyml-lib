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
package net.sourceforge.easyml.marshalling.java.util;

import net.sourceforge.easyml.marshalling.*;

import java.util.HexFormat;

/**
 * HexFormatStrategy class that implements the {@linkplain CompositeStrategy}
 * interface for the Java {@linkplain HexFormat}. This implementation is
 * thread-safe.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.7.2
 * @since 1.7.2
 */
public final class HexFormatStrategy extends AbstractStrategy implements CompositeStrategy<HexFormat> {

    /**
     * Constant defining the value used for the strategy name.
     */
    public static final String NAME = "hexfmt";
    /**
     * Constant defining the singleton instance.
     */
    public static final HexFormatStrategy INSTANCE = new HexFormatStrategy();
    private static final String ATTRIBUTE_DELIMITER = "delimiter";
    private static final String ATTRIBUTE_PREFIX = "prefix";
    private static final String ATTRIBUTE_SUFFIX = "suffix";
    private static final String ATTRIBUTE_IS_UPPER = "upper";

    private HexFormatStrategy() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<HexFormat> target() {
        return HexFormat.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String name() {
        return HexFormatStrategy.NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void marshal(HexFormat target, CompositeWriter writer, MarshalContext ctx) {
        writer.startElement(HexFormatStrategy.NAME);
        writeAttrIfNotEmpty(writer, ATTRIBUTE_DELIMITER, target.delimiter());
        writeAttrIfNotEmpty(writer, ATTRIBUTE_PREFIX, target.prefix());
        writeAttrIfNotEmpty(writer, ATTRIBUTE_SUFFIX, target.suffix());
        if (target.isUpperCase()) {
            writer.setAttribute(HexFormatStrategy.ATTRIBUTE_IS_UPPER, String.valueOf(true));
        }
        writer.endElement();
    }

    private static void writeAttrIfNotEmpty(CompositeWriter writer, String attr, String value) {
        if (!value.isEmpty()) {
            writer.setAttribute(attr, value);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public HexFormat unmarshalNew(CompositeReader reader, UnmarshalContext ctx) {
        HexFormat result;
        // read the attributes set at marshalling:
        try {
            final String delimiter = reader.elementAttribute(HexFormatStrategy.ATTRIBUTE_DELIMITER);
            result = delimiter != null ? HexFormat.ofDelimiter(delimiter) : HexFormat.of();

            final String prefix = reader.elementAttribute(HexFormatStrategy.ATTRIBUTE_PREFIX);
            if (prefix != null) {
                result = result.withPrefix(prefix);
            }
            final String suffix = reader.elementAttribute(HexFormatStrategy.ATTRIBUTE_SUFFIX);
            if (suffix != null) {
                result = result.withSuffix(suffix);
            }
            final boolean isUpper = Boolean.parseBoolean(reader.elementAttribute(ATTRIBUTE_IS_UPPER));
            if (isUpper) {
                result = result.withUpperCase();
            }
        } finally {
            reader.next(); // moved the reader on the root element end.
        }
        return result;
    }
}

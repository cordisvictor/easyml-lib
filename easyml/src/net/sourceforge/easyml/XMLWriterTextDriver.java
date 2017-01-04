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
package net.sourceforge.easyml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.easyml.util.XMLUtil;

/**
 * XMLWriterTextDriver class is the XML writer driver implementation for writing
 * XML to output streams.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.1.0
 * @version 1.4.1
 */
/* default */ final class XMLWriterTextDriver extends XMLWriter.Driver {

    private static final String XML_NEWLINE = System.getProperty("line.separator");
    private static final String XML_FRAGMENT_SLASH_GT = "/>";
    private static final String XML_FRAGMENT_LT_SLASH = "</";
    private static final int XML_INDENTATION_INIT = -1;
    private static final char[] XML_INDENTATION_BUF
            = new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};

    private final Writer writer;
    private final List<String> elementStack;

    /**
     * Creates a new instance.
     *
     * @param target to use and be used by
     * @param out to write to
     */
    public XMLWriterTextDriver(XMLWriter target, Writer out) {
        super(target);
        this.writer = out;
        this.elementStack = new ArrayList<>(XML_INDENTATION_BUF.length);
    }

    private void writeIndent() throws IOException {
        this.writer.write(XML_NEWLINE);
        int size = XML_INDENTATION_INIT + this.elementStack.size();
        while (size >= XML_INDENTATION_BUF.length) {
            this.writer.write(XML_INDENTATION_BUF);
            size -= XML_INDENTATION_BUF.length;
        }
        if (size > 0) {
            this.writer.write(XML_INDENTATION_BUF, 0, size);
        }
    }

    private void writeIndentedLt() throws IOException {
        if (this.isPrettyPrint()) {
            this.writeIndent();
        }
        this.writer.write('<');
    }

    private void writeIndentedLtSlash() throws IOException {
        if (this.isPrettyPrint()) {
            this.writeIndent();
        }
        this.writer.write(XMLWriterTextDriver.XML_FRAGMENT_LT_SLASH);
    }

    private void writeAttrEqValue(String attr, String value) throws IOException {
        this.writer.write(' ');
        this.writer.write(attr);
        this.writer.write("=\"");
        this.writer.write(value);
        this.writer.write('\"');
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void startElement(String name) {
        try {
            if (this.state == XMLWriter.Driver.STATE_START) {
                this.writer.write('>');
                writeIndentedLt();
            } else if (this.state == XMLWriter.Driver.STATE_INITIAL) {
                this.writer.write('<');
            } else if (this.state != XMLWriter.Driver.STATE_VALUE) {
                throw new IllegalStateException("cannot write element start");
            } else {
                writeIndentedLt();
            }
            this.writer.write(name);
            if (this.hasOneTimeUniqueId()) {
                writeAttrEqValue(DTD.ATTRIBUTE_ID, this.oneTimeUniqueId());
            }
            // update state:
            this.elementStack.add(name);
            this.state = XMLWriter.Driver.STATE_START;
        } catch (IOException ioX) {
            throw new RuntimeException(ioX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setAttribute(String attribute, String value) {
        if (this.state != XMLWriter.Driver.STATE_START) {
            throw new IllegalStateException("cannot write element attributes");
        }
        try {
            writeAttrEqValue(XMLUtil.escapeXML(attribute), XMLUtil.escapeXML(value));
        } catch (IOException ioX) {
            throw new RuntimeException(ioX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void endElement() {
        if (this.state == XMLWriter.Driver.STATE_INITIAL) {
            throw new IllegalStateException("cannot write element end");
        }
        // proceed with end element: first remove from stack because stack size is used for indenting end tag:
        try {
            final String endTag = this.elementStack.remove(this.elementStack.size() - 1);
            if (this.state == XMLWriter.Driver.STATE_START) {
                this.writer.write(XMLWriterTextDriver.XML_FRAGMENT_SLASH_GT);
            } else {
                if (this.state == XMLWriter.Driver.STATE_VALUE) {
                    writeIndentedLtSlash();
                } else { // XMLWriter.Driver.STATE_VALUE_END:
                    this.writer.write(XML_FRAGMENT_LT_SLASH);
                }
                this.writer.write(endTag);
                this.writer.write('>');
            }
            // update state:
            this.state = XMLWriter.Driver.STATE_VALUE;
        } catch (IOException ioX) {
            throw new RuntimeException(ioX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void writeValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value: null");
        }
        try {
            if (this.state == XMLWriter.Driver.STATE_START) {
                this.writer.write('>');
                this.state = XMLWriter.Driver.STATE_VALUE;
            } else if (this.state != XMLWriter.Driver.STATE_VALUE) {
                throw new IllegalStateException("cannot write value");
            }
            this.writer.write(XMLUtil.escapeXML(value));
            this.state = STATE_VALUE_END;
        } catch (IOException ioX) {
            throw new RuntimeException(ioX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void flush() {
        super.flush();
        try {
            this.writer.flush();
        } catch (IOException ioX) {
            throw new RuntimeException(ioX);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() {
        super.close();
        try {
            this.writer.close();
        } catch (IOException ioX) {
            // ignore.
        }
        this.elementStack.clear();
    }
}

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
package net.sourceforge.easyml;

import net.sourceforge.easyml.util.XMLUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * XMLWriterTextDriver class is the XML writer driver implementation for writing
 * XML to output streams.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.8.3
 * @since 1.1.0
 */
final class XMLWriterTextDriver extends XMLWriter.Driver {

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
     * @param out    to write to
     */
    public XMLWriterTextDriver(XMLWriter target, Writer out) {
        super(target);
        this.writer = out;
        this.elementStack = new ArrayList<>();
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
            final String escapedName = XMLUtil.escapeXMLTag(name);
            this.writer.write(escapedName);
            this.writeOneTimeUniqueId(id -> tryWriteAttrEqValue(DTD.ATTRIBUTE_ID, id));
            // update state:
            this.elementStack.add(escapedName);
            this.state = XMLWriter.Driver.STATE_START;
        } catch (IOException ioX) {
            throw new RuntimeException(ioX);
        }
    }

    private void writeIndentedLt() throws IOException {
        if (this.isPrettyPrint()) {
            this.writeIndent();
        }
        this.writer.write('<');
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

    private void tryWriteAttrEqValue(String attr, String value) {
        try {
            writeAttrEqValue(attr, value);
        } catch (IOException ioX) {
            throw new RuntimeException(ioX);
        }
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
    public void setAttribute(String attribute, String value) {
        if (!XMLUtil.isLegalXMLTag(attribute)) {
            throw new IllegalArgumentException("attribute: " + attribute);
        }
        if (this.state != XMLWriter.Driver.STATE_START) {
            throw new IllegalStateException("cannot write element attributes");
        }
        try {
            writeAttrEqValue(attribute, XMLUtil.escapeXML(value));
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

    private void writeIndentedLtSlash() throws IOException {
        if (this.isPrettyPrint()) {
            this.writeIndent();
        }
        this.writer.write(XMLWriterTextDriver.XML_FRAGMENT_LT_SLASH);
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

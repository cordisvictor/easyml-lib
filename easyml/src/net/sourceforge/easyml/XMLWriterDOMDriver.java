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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * XMLWriterDOMDriver class is the XML reader driver implementation for writing
 * XML to DOM documents.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.3.9
 * @since 1.1.0
 */
final class XMLWriterDOMDriver extends XMLWriter.Driver {

    private Document root;
    private Element crt;

    /**
     * Creates a new instance.
     *
     * @param target to use and be used by
     * @param out    to write to
     */
    public XMLWriterDOMDriver(XMLWriter target, Document out) {
        super(target);
        if (out.hasChildNodes()) {
            throw new IllegalArgumentException("out: not empty");
        }
        this.root = out;
        this.crt = null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void startElement(String name) {
        if (this.state == XMLWriter.Driver.STATE_INITIAL || this.state == XMLWriter.Driver.STATE_START) {
            this.state = XMLWriter.Driver.STATE_VALUE;
        } else if (this.state != XMLWriter.Driver.STATE_VALUE) {
            throw new IllegalStateException("cannot write element start");
        }
        // update state:
        final Element started = this.root.createElement(name);
        if (this.crt == null) {
            this.root.appendChild(started);
        } else {
            this.crt.appendChild(started);
        }
        this.crt = started;
        this.writeOneTimeUniqueId(id -> crt.setAttribute(DTD.ATTRIBUTE_ID, id));
        this.state = XMLWriter.Driver.STATE_START;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setAttribute(String attribute, String value) {
        if (this.state != XMLWriter.Driver.STATE_START) {
            throw new IllegalStateException("cannot write element attributes");
        }
        this.crt.setAttribute(attribute, value);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void endElement() {
        if (this.state == XMLWriter.Driver.STATE_INITIAL) {
            throw new IllegalStateException("cannot write element end");
        }
        // update state:
        final Node parent = this.crt.getParentNode();
        this.crt = parent.getNodeType() == Node.ELEMENT_NODE ? (Element) parent : null;
        this.state = XMLWriter.Driver.STATE_VALUE;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void writeValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value: null");
        }
        if (this.state == XMLWriter.Driver.STATE_START) {
            this.state = XMLWriter.Driver.STATE_VALUE;
        } else if (this.state != XMLWriter.Driver.STATE_VALUE) {
            throw new IllegalStateException("cannot write value");
        }
        this.crt.appendChild(this.root.createTextNode(value));
        this.state = STATE_VALUE_END;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() {
        super.close();
        this.root = null;
        this.crt = null;
    }
}

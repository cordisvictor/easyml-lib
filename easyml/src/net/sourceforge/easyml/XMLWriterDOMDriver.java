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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * XMLWriterDOMDriver class is the XML reader driver implementation for writing
 * XML to DOM documents.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.1.0
 * @version 1.3.9
 */
/* default */ final class XMLWriterDOMDriver extends XMLWriter.Driver {

    private Document root;
    private Element crt;

    /**
     * Creates a new instance.
     *
     * @param target to use and be used by
     * @param out to write to
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
        final Element started = this.root.createElement(name);
        if (this.hasOneTimeUniqueId()) {
            started.setAttribute(DTD.ATTRIBUTE_ID, this.oneTimeUniqueId());
        }
        // update state:
        if (this.crt == null) {
            this.root.appendChild(started);
        } else {
            this.crt.appendChild(started);
        }
        this.crt = started;
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

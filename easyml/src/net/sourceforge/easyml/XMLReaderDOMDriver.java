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
import org.w3c.dom.NodeList;

/**
 * XMLReaderDOMDriver class is the XML reader driver implementation for reading
 * already parsed XML from DOM documents. This implementation adapts the
 * pull-parsing API, meant for working with XML text, so that it works for
 * already parsed XML in the form of DOM. This prevents workarounds such as
 * transforming the DOM to text so that it can be inputed as text to EasyML.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @since 1.1.0
 * @version 1.3.9
 */
/* default */ final class XMLReaderDOMDriver extends XMLReader.Driver {

    private Document root;
    private Element crt;
    private boolean crtAtEnd;

    /**
     * Creates a new instance.
     *
     * @param target to use and be used by
     * @param in to read from
     */
    public XMLReaderDOMDriver(XMLReader target, Document in) {
        super(target);
        if (!in.hasChildNodes()) {
            throw new IllegalArgumentException("in: is empty, should contain at least the easyml node");
        }
        this.root = in;
        this.crt = null;
        this.crtAtEnd = false;
    }

    /**
     * Returns a path-like string to indicate the position in the XML DOM.
     *
     * @return path position string
     */
    @Override
    public String positionDescriptor() {
        final StringBuilder descriptorBuilder = new StringBuilder(32);
        final StringBuilder eBuilder = new StringBuilder(24);
        Element e = this.crt;
        while (e != null) {
            eBuilder.append('/').append(e.getNodeName());
            if (e.hasAttribute(DTD.ATTRIBUTE_ID)) {
                eBuilder.append("[@").append(DTD.ATTRIBUTE_ID)
                        .append("='").append(e.getAttribute(DTD.ATTRIBUTE_ID)).append("']");
            }
            descriptorBuilder.insert(0, eBuilder);
            eBuilder.delete(0, eBuilder.length());
            final Node ep = e.getParentNode();
            e = ep.getNodeType() == Node.ELEMENT_NODE ? (Element) ep : null;
        }
        return descriptorBuilder.toString();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean next() {
        // if initially, set to root:
        if (this.crt == null) {
            this.crt = this.root.getDocumentElement();
            this.crtAtEnd = false;
            return true;
        }
        if (!this.crtAtEnd) {
            // if !atEnd and we can go down:
            final NodeList crtNL = this.crt.getChildNodes();
            final int crtNLlen = crtNL.getLength();
            for (int i = 0; i < crtNLlen; i++) {
                final Node iN = crtNL.item(i);
                if (iN.getNodeType() == Node.ELEMENT_NODE) {
                    this.crt = (Element) iN;
                    return true;
                }
            }
            // if we go to end tag:
            if (crtNLlen == 0 || this.crt.getLastChild().getNodeType() == Node.TEXT_NODE) {
                this.crtAtEnd = true;
                return true;
            }
        }
        // if we go to sibling start tag:
        if (this.crtAtEnd) {
            Node sN = this.crt.getNextSibling();
            while (sN != null && sN.getNodeType() != Node.ELEMENT_NODE) {
                sN = sN.getNextSibling();
            }
            if (sN != null && sN.getNodeType() == Node.ELEMENT_NODE) {
                this.crt = (Element) sN;
                this.crtAtEnd = false;
                return true;
            }
            // if we go up (at end=true):
            final Node pN = this.crt.getParentNode();
            if (pN != null && pN.getNodeType() == Node.ELEMENT_NODE) {
                this.crt = (Element) pN;
                this.crtAtEnd = true;
                return true;
            }
        }
        // up == null => return false:
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean atElementStart() {
        return this.crt != null && !this.crtAtEnd;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean atElementEnd() {
        return this.crt != null && this.crtAtEnd;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String elementName() {
        if (this.crt == null) {
            throw new IllegalStateException("not at element start or end: " + this.positionDescriptor());
        }
        return this.crt.getNodeName();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String elementAttribute(String name) {
        if (this.crt == null || this.crtAtEnd) {
            throw new IllegalStateException("not at element start: " + this.positionDescriptor());
        }
        // signal attribute not present using null not "":
        return this.crt.hasAttribute(name) ? this.crt.getAttribute(name) : null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String elementRequiredAttribute(String name) {
        if (this.crt == null || this.crtAtEnd) {
            throw new IllegalStateException("not at element start: " + this.positionDescriptor());
        }
        if (this.crt.hasAttribute(name)) {
            return this.crt.getAttribute(name);
        }
        throw new InvalidFormatException(this.positionDescriptor(), "element missing attribute: " + name);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String readValue() {
        if (this.crt != null && !this.crtAtEnd) {
            final Node fcn = this.crt.getLastChild();
            if (fcn.getNodeType() == Node.TEXT_NODE) {
                this.crtAtEnd = true;
                return fcn.getTextContent();
            }
        }
        throw new IllegalStateException("not at element start: " + this.positionDescriptor());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void consumeFully() {
        // move up until easyml is parent:
        final String rootTag = this.rootTag();
        Element e = this.crt;
        Node parent = e.getParentNode();
        while (parent.getNodeType() == Node.ELEMENT_NODE && !parent.getNodeName().equals(rootTag)) {
            e = (Element) parent;
            parent = e.getParentNode();
        }
        // search next sibling, if any, or move to easyml end:
        Node sibling = e.getNextSibling();
        while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
            sibling = sibling.getNextSibling();
        }
        if (sibling != null) {
            this.crt = (Element) sibling;
            this.crtAtEnd = false;
        } else {
            this.crt = (Element) parent;
            this.crtAtEnd = true;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() {
        this.root = null;
        this.crt = null;
    }
}

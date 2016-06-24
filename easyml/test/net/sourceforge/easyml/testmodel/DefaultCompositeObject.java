/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.easyml.testmodel;

import java.io.Serializable;

/**
 *
 * @author victor
 */
public class DefaultCompositeObject implements Serializable {

    private int extended;
    private DefaultObject defObject;

    public DefaultCompositeObject() {
        extended = 2;
        defObject = new DefaultObject(2, "N2");
    }

    public int getExtended() {
        return extended;
    }

    public void setExtended(int extended) {
        this.extended = extended;
    }

    public DefaultObject getDefObject() {
        return defObject;
    }

    public void setDefObject(DefaultObject defObject) {
        this.defObject = defObject;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.extended;
        hash = 41 * hash + (this.defObject != null ? this.defObject.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultCompositeObject other = (DefaultCompositeObject) obj;
        if (this.extended != other.extended) {
            return false;
        }
        if (this.defObject != other.defObject && (this.defObject == null || !this.defObject.equals(other.defObject))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DefaultCompositeObject{" + "extended=" + extended + ", defObject=" + defObject + '}';
    }

}

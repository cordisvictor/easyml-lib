package net.sourceforge.easyml.testmodel;

import java.io.Serializable;

/**
 * @author Victor Cordis
 */
public abstract class AbstractDTO implements Serializable {

    private static final long serialVersionUID = 4886226420673360563L;
    protected int id;

    public AbstractDTO() {
    }

    public AbstractDTO(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractDTO other = (AbstractDTO) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.id;
        return hash;
    }

    @Override
    public String toString() {
        return "id=" + id;
    }
}

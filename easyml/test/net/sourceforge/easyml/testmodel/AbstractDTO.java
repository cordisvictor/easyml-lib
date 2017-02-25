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
}

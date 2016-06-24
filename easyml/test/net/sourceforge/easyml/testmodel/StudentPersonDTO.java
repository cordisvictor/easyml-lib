package net.sourceforge.easyml.testmodel;

/**
 * @author Victor Cordis
 */
public final class StudentPersonDTO extends PersonDTO {

    private static final long serialVersionUID = -1757734698117151944L;
    private boolean active;
    private FacultyDTO faculty;

    public StudentPersonDTO() {
    }

    public StudentPersonDTO(int id, String firstName, String lastName,
            boolean active, FacultyDTO faculty) {
        super(id, firstName, lastName);
        this.active = active;
        this.faculty = faculty;
    }

    /**
     * Get the value of faculty
     * 
     * @return the value of faculty
     */
    public FacultyDTO getFaculty() {
        return faculty;
    }

    /**
     * Get the value of active
     * 
     * @return the value of active
     */
    public boolean isActive() {
        return active;
    }

    // /**
    // * Set the value of active
    // *
    // * @param active new value of active
    // */
    // public void setActive(boolean active) {
    // this.active = active;
    // }
    @Override
    public String toString() {
        return super.toString() + ", active=" + active + ", faculty=" + faculty;
    }
}

package net.sourceforge.easyml.testmodel;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.active ? 1 : 0);
        if (this.faculty != null) {
            hash = 17 * hash + Objects.hashCode(this.faculty.id);
        }
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
        final StudentPersonDTO other = (StudentPersonDTO) obj;
        if (this.active != other.active) {
            return false;
        }
        if (this.faculty != null) {
            if (!Objects.equals(this.faculty.id, other.faculty.id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "StudentPersonDTO{" + "active=" + active + ", faculty=" + (faculty != null ? faculty.id : "unknown") + '}';
    }
}

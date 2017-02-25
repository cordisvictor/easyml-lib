package net.sourceforge.easyml.testmodel;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Victor Cordis
 */
public class FacultyDTO extends AbstractDTO {

    private static final long serialVersionUID = 5921339868082694628L;
    private String name;
    private StudentPersonDTO[] students;

    public FacultyDTO() {
        this.name = "N/A";
        this.students = new StudentPersonDTO[0];
    }

    public FacultyDTO(int id) {
        super(id);
    }

    public FacultyDTO(int id, String name) {
        super(id);
        this.name = name;
    }

    public FacultyDTO(int id, String name, StudentPersonDTO[] students) {
        super(id);
        this.name = name;
        this.students = students;
    }

    public String getName() {
        return name;
    }

    public StudentPersonDTO[] getStudents() {
        return students;
    }

    public void setStudents(StudentPersonDTO[] students) {
        this.students = students;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Arrays.deepHashCode(this.students);
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
        final FacultyDTO other = (FacultyDTO) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Arrays.deepEquals(this.students, other.students);
    }

    @Override
    public String toString() {
        return "FacultyDTO{" + "name=" + name + ", students=" + Arrays.toString(students) + '}';
    }

}

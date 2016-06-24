package net.sourceforge.easyml.testmodel;

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
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(", name=").append(name);
        if (this.students != null) {
            for (StudentPersonDTO student : this.students) {
                sb.append(',');
                sb.append(student);
            }
        }
        return sb.toString();
    }
}

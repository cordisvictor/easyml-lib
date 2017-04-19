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

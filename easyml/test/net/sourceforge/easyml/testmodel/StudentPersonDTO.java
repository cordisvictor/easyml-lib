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

    public FacultyDTO getFaculty() {
        return faculty;
    }

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

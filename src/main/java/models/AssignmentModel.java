package models;

class AssignmentModel {
    private int assignmentId, moduleId, academicYear, semester;

    public AssignmentModel() {
    }

    public AssignmentModel(int assignmentId, int moduleId, int academicYear, int semester) {
        this.assignmentId = assignmentId;
        this.moduleId = moduleId;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return "AssignmentModel{" +
                "assignmentId=" + assignmentId +
                ", moduleId=" + moduleId +
                ", academicYear=" + academicYear +
                ", semester=" + semester +
                '}';
    }
}

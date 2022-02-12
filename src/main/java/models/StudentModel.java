package models;

class StudentModel {
    private String studentId;

    public StudentModel() {
    }

    public StudentModel(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }

    @Override
    public String toString() {
        return "StudentModel{" +
                "studentId='" + studentId + '\'' +
                '}';
    }
}

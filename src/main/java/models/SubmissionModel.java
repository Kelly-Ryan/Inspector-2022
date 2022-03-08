package models;

public class SubmissionModel {
    private int assignmentId, moduleId;
    private String studentId, assignmentText, comments;
    private float maxMarks, receivedMarks;

    public SubmissionModel() {
    }

    public SubmissionModel(int moduleId, String studentId, String assignmentText, String comments,
                           float maxMarks, float receivedMarks) {
        this.moduleId = moduleId;
        this.studentId = studentId;
        this.assignmentText = assignmentText;
        this.comments = comments;
        this.maxMarks = maxMarks;
        this.receivedMarks = receivedMarks;
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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getAssignmentText() {
        return assignmentText;
    }

    public void setAssignmentText(String assignmentText) {
        this.assignmentText = assignmentText;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public float getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(float maxMarks) {
        this.maxMarks = maxMarks;
    }

    public float getReceivedMarks() {
        return receivedMarks;
    }

    public void setReceivedMarks(float receivedMarks) {
        this.receivedMarks = receivedMarks;
    }

    @Override
    public String toString() {
        return "SubmissionModel{" +
                "assignmentId=" + assignmentId +
                ", moduleId=" + moduleId +
                ", studentId='" + studentId + '\'' +
                ", assignmentText='" + assignmentText + '\'' +
                ", comments='" + comments + '\'' +
                ", maxMarks=" + maxMarks +
                ", receivedMarks=" + receivedMarks +
                '}';
    }
}
